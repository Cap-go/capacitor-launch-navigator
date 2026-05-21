package app.capgo.plugin.launch_navigator;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class LaunchNavigator {

    private Context context;
    private Map<String, AppInfo> navigationApps;

    private static class AppInfo {

        String name;
        String[] packageNames;

        AppInfo(String name, String... packageNames) {
            this.name = name;
            this.packageNames = packageNames;
        }
    }

    public LaunchNavigator(Context context) {
        this.context = context;
        initializeApps();
    }

    private void initializeApps() {
        navigationApps = new LinkedHashMap<>();
        navigationApps.put("google_maps", new AppInfo("Google Maps", "com.google.android.apps.maps"));
        navigationApps.put("waze", new AppInfo("Waze", "com.waze"));
        navigationApps.put("citymapper", new AppInfo("Citymapper", "com.citymapper.app.release"));
        navigationApps.put("uber", new AppInfo("Uber", "com.ubercab"));
        navigationApps.put("yandex", new AppInfo("Yandex Navigator", "ru.yandex.yandexnavi"));
        navigationApps.put("sygic", new AppInfo("Sygic", "com.sygic.aura"));
        navigationApps.put("here", new AppInfo("HERE Maps", "com.here.app.maps"));
        navigationApps.put("moovit", new AppInfo("Moovit", "com.tranzmate"));
        navigationApps.put("lyft", new AppInfo("Lyft", "me.lyft.android"));
        navigationApps.put("mapsme", new AppInfo("MAPS.ME", "com.mapswithme.maps.pro"));
        navigationApps.put("tomtom", new AppInfo("TomTom GO", "com.tomtom.gplay.navapp"));
        navigationApps.put("guru_maps", new AppInfo("Guru Maps", "com.bodunov.galileo", "com.bodunov.GalileoPro"));
        navigationApps.put("organic_maps", new AppInfo("Organic Maps", "app.organicmaps"));
        navigationApps.put("yandex_maps", new AppInfo("Yandex Maps", "ru.yandex.yandexmaps"));
        navigationApps.put("mapy", new AppInfo("Mapy.com", "cz.seznam.mapy"));
        navigationApps.put("2gis", new AppInfo("2GIS", "ru.dublgis.dgismobile"));
        navigationApps.put("cabify", new AppInfo("Cabify", "com.cabify.rider"));
        navigationApps.put("baidu", new AppInfo("Baidu Maps", "com.baidu.BaiduMap"));
        navigationApps.put("gaode", new AppInfo("Gaode Maps", "com.autonavi.minimap"));
        navigationApps.put("tesla", new AppInfo("Tesla", "com.teslamotors.tesla"));
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

            if (intent != null && canHandleIntent(intent)) {
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
                return createMapyIntent(lat, lon);
            case "2gis":
                return create2GisIntent(lat, lon, startLat, startLon, transportMode);
            case "cabify":
                return createCabifyIntent(lat, lon, startLat, startLon);
            case "baidu":
                return createBaiduIntent(lat, lon, startLat, startLon);
            case "gaode":
                return createGaodeIntent(lat, lon, startLat, startLon);
            case "tesla":
                return createTeslaIntent(lat, lon, startLat, startLon, transportMode);
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

    private Intent createMapyIntent(double lat, double lon) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, "geo:0,0?q=%f,%f", lat, lon)));
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

    private Intent createTeslaIntent(double lat, double lon, Double startLat, Double startLon, String transportMode) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, createGoogleMapsWebUrl(lat, lon, startLat, startLon, transportMode));
        intent.setPackage("com.teslamotors.tesla");
        return intent;
    }

    private String createGoogleMapsWebUrl(double lat, double lon, Double startLat, Double startLon, String transportMode) {
        String url = String.format(
            Locale.US,
            "https://www.google.com/maps/dir/?api=1&destination=%f,%f&travelmode=%s",
            lat,
            lon,
            getGoogleMapsTravelMode(transportMode)
        );
        if (startLat != null && startLon != null) {
            url += String.format(Locale.US, "&origin=%f,%f", startLat, startLon);
        }
        return url;
    }

    private String getGoogleMapsTravelMode(String transportMode) {
        switch (transportMode) {
            case "walking":
                return "walking";
            case "bicycling":
                return "bicycling";
            case "transit":
                return "transit";
            case "driving":
            default:
                return "driving";
        }
    }

    public boolean isAppAvailable(String app) {
        Intent intent = createNavigationIntent(app, 0, 0, null, null, null, "driving");
        return intent != null && canHandleIntent(intent);
    }

    private boolean canHandleIntent(Intent intent) {
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
                // Try the next package for apps that ship multiple variants.
            }
        }
        return null;
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
