package app.capgo.plugin.launch_navigator;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "LaunchNavigator")
public class LaunchNavigatorPlugin extends Plugin {

    private final String pluginVersion = "7.1.9";

    private LaunchNavigator implementation;

    @Override
    public void load() {
        implementation = new LaunchNavigator(getContext());
    }

    @PluginMethod
    public void navigate(PluginCall call) {
        JSArray destination = call.getArray("destination");
        if (destination == null || destination.length() != 2) {
            call.reject("Destination coordinates [latitude, longitude] are required");
            return;
        }

        try {
            double lat = destination.getDouble(0);
            double lon = destination.getDouble(1);

            JSObject options = call.getObject("options", new JSObject());
            String app = options.getString("app", "google_maps");
            String transportMode = options.getString("transportMode", "driving");

            Double startLat = null;
            Double startLon = null;

            // Try to get start array as JSONArray from the options object
            try {
                org.json.JSONArray startArray = options.getJSONArray("start");
                if (startArray != null && startArray.length() == 2) {
                    startLat = startArray.getDouble(0);
                    startLon = startArray.getDouble(1);
                }
            } catch (Exception e) {
                // start is optional, ignore if not present or invalid
            }

            String startName = options.getString("startName");
            String destinationName = options.getString("destinationName");

            boolean result = implementation.navigate(app, lat, lon, startLat, startLon, startName, destinationName, transportMode);

            if (result) {
                call.resolve();
            } else {
                call.reject("Failed to launch navigation app");
            }
        } catch (Exception e) {
            call.reject("Error parsing navigation parameters: " + e.getMessage());
        }
    }

    @PluginMethod
    public void isAppAvailable(PluginCall call) {
        String app = call.getString("app");
        if (app == null) {
            call.reject("App identifier is required");
            return;
        }

        boolean available = implementation.isAppAvailable(app);

        JSObject ret = new JSObject();
        ret.put("available", available);
        call.resolve(ret);
    }

    @PluginMethod
    public void getAvailableApps(PluginCall call) {
        JSArray apps = implementation.getAvailableApps();

        JSObject ret = new JSObject();
        ret.put("apps", apps);
        call.resolve(ret);
    }

    @PluginMethod
    public void getSupportedApps(PluginCall call) {
        JSArray apps = implementation.getSupportedApps();

        JSObject ret = new JSObject();
        ret.put("apps", apps);
        call.resolve(ret);
    }

    @PluginMethod
    public void getDefaultApp(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("app", "google_maps");
        call.resolve(ret);
    }

    @PluginMethod
    public void getPluginVersion(final PluginCall call) {
        try {
            final JSObject ret = new JSObject();
            ret.put("version", this.pluginVersion);
            call.resolve(ret);
        } catch (final Exception e) {
            call.reject("Could not get plugin version", e);
        }
    }
}
