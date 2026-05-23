package app.capgo.plugin.launch_navigator;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.getcapacitor.Bridge;
import com.getcapacitor.FileUtils;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

public class LaunchNavigator {

    private static final long DEFAULT_ICON_CACHE_MAX_AGE_MS = 24L * 60L * 60L * 1000L;
    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 8000;
    private static final int MAX_ICON_BYTES = 2 * 1024 * 1024;
    private static final int MAX_HTML_BYTES = 512 * 1024;
    private static final String ICON_CACHE_DIR = "launch_navigator_icons";
    private static final Pattern ICON_LINK_PATTERN = Pattern.compile(
        "<link\\s+[^>]*rel=[\"'][^\"']*(?:apple-touch-icon|icon)[^\"']*[\"'][^>]*>",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern HREF_PATTERN = Pattern.compile("\\shref=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    private final Context context;
    private final Bridge bridge;
    private final Map<String, Object> iconCacheLocks = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock iconCacheDirectoryLock = new ReentrantReadWriteLock();
    private Map<String, AppInfo> navigationApps;

    private static class AppInfo {

        String name;
        String[] packageNames;
        String url;

        AppInfo(String name, String url, String... packageNames) {
            this.name = name;
            this.url = url;
            this.packageNames = packageNames;
        }
    }

    private static class IconProvider {

        String app;
        String name;
        String url;
        String iconUrl;

        IconProvider(String app, String name, String url, String iconUrl) {
            this.app = app;
            this.name = name;
            this.url = url;
            this.iconUrl = iconUrl;
        }
    }

    private static class CachedIcon {

        File file;
        JSONObject metadata;

        CachedIcon(File file, JSONObject metadata) {
            this.file = file;
            this.metadata = metadata;
        }
    }

    private static class DownloadedIcon {

        byte[] data;
        String sourceUrl;
        String mimeType;

        DownloadedIcon(byte[] data, String sourceUrl, String mimeType) {
            this.data = data;
            this.sourceUrl = sourceUrl;
            this.mimeType = mimeType;
        }
    }

    public LaunchNavigator(Context context, Bridge bridge) {
        this.context = context;
        this.bridge = bridge;
        initializeApps();
    }

    private void initializeApps() {
        navigationApps = new LinkedHashMap<>();
        navigationApps.put("google_maps", new AppInfo("Google Maps", "https://www.google.com/maps", "com.google.android.apps.maps"));
        navigationApps.put("waze", new AppInfo("Waze", "https://www.waze.com", "com.waze"));
        navigationApps.put("citymapper", new AppInfo("Citymapper", "https://citymapper.com", "com.citymapper.app.release"));
        navigationApps.put("uber", new AppInfo("Uber", "https://www.uber.com", "com.ubercab"));
        navigationApps.put("yandex", new AppInfo("Yandex Navigator", "https://yandex.com/maps", "ru.yandex.yandexnavi"));
        navigationApps.put("sygic", new AppInfo("Sygic", "https://www.sygic.com/gps-navigation", "com.sygic.aura"));
        navigationApps.put("here", new AppInfo("HERE Maps", "https://wego.here.com", "com.here.app.maps"));
        navigationApps.put("moovit", new AppInfo("Moovit", "https://moovitapp.com", "com.tranzmate"));
        navigationApps.put("lyft", new AppInfo("Lyft", "https://www.lyft.com", "me.lyft.android"));
        navigationApps.put("mapsme", new AppInfo("MAPS.ME", "https://maps.me", "com.mapswithme.maps.pro"));
        navigationApps.put("tomtom", new AppInfo("TomTom GO", "https://www.tomtom.com", "com.tomtom.gplay.navapp"));
        navigationApps.put("guru_maps", new AppInfo("Guru Maps", "https://gurumaps.app", "com.bodunov.galileo", "com.bodunov.GalileoPro"));
        navigationApps.put("organic_maps", new AppInfo("Organic Maps", "https://organicmaps.app", "app.organicmaps"));
        navigationApps.put("yandex_maps", new AppInfo("Yandex Maps", "https://yandex.com/maps", "ru.yandex.yandexmaps"));
        navigationApps.put("mapy", new AppInfo("Mapy.com", "https://mapy.com", "cz.seznam.mapy"));
        navigationApps.put("2gis", new AppInfo("2GIS", "https://2gis.com", "ru.dublgis.dgismobile"));
        navigationApps.put("cabify", new AppInfo("Cabify", "https://cabify.com", "com.cabify.rider"));
        navigationApps.put("baidu", new AppInfo("Baidu Maps", "https://map.baidu.com", "com.baidu.BaiduMap"));
        navigationApps.put("gaode", new AppInfo("Gaode Maps", "https://www.amap.com", "com.autonavi.minimap"));
        navigationApps.put("tesla", new AppInfo("Tesla", "https://www.tesla.com", "com.teslamotors.tesla"));
    }

    public boolean navigate(
        String app,
        double lat,
        double lon,
        Double startLat,
        Double startLon,
        String startName,
        String destinationName,
        String transportMode
    ) {
        try {
            Intent intent = createNavigationIntent(app, lat, lon, startLat, startLon, destinationName, transportMode);

            if (intent != null && canResolveIntent(intent)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Intent createNavigationIntent(
        String app,
        double lat,
        double lon,
        Double startLat,
        Double startLon,
        String destinationName,
        String transportMode
    ) {
        switch (app) {
            case "google_maps":
                return createGoogleMapsIntent(lat, lon, startLat, startLon, transportMode);
            case "waze":
                return createWazeIntent(lat, lon);
            case "citymapper":
                return createCitymapperIntent(lat, lon, startLat, startLon);
            case "uber":
                return createUberIntent(lat, lon, startLat, startLon);
            case "yandex":
                return createYandexIntent(lat, lon, startLat, startLon);
            case "sygic":
                return createSygicIntent(lat, lon);
            case "here":
                return createHereIntent(lat, lon, startLat, startLon);
            case "moovit":
                return createMoovitIntent(lat, lon, startLat, startLon);
            case "lyft":
                return createLyftIntent(lat, lon);
            case "mapsme":
                return createMapsMeIntent(lat, lon);
            case "tomtom":
                return createTomTomIntent(lat, lon);
            case "guru_maps":
                return createGuruMapsIntent(lat, lon, startLat, startLon, transportMode);
            case "organic_maps":
                return createOrganicMapsIntent(lat, lon, startLat, startLon, destinationName, transportMode);
            case "yandex_maps":
                return createYandexMapsIntent(lat, lon, startLat, startLon);
            case "mapy":
                return createMapyIntent(lat, lon, transportMode);
            case "2gis":
                return create2GisIntent(lat, lon, startLat, startLon, transportMode);
            case "cabify":
                return createCabifyIntent(lat, lon, startLat, startLon);
            case "baidu":
                return createBaiduIntent(lat, lon, startLat, startLon);
            case "gaode":
                return createGaodeIntent(lat, lon, startLat, startLon);
            case "tesla":
                return createTeslaIntent(lat, lon, destinationName);
            default:
                return null;
        }
    }

    private Intent createGoogleMapsIntent(double lat, double lon, Double startLat, Double startLon, String transportMode) {
        String uri;
        if (startLat != null && startLon != null) {
            uri = String.format(Locale.US, "google.navigation:q=%f,%f&mode=%s", lat, lon, getGoogleMapsMode(transportMode));
        } else {
            uri = String.format(Locale.US, "google.navigation:q=%f,%f&mode=%s", lat, lon, getGoogleMapsMode(transportMode));
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        return intent;
    }

    private String getGoogleMapsMode(String transportMode) {
        switch (transportMode) {
            case "walking":
                return "w";
            case "bicycling":
                return "b";
            case "transit":
                return "r";
            case "driving":
            default:
                return "d";
        }
    }

    private Intent createWazeIntent(double lat, double lon) {
        String uri = String.format(Locale.US, "waze://?ll=%f,%f&navigate=yes", lat, lon);
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    private Intent createCitymapperIntent(double lat, double lon, Double startLat, Double startLon) {
        String uri = String.format(Locale.US, "citymapper://directions?endcoord=%f,%f", lat, lon);
        if (startLat != null && startLon != null) {
            uri += String.format(Locale.US, "&startcoord=%f,%f", startLat, startLon);
        }
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    private Intent createUberIntent(double lat, double lon, Double startLat, Double startLon) {
        String uri;
        if (startLat != null && startLon != null) {
            uri = String.format(
                Locale.US,
                "uber://?action=setPickup&pickup[latitude]=%f&pickup[longitude]=%f&dropoff[latitude]=%f&dropoff[longitude]=%f",
                startLat,
                startLon,
                lat,
                lon
            );
        } else {
            uri = String.format(
                Locale.US,
                "uber://?action=setPickup&pickup=my_location&dropoff[latitude]=%f&dropoff[longitude]=%f",
                lat,
                lon
            );
        }
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    private Intent createYandexIntent(double lat, double lon, Double startLat, Double startLon) {
        String uri;
        if (startLat != null && startLon != null) {
            uri = String.format(
                Locale.US,
                "yandexnavi://build_route_on_map?lat_from=%f&lon_from=%f&lat_to=%f&lon_to=%f",
                startLat,
                startLon,
                lat,
                lon
            );
        } else {
            uri = String.format(Locale.US, "yandexnavi://build_route_on_map?lat_to=%f&lon_to=%f", lat, lon);
        }
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    private Intent createSygicIntent(double lat, double lon) {
        String uri = String.format(Locale.US, "com.sygic.aura://coordinate|%f|%f|drive", lon, lat);
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    private Intent createHereIntent(double lat, double lon, Double startLat, Double startLon) {
        String uri;
        if (startLat != null && startLon != null) {
            uri = String.format(Locale.US, "here.directions://v1.0/mylocation/%f,%f?m=w", lat, lon);
        } else {
            uri = String.format(Locale.US, "here.directions://v1.0/mylocation/%f,%f?m=w", lat, lon);
        }
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    private Intent createMoovitIntent(double lat, double lon, Double startLat, Double startLon) {
        String uri = String.format(Locale.US, "moovit://directions?dest_lat=%f&dest_lon=%f", lat, lon);
        if (startLat != null && startLon != null) {
            uri += String.format(Locale.US, "&orig_lat=%f&orig_lon=%f", startLat, startLon);
        }
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    private Intent createLyftIntent(double lat, double lon) {
        String uri = String.format(Locale.US, "lyft://ridetype?id=lyft&destination[latitude]=%f&destination[longitude]=%f", lat, lon);
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    private Intent createMapsMeIntent(double lat, double lon) {
        String uri = String.format(Locale.US, "mapsme://map?ll=%f,%f", lat, lon);
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    private Intent createTomTomIntent(double lat, double lon) {
        String uri = String.format(Locale.US, "tomtomgo://x-callback-url/navigate?destination=%f,%f", lat, lon);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.tomtom.gplay.navapp");
        return intent;
    }

    private Intent createGuruMapsIntent(double lat, double lon, Double startLat, Double startLon, String transportMode) {
        String uri = String.format(
            Locale.US,
            "guru://nav?finish=%f,%f&mode=%s&start_navigation=true",
            lat,
            lon,
            getGuruMapsMode(transportMode)
        );
        if (startLat != null && startLon != null) {
            uri += String.format(Locale.US, "&start=%f,%f", startLat, startLon);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        String packageName = getFirstInstalledPackage("guru_maps");
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        return intent;
    }

    private String getGuruMapsMode(String transportMode) {
        switch (transportMode) {
            case "walking":
                return "pedestrian";
            case "bicycling":
                return "bicycle";
            case "driving":
            case "transit":
            default:
                return "auto";
        }
    }

    private Intent createOrganicMapsIntent(
        double lat,
        double lon,
        Double startLat,
        Double startLon,
        String destinationName,
        String transportMode
    ) {
        String origin = "currentLocation";
        if (startLat != null && startLon != null) {
            origin = String.format(Locale.US, "%f,%f", startLat, startLon);
        }

        String uri = String.format(
            Locale.US,
            "om://v2/nav?origin=%s&destination=%f,%f&mode=%s",
            Uri.encode(origin),
            lat,
            lon,
            getOrganicMapsMode(transportMode)
        );
        if (destinationName != null && !destinationName.isEmpty()) {
            uri += "&destination_name=" + Uri.encode(destinationName);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("app.organicmaps");
        return intent;
    }

    private String getOrganicMapsMode(String transportMode) {
        switch (transportMode) {
            case "walking":
                return "pedestrian";
            case "bicycling":
                return "bicycle";
            case "transit":
                return "transit";
            case "driving":
            default:
                return "drive";
        }
    }

    private Intent createYandexMapsIntent(double lat, double lon, Double startLat, Double startLon) {
        String uri = String.format(Locale.US, "yandexmaps://build_route_on_map/?lat_to=%f&lon_to=%f", lat, lon);
        if (startLat != null && startLon != null) {
            uri += String.format(Locale.US, "&lat_from=%f&lon_from=%f", startLat, startLon);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("ru.yandex.yandexmaps");
        return intent;
    }

    private Intent createMapyIntent(double lat, double lon, String transportMode) {
        String uri = String.format(Locale.US, "google.navigation:q=%f,%f&mode=%s", lat, lon, getGoogleMapsMode(transportMode));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("cz.seznam.mapy");
        return intent;
    }

    private Intent create2GisIntent(double lat, double lon, Double startLat, Double startLon, String transportMode) {
        String uri = String.format(Locale.US, "dgis://2gis.ru/routeSearch/rsType/%s", get2GisMode(transportMode));
        if (startLat != null && startLon != null) {
            uri += String.format(Locale.US, "/from/%f,%f", startLon, startLat);
        }
        uri += String.format(Locale.US, "/to/%f,%f", lon, lat);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("ru.dublgis.dgismobile");
        return intent;
    }

    private String get2GisMode(String transportMode) {
        switch (transportMode) {
            case "walking":
                return "pedestrian";
            case "transit":
                return "ctx";
            case "driving":
            case "bicycling":
            default:
                return "car";
        }
    }

    private Intent createCabifyIntent(double lat, double lon, Double startLat, Double startLon) {
        String uri;
        if (startLat != null && startLon != null) {
            uri = String.format(
                Locale.US,
                "cabify://ride?pickup[latitude]=%f&pickup[longitude]=%f&dropoff[latitude]=%f&dropoff[longitude]=%f",
                startLat,
                startLon,
                lat,
                lon
            );
        } else {
            uri = String.format(Locale.US, "cabify://rideto?lat=%f&lng=%f", lat, lon);
        }
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    private Intent createBaiduIntent(double lat, double lon, Double startLat, Double startLon) {
        String uri = String.format(Locale.US, "baidumap://map/direction?destination=%f,%f&mode=driving", lat, lon);
        if (startLat != null && startLon != null) {
            uri += String.format(Locale.US, "&origin=%f,%f", startLat, startLon);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.baidu.BaiduMap");
        return intent;
    }

    private Intent createGaodeIntent(double lat, double lon, Double startLat, Double startLon) {
        String uri = String.format(Locale.US, "amapuri://route/plan/?dlat=%f&dlon=%f&dev=0&t=0", lat, lon);
        if (startLat != null && startLon != null) {
            uri += String.format(Locale.US, "&slat=%f&slon=%f", startLat, startLon);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.autonavi.minimap");
        return intent;
    }

    private Intent createTeslaIntent(double lat, double lon, String destinationName) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, createTeslaShareText(lat, lon, destinationName));
        intent.setPackage("com.teslamotors.tesla");
        return intent;
    }

    static String createTeslaShareText(double lat, double lon, String destinationName) {
        return createTeslaShareLabel(destinationName) + "\n\n" + createGoogleMapsPositionUrl(lat, lon);
    }

    static String createGoogleMapsPositionUrl(double lat, double lon) {
        return String.format(Locale.US, "https://maps.google.com/?q=%f,%f", lat, lon);
    }

    private static String createTeslaShareLabel(String destinationName) {
        if (destinationName == null) {
            return "Dropped pin";
        }

        String normalizedName = destinationName.trim().replaceAll("[\\r\\n]+", " ");
        return normalizedName.isEmpty() ? "Dropped pin" : normalizedName;
    }

    public boolean isAppAvailable(String app) {
        Intent intent = createNavigationIntent(app, 0, 0, null, null, null, "driving");
        return intent != null && canResolveIntent(intent);
    }

    private boolean canResolveIntent(Intent intent) {
        PackageManager pm = context.getPackageManager();
        return intent.resolveActivity(pm) != null || !pm.queryIntentActivities(intent, 0).isEmpty();
    }

    private String getFirstInstalledPackage(String app) {
        AppInfo appInfo = navigationApps.get(app);
        if (appInfo == null) {
            return null;
        }

        PackageManager pm = context.getPackageManager();
        for (String packageName : appInfo.packageNames) {
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                return packageName;
            } catch (PackageManager.NameNotFoundException e) {
                // Try the next package for apps that ship free and pro variants.
            }
        }
        return null;
    }

    public JSObject getAppIcons(JSObject options) {
        return getAppIcons(options, false);
    }

    public JSObject refreshAppIcons(JSObject options) {
        return getAppIcons(options, true);
    }

    private JSObject getAppIcons(JSObject options, boolean forceRefreshOverride) {
        JSObject safeOptions = options == null ? new JSObject() : options;
        long maxAgeMs = getMaxAgeMs(safeOptions);
        boolean forceRefresh = forceRefreshOverride || safeOptions.optBoolean("forceRefresh", false);
        JSArray icons = new JSArray();
        JSArray failures = new JSArray();

        for (IconProvider provider : resolveIconProviders(safeOptions)) {
            try {
                icons.put(resolveProviderIcon(provider, maxAgeMs, forceRefresh));
            } catch (Exception e) {
                failures.put(createIconFailure(provider, e));
            }
        }

        JSObject ret = new JSObject();
        ret.put("icons", icons);
        ret.put("failures", failures);
        return ret;
    }

    public JSObject clearIconCache(JSObject options) {
        JSObject safeOptions = options == null ? new JSObject() : options;
        JSONArray apps = safeOptions.optJSONArray("apps");
        int cleared = 0;

        if (apps != null && apps.length() > 0) {
            for (int i = 0; i < apps.length(); i++) {
                String app = apps.optString(i, "");
                if (!app.isEmpty()) {
                    cleared += clearIconCacheForApp(app);
                }
            }
        } else {
            cleared = clearAllIconCache();
        }

        JSObject ret = new JSObject();
        ret.put("cleared", cleared);
        return ret;
    }

    private JSObject resolveProviderIcon(IconProvider provider, long maxAgeMs, boolean forceRefresh) throws Exception {
        iconCacheDirectoryLock.readLock().lock();
        try {
            synchronized (iconCacheLock(provider.app)) {
                return resolveProviderIconLocked(provider, maxAgeMs, forceRefresh);
            }
        } finally {
            iconCacheDirectoryLock.readLock().unlock();
        }
    }

    private JSObject resolveProviderIconLocked(IconProvider provider, long maxAgeMs, boolean forceRefresh) throws Exception {
        CachedIcon cachedIcon = readCachedIcon(provider.app);
        long now = System.currentTimeMillis();

        if (cachedIcon != null && !forceRefresh && now - cachedIcon.metadata.optLong("fetchedAt", 0) < maxAgeMs) {
            return createIconObject(cachedIcon, true, false);
        }

        try {
            DownloadedIcon downloadedIcon = downloadIcon(provider);
            File iconFile = writeIcon(provider.app, downloadedIcon);
            JSONObject metadata = new JSONObject();
            metadata.put("app", provider.app);
            metadata.put("name", provider.name);
            metadata.put("sourceUrl", downloadedIcon.sourceUrl);
            metadata.put("mimeType", downloadedIcon.mimeType);
            metadata.put("fetchedAt", now);
            metadata.put("fileName", iconFile.getName());
            writeString(metadataFile(provider.app), metadata.toString());
            deleteCachedFilesExcept(provider.app, iconFile.getName(), metadataFile(provider.app).getName());
            return createIconObject(new CachedIcon(iconFile, metadata), false, false);
        } catch (Exception e) {
            if (cachedIcon != null) {
                return createIconObject(cachedIcon, true, true);
            }
            throw e;
        }
    }

    private int clearIconCacheForApp(String app) {
        iconCacheDirectoryLock.readLock().lock();
        try {
            synchronized (iconCacheLock(app)) {
                return deleteCachedFiles(app);
            }
        } finally {
            iconCacheDirectoryLock.readLock().unlock();
        }
    }

    private int clearAllIconCache() {
        iconCacheDirectoryLock.writeLock().lock();
        try {
            File cacheDirectory = ensureIconCacheDirectory();
            File[] files = cacheDirectory.listFiles();
            int cleared = 0;
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.delete()) {
                        cleared++;
                    }
                }
            }
            iconCacheLocks.clear();
            return cleared;
        } finally {
            iconCacheDirectoryLock.writeLock().unlock();
        }
    }

    private JSObject createIconObject(CachedIcon cachedIcon, boolean fromCache, boolean stale) {
        JSObject icon = new JSObject();
        icon.put("app", cachedIcon.metadata.optString("app"));
        String name = cachedIcon.metadata.optString("name", null);
        if (name != null && !name.isEmpty()) {
            icon.put("name", name);
        }
        icon.put("localUrl", portablePath(cachedIcon.file));
        icon.put("sourceUrl", cachedIcon.metadata.optString("sourceUrl"));
        String mimeType = cachedIcon.metadata.optString("mimeType", null);
        if (mimeType != null && !mimeType.isEmpty()) {
            icon.put("mimeType", mimeType);
        }
        icon.put("fetchedAt", cachedIcon.metadata.optLong("fetchedAt", 0));
        icon.put("fromCache", fromCache);
        icon.put("stale", stale);
        return icon;
    }

    private JSObject createIconFailure(IconProvider provider, Exception error) {
        JSObject failure = new JSObject();
        failure.put("app", provider.app);
        if (provider.name != null && !provider.name.isEmpty()) {
            failure.put("name", provider.name);
        }
        String sourceUrl = provider.iconUrl != null ? provider.iconUrl : provider.url;
        if (sourceUrl != null && !sourceUrl.isEmpty()) {
            failure.put("sourceUrl", sourceUrl);
        }
        failure.put("message", error.getMessage() == null ? error.toString() : error.getMessage());
        return failure;
    }

    private DownloadedIcon downloadIcon(IconProvider provider) throws IOException {
        String sourceUrl;
        if (provider.iconUrl != null && !provider.iconUrl.isEmpty()) {
            sourceUrl = resolveUrl(provider.iconUrl, provider.url);
        } else {
            if (provider.url == null || provider.url.isEmpty()) {
                throw new IOException("Provider url or iconUrl is required");
            }
            sourceUrl = discoverIconUrl(provider.url);
        }

        HttpURLConnection connection = openConnection(sourceUrl);
        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("Icon request failed with status " + status);
            }

            String mimeType = normalizeMimeType(connection.getContentType());
            byte[] data = readLimitedBytes(connection.getInputStream(), MAX_ICON_BYTES);
            if (!isSupportedImageResponse(mimeType, sourceUrl)) {
                throw new IOException("Icon response is not an image");
            }
            return new DownloadedIcon(data, connection.getURL().toString(), mimeType);
        } finally {
            connection.disconnect();
        }
    }

    private String discoverIconUrl(String providerUrl) throws IOException {
        HttpURLConnection connection = openConnection(providerUrl);
        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("Provider page request failed with status " + status);
            }

            String html = new String(readLimitedBytes(connection.getInputStream(), MAX_HTML_BYTES), StandardCharsets.UTF_8);
            Matcher linkMatcher = ICON_LINK_PATTERN.matcher(html);
            if (linkMatcher.find()) {
                Matcher hrefMatcher = HREF_PATTERN.matcher(linkMatcher.group());
                if (hrefMatcher.find()) {
                    return resolveUrl(hrefMatcher.group(1), connection.getURL().toString());
                }
            }

            return resolveUrl("/favicon.ico", connection.getURL().toString());
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "CapgoLaunchNavigator/8");
        return connection;
    }

    private String resolveUrl(String url, String baseUrl) throws IOException {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return new URL(new URL(baseUrl), url).toString();
        }
        return new URL(url).toString();
    }

    private byte[] readLimitedBytes(InputStream inputStream, int maxBytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;

        while ((read = inputStream.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new IOException("Response is too large");
            }
            outputStream.write(buffer, 0, read);
        }

        return outputStream.toByteArray();
    }

    private boolean isSupportedImageResponse(String mimeType, String sourceUrl) {
        String path = pathForExtension(sourceUrl);
        if (mimeType == null || mimeType.isEmpty()) {
            return hasKnownImageExtension(path);
        }
        return mimeType.startsWith("image/") || (mimeType.equals("application/octet-stream") && hasKnownImageExtension(path));
    }

    private boolean hasKnownImageExtension(String path) {
        String lowerPath = path.toLowerCase(Locale.US);
        return (
            lowerPath.endsWith(".png") ||
            lowerPath.endsWith(".jpg") ||
            lowerPath.endsWith(".jpeg") ||
            lowerPath.endsWith(".gif") ||
            lowerPath.endsWith(".webp") ||
            lowerPath.endsWith(".svg") ||
            lowerPath.endsWith(".ico")
        );
    }

    private File writeIcon(String app, DownloadedIcon downloadedIcon) throws IOException {
        File cacheDirectory = ensureIconCacheDirectory();
        File iconFile = new File(cacheDirectory, cacheKey(app) + guessExtension(downloadedIcon.mimeType, downloadedIcon.sourceUrl));
        File tempFile = new File(cacheDirectory, iconFile.getName() + ".tmp");
        File backupFile = new File(cacheDirectory, iconFile.getName() + ".bak");
        if (tempFile.exists()) {
            tempFile.delete();
        }
        if (backupFile.exists()) {
            backupFile.delete();
        }
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            outputStream.write(downloadedIcon.data);
        }
        if (iconFile.exists() && !iconFile.renameTo(backupFile)) {
            tempFile.delete();
            throw new IOException("Could not replace cached icon");
        }
        if (!tempFile.renameTo(iconFile)) {
            tempFile.delete();
            if (backupFile.exists()) {
                backupFile.renameTo(iconFile);
            }
            throw new IOException("Could not store cached icon");
        }
        if (backupFile.exists()) {
            backupFile.delete();
        }
        return iconFile;
    }

    private CachedIcon readCachedIcon(String app) {
        File metadata = metadataFile(app);
        if (!metadata.exists()) {
            return null;
        }

        try {
            JSONObject metadataObject = new JSONObject(readString(metadata));
            String fileName = metadataObject.optString("fileName", "");
            if (fileName.isEmpty()) {
                return null;
            }
            File iconFile = new File(ensureIconCacheDirectory(), fileName);
            if (!iconFile.exists()) {
                return null;
            }
            return new CachedIcon(iconFile, metadataObject);
        } catch (Exception e) {
            return null;
        }
    }

    private String readString(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return new String(readLimitedBytes(inputStream, MAX_HTML_BYTES), StandardCharsets.UTF_8);
        }
    }

    private void writeString(File file, String value) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(value.getBytes(StandardCharsets.UTF_8));
        }
    }

    private File ensureIconCacheDirectory() {
        File directory = new File(context.getCacheDir(), ICON_CACHE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private File metadataFile(String app) {
        return new File(ensureIconCacheDirectory(), cacheKey(app) + ".json");
    }

    private Object iconCacheLock(String app) {
        return iconCacheLocks.computeIfAbsent(cacheKey(app), (key) -> new Object());
    }

    private int deleteCachedFiles(String app) {
        File cacheDirectory = ensureIconCacheDirectory();
        String prefix = cacheKey(app) + ".";
        int deleted = 0;
        File[] files = cacheDirectory.listFiles();
        if (files == null) {
            return 0;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().startsWith(prefix) && file.delete()) {
                deleted++;
            }
        }
        return deleted;
    }

    private int deleteCachedFilesExcept(String app, String iconFileName, String metadataFileName) {
        File cacheDirectory = ensureIconCacheDirectory();
        String prefix = cacheKey(app) + ".";
        int deleted = 0;
        File[] files = cacheDirectory.listFiles();
        if (files == null) {
            return 0;
        }

        for (File file : files) {
            String fileName = file.getName();
            if (
                file.isFile() &&
                fileName.startsWith(prefix) &&
                !fileName.equals(iconFileName) &&
                !fileName.equals(metadataFileName) &&
                file.delete()
            ) {
                deleted++;
            }
        }
        return deleted;
    }

    private String portablePath(File file) {
        String host = bridge == null ? null : bridge.getLocalUrl();
        if (host == null || host.isEmpty()) {
            return Uri.fromFile(file).toString();
        }
        return FileUtils.getPortablePath(context, host, Uri.fromFile(file));
    }

    private long getMaxAgeMs(JSObject options) {
        if (!options.has("maxAgeMs")) {
            return DEFAULT_ICON_CACHE_MAX_AGE_MS;
        }

        double maxAgeMs = options.optDouble("maxAgeMs", DEFAULT_ICON_CACHE_MAX_AGE_MS);
        if (Double.isNaN(maxAgeMs) || maxAgeMs < 0) {
            return DEFAULT_ICON_CACHE_MAX_AGE_MS;
        }
        return (long) maxAgeMs;
    }

    private IconProvider[] resolveIconProviders(JSObject options) {
        Map<String, IconProvider> providers = new LinkedHashMap<>();

        for (Map.Entry<String, AppInfo> entry : navigationApps.entrySet()) {
            providers.put(entry.getKey(), new IconProvider(entry.getKey(), entry.getValue().name, entry.getValue().url, null));
        }

        JSONArray customProviders = options.optJSONArray("providers");
        if (customProviders != null) {
            for (int i = 0; i < customProviders.length(); i++) {
                JSONObject providerObject = customProviders.optJSONObject(i);
                if (providerObject == null) {
                    continue;
                }

                String app = providerObject.optString("app", "");
                if (app.isEmpty()) {
                    continue;
                }

                IconProvider existing = providers.get(app);
                String name = providerObject.optString("name", existing == null ? null : existing.name);
                String url = providerObject.optString("url", existing == null ? null : existing.url);
                String iconUrl = providerObject.optString("iconUrl", existing == null ? null : existing.iconUrl);
                providers.put(app, new IconProvider(app, name, url, iconUrl));
            }
        }

        JSONArray apps = options.optJSONArray("apps");
        if (apps != null && apps.length() > 0) {
            IconProvider[] selected = new IconProvider[apps.length()];
            for (int i = 0; i < apps.length(); i++) {
                String app = apps.optString(i, "");
                IconProvider provider = providers.get(app);
                selected[i] = provider == null ? new IconProvider(app, null, null, null) : provider;
            }
            return selected;
        }

        return providers.values().toArray(new IconProvider[0]);
    }

    private String normalizeMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        return mimeType.split(";")[0].trim().toLowerCase(Locale.US);
    }

    private String guessExtension(String mimeType, String sourceUrl) {
        if ("image/jpeg".equals(mimeType)) {
            return ".jpg";
        } else if ("image/png".equals(mimeType)) {
            return ".png";
        } else if ("image/gif".equals(mimeType)) {
            return ".gif";
        } else if ("image/webp".equals(mimeType)) {
            return ".webp";
        } else if ("image/svg+xml".equals(mimeType)) {
            return ".svg";
        } else if ("image/x-icon".equals(mimeType) || "image/vnd.microsoft.icon".equals(mimeType)) {
            return ".ico";
        }

        String lowerPath = pathForExtension(sourceUrl).toLowerCase(Locale.US);
        if (lowerPath.endsWith(".png")) {
            return ".png";
        } else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return ".jpg";
        } else if (lowerPath.endsWith(".gif")) {
            return ".gif";
        } else if (lowerPath.endsWith(".webp")) {
            return ".webp";
        } else if (lowerPath.endsWith(".svg")) {
            return ".svg";
        } else if (lowerPath.endsWith(".ico")) {
            return ".ico";
        }

        return ".img";
    }

    private String pathForExtension(String sourceUrl) {
        try {
            return new URL(sourceUrl).getPath();
        } catch (Exception e) {
            int queryIndex = sourceUrl.indexOf('?');
            int fragmentIndex = sourceUrl.indexOf('#');
            int endIndex = sourceUrl.length();
            if (queryIndex >= 0) {
                endIndex = Math.min(endIndex, queryIndex);
            }
            if (fragmentIndex >= 0) {
                endIndex = Math.min(endIndex, fragmentIndex);
            }
            return sourceUrl.substring(0, endIndex);
        }
    }

    private String cacheKey(String app) {
        String safeApp = app.replaceAll("[^A-Za-z0-9._-]", "_");
        String hash = hashed(app);
        return safeApp + "_" + hash.substring(0, Math.min(8, hash.length()));
    }

    private String hashed(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) {
                sb.append(String.format(Locale.US, "%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    public JSArray getAvailableApps() {
        JSArray apps = new JSArray();

        for (Map.Entry<String, AppInfo> entry : navigationApps.entrySet()) {
            JSObject appObject = new JSObject();
            appObject.put("app", entry.getKey());
            appObject.put("name", entry.getValue().name);
            appObject.put("available", isAppAvailable(entry.getKey()));
            apps.put(appObject);
        }

        return apps;
    }

    public JSArray getSupportedApps() {
        JSArray apps = new JSArray();

        for (String appKey : navigationApps.keySet()) {
            apps.put(appKey);
        }

        return apps;
    }
}
