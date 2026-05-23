package app.capgo.plugin.launch_navigator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LaunchNavigatorTest {

    @Test
    public void createTeslaShareTextUsesGoogleMapsPosition() {
        String shareText = LaunchNavigator.createTeslaShareText(47.620500, -122.349300, "Space Needle");

        assertEquals("Space Needle\n\nhttps://maps.google.com/?q=47.620500,-122.349300", shareText);
    }

    @Test
    public void createTeslaShareTextFallsBackToDroppedPinLabel() {
        String shareText = LaunchNavigator.createTeslaShareText(47.620500, -122.349300, "  \n  ");

        assertEquals("Dropped pin\n\nhttps://maps.google.com/?q=47.620500,-122.349300", shareText);
    }
}
