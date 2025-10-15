package app.capgo.plugin.launch_navigator;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LaunchNavigator {

    private Context context;
    private Map<String, AppInfo> navigationApps;

    private static class AppInfo {

        String name;
        String packageName;

        AppInfo(String name, String packageName) {
            this.name = name;
            this.packageName = packageName;
        }
    }

    public LaunchNavigator(Context context) {
        this.context = context;
        initializeApps();
    }

    private void initializeApps() {
        navigationApps = new HashMap<>();
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
        navigationApps.put("cabify", new AppInfo("Cabify", "com.cabify.rider"));
        navigationApps.put("baidu", new AppInfo("Baidu Maps", "com.baidu.BaiduMap"));
        navigationApps.put("gaode", new AppInfo("Gaode Maps", "com.autonavi.minimap"));
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
            Intent intent;

            switch (app) {
                case "google_maps":
                    intent = createGoogleMapsIntent(lat, lon, startLat, startLon, transportMode);
                    break;
                case "waze":
                    intent = createWazeIntent(lat, lon);
                    break;
                case "citymapper":
                    intent = createCitymapperIntent(lat, lon, startLat, startLon);
                    break;
                case "uber":
                    intent = createUberIntent(lat, lon, startLat, startLon);
                    break;
                case "yandex":
                    intent = createYandexIntent(lat, lon, startLat, startLon);
                    break;
                case "sygic":
                    intent = createSygicIntent(lat, lon);
                    break;
                case "here":
                    intent = createHereIntent(lat, lon, startLat, startLon);
                    break;
                case "moovit":
                    intent = createMoovitIntent(lat, lon, startLat, startLon);
                    break;
                case "lyft":
                    intent = createLyftIntent(lat, lon);
                    break;
                case "mapsme":
                    intent = createMapsMeIntent(lat, lon);
                    break;
                case "cabify":
                    intent = createCabifyIntent(lat, lon, startLat, startLon);
                    break;
                case "baidu":
                    intent = createBaiduIntent(lat, lon, startLat, startLon);
                    break;
                case "gaode":
                    intent = createGaodeIntent(lat, lon, startLat, startLon);
                    break;
                default:
                    return false;
            }

            if (intent != null) {
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

    public boolean isAppAvailable(String app) {
        AppInfo appInfo = navigationApps.get(app);
        if (appInfo == null) {
            return false;
        }

        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(appInfo.packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
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
