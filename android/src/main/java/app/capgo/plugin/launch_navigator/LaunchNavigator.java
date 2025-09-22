package app.capgo.plugin.launch_navigator;

import com.getcapacitor.Logger;

public class LaunchNavigator {

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}
