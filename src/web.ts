import { WebPlugin } from '@capacitor/core';

import type { LaunchNavigatorPlugin, NavigateOptions, AvailableApp } from './definitions';

export class LaunchNavigatorWeb extends WebPlugin implements LaunchNavigatorPlugin {
  /**
   * Navigate to a location using latitude and longitude
   * Opens the location in the default map application or Google Maps web
   */
  async navigate(options: { destination: [number, number]; options?: NavigateOptions }): Promise<void> {
    const [lat, lon] = options.destination;
    const navOptions = options.options || {};

    let url: string;

    if (navOptions.start) {
      const [startLat, startLon] = navOptions.start;
      url = `https://www.google.com/maps/dir/${startLat},${startLon}/${lat},${lon}`;
    } else {
      url = `https://www.google.com/maps/search/?api=1&query=${lat},${lon}`;
    }

    if (navOptions.transportMode) {
      const modeMap: Record<string, string> = {
        driving: 'driving',
        walking: 'walking',
        bicycling: 'bicycling',
        transit: 'transit',
      };
      const mode = modeMap[navOptions.transportMode];
      if (mode && navOptions.start) {
        url += `&travelmode=${mode}`;
      }
    }

    window.open(url, '_blank');
  }

  /**
   * Check if a specific navigation app is available
   * Always returns false on web
   */
  async isAppAvailable(options: { app: string }): Promise<{ available: boolean }> {
    console.log('isAppAvailable called with app:', options.app);
    return { available: false };
  }

  /**
   * Get list of available navigation apps
   * Returns only Google Maps as available on web
   */
  async getAvailableApps(): Promise<{ apps: AvailableApp[] }> {
    return {
      apps: [
        {
          app: 'google_maps',
          name: 'Google Maps (Web)',
          available: true,
        },
      ],
    };
  }

  /**
   * Get list of supported apps for the current platform
   * Returns only Google Maps for web
   */
  async getSupportedApps(): Promise<{ apps: string[] }> {
    return {
      apps: ['google_maps'],
    };
  }

  /**
   * Get the default app for navigation
   * Returns Google Maps for web
   */
  async getDefaultApp(): Promise<{ app: string }> {
    return {
      app: 'google_maps',
    };
  }

  async getPluginVersion(): Promise<{ version: string }> {
    return { version: 'web' };
  }
}
