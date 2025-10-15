/**
 * Available navigation apps for iOS
 */
export enum IOSNavigationApp {
  APPLE_MAPS = 'apple_maps',
  GOOGLE_MAPS = 'google_maps',
  WAZE = 'waze',
  CITYMAPPER = 'citymapper',
  GARMIN_NAVIGON = 'garmin_navigon',
  TRANSIT_APP = 'transit_app',
  YANDEX_NAVIGATOR = 'yandex',
  UBER = 'uber',
  TOMTOM = 'tomtom',
  SYGIC = 'sygic',
  HERE_MAPS = 'here',
  MOOVIT = 'moovit',
  LYFT = 'lyft',
  MAPS_ME = 'mapsme',
  CABIFY = 'cabify',
  BAIDU = 'baidu',
  GAODE = 'gaode',
  TAXI_99 = '99taxi',
}

/**
 * Available navigation apps for Android
 */
export enum AndroidNavigationApp {
  GOOGLE_MAPS = 'google_maps',
  WAZE = 'waze',
  CITYMAPPER = 'citymapper',
  UBER = 'uber',
  YANDEX = 'yandex',
  SYGIC = 'sygic',
  HERE_MAPS = 'here',
  MOOVIT = 'moovit',
  LYFT = 'lyft',
  MAPS_ME = 'mapsme',
  CABIFY = 'cabify',
  BAIDU = 'baidu',
  GAODE = 'gaode',
}

/**
 * Transport modes
 */
export enum TransportMode {
  DRIVING = 'driving',
  WALKING = 'walking',
  BICYCLING = 'bicycling',
  TRANSIT = 'transit',
}

/**
 * Launch modes
 */
export enum LaunchMode {
  MAPS = 'maps',
  TURN_BY_TURN = 'turn_by_turn',
  GEO = 'geo',
}

/**
 * Options for navigation
 */
export interface NavigateOptions {
  /**
   * Starting location coordinates [latitude, longitude]
   */
  start?: [number, number];

  /**
   * Starting location name
   */
  startName?: string;

  /**
   * Destination name (will be ignored since we only support coordinates)
   */
  destinationName?: string;

  /**
   * Transport mode
   */
  transportMode?: TransportMode;

  /**
   * Specific app to launch (if not specified, will use default or prompt)
   */
  app?: IOSNavigationApp | AndroidNavigationApp | string;

  /**
   * Launch mode
   */
  launchMode?: LaunchMode;

  /**
   * Additional parameters specific to certain apps
   */
  extras?: Record<string, any>;

  /**
   * Enable debug logging
   */
  enableDebug?: boolean;
}

/**
 * Result of checking app availability
 */
export interface AvailableApp {
  /**
   * App identifier
   */
  app: string;

  /**
   * Display name of the app
   */
  name: string;

  /**
   * Whether the app is available on the device
   */
  available: boolean;
}

/**
 * Main plugin interface
 */
export interface LaunchNavigatorPlugin {
  /**
   * Navigate to a location using latitude and longitude
   * @param options Navigation options with destination coordinates
   */
  navigate(options: {
    /**
     * Destination coordinates [latitude, longitude]
     */
    destination: [number, number];

    /**
     * Optional navigation options
     */
    options?: NavigateOptions;
  }): Promise<void>;

  /**
   * Check if a specific navigation app is available
   * @param options Options containing app identifier
   */
  isAppAvailable(options: {
    /**
     * App identifier to check
     */
    app: IOSNavigationApp | AndroidNavigationApp | string;
  }): Promise<{
    /**
     * Whether the app is available
     */
    available: boolean;
  }>;

  /**
   * Get list of available navigation apps on the device
   */
  getAvailableApps(): Promise<{
    /**
     * List of available apps
     */
    apps: AvailableApp[];
  }>;

  /**
   * Get list of supported apps for the current platform
   */
  getSupportedApps(): Promise<{
    /**
     * List of supported app identifiers
     */
    apps: string[];
  }>;

  /**
   * Get the name of the default app for navigation
   */
  getDefaultApp(): Promise<{
    /**
     * Default app identifier
     */
    app: string;
  }>;
}
