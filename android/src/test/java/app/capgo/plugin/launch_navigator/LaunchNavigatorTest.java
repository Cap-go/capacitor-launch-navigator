package app.capgo.plugin.launch_navigator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LaunchNavigatorTest {

    @Test
    public void createOsmAndUriUsesNavigationScheme() {
        String uri = LaunchNavigator.createOsmAndUri(47.620500, -122.349300);

        assertEquals("osmand.api://navigate?lat=47.620500&lon=-122.349300", uri);
    }

    @Test
    public void createKomootPlanUrlUsesPlanEndpoint() {
        String url = LaunchNavigator.createKomootPlanUrl(47.620500, -122.349300);

        assertEquals("https://www.komoot.com/plan/@47.620500,-122.349300", url);
    }

    @Test
    public void createLocusMapUriUsesNavigationScheme() {
        String uri = LaunchNavigator.createLocusMapUri(47.620500, -122.349300);

        assertEquals("locus://navigation?lat=47.620500&lon=-122.349300", uri);
    }

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
