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
  GURU_MAPS = 'guru_maps',
  ORGANIC_MAPS = 'organic_maps',
  YANDEX_MAPS = 'yandex_maps',
  TWO_GIS = '2gis',
  CABIFY = 'cabify',
  BAIDU = 'baidu',
  GAODE = 'gaode',
  TESLA = 'tesla',
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
  TOMTOM = 'tomtom',
  GURU_MAPS = 'guru_maps',
  ORGANIC_MAPS = 'organic_maps',
  YANDEX_MAPS = 'yandex_maps',
  MAPY = 'mapy',
  TWO_GIS = '2gis',
  CABIFY = 'cabify',
  BAIDU = 'baidu',
  GAODE = 'gaode',
  TESLA = 'tesla',
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
 * Web source used to discover or download a provider icon.
 */
export interface IconProvider {
  /**
   * Navigation app identifier
   */
  app: IOSNavigationApp | AndroidNavigationApp | string;

  /**
   * Display name for the provider
   */
  name?: string;

  /**
   * Provider website used to discover favicon metadata
   */
  url?: string;

  /**
   * Direct image URL. When provided, the plugin downloads this URL instead of discovering a favicon from `url`.
   */
  iconUrl?: string;
}

/**
 * Options for fetching navigation provider icons.
 */
export interface GetAppIconsOptions {
  /**
   * App identifiers to fetch. Defaults to all built-in providers for the current platform.
   */
  apps?: (IOSNavigationApp | AndroidNavigationApp | string)[];

  /**
   * Provider definitions to fetch or override built-in provider websites.
   */
  providers?: IconProvider[];

  /**
   * Cache revalidation interval in milliseconds. Defaults to 24 hours.
   */
  maxAgeMs?: number;

  /**
   * Ignore the current cache and fetch icons again.
   */
  forceRefresh?: boolean;
}

/**
 * Cached icon for a navigation provider.
 */
export interface ProviderIcon {
  /**
   * Navigation app identifier
   */
  app: string;

  /**
   * Display name for the provider
   */
  name?: string;

  /**
   * URL that can be used directly in an image element inside the WebView.
   */
  localUrl: string;

  /**
   * Web URL used to download the cached image
   */
  sourceUrl: string;

  /**
   * MIME type reported for the cached image, when known
   */
  mimeType?: string;

  /**
   * Unix timestamp in milliseconds when the icon was last fetched
   */
  fetchedAt: number;

  /**
   * Whether the icon came from the local cache without a network refresh
   */
  fromCache: boolean;

  /**
   * Whether a stale cached icon was returned because refresh failed
   */
  stale: boolean;
}

/**
 * Icon fetch failure for a provider.
 */
export interface ProviderIconFailure {
  /**
   * Navigation app identifier
   */
  app: string;

  /**
   * Display name for the provider
   */
  name?: string;

  /**
   * Web URL that failed, when known
   */
  sourceUrl?: string;

  /**
   * Failure message
   */
  message: string;
}

/**
 * Result of fetching provider icons.
 */
export interface ProviderIconsResult {
  /**
   * Icons available from cache or freshly downloaded
   */
  icons: ProviderIcon[];

  /**
   * Providers that could not be fetched and had no cached fallback
   */
  failures: ProviderIconFailure[];
}

/**
 * Options for clearing cached provider icons.
 */
export interface ClearIconCacheOptions {
  /**
   * App identifiers to clear. Defaults to all cached icons.
   */
  apps?: (IOSNavigationApp | AndroidNavigationApp | string)[];
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

  /**
   * Fetch provider icons and cache them locally.
   *
   * The native implementations revalidate cached icons after 24 hours by default.
   * Pass `forceRefresh: true` to bypass the cache when an icon must be repaired.
   */
  getAppIcons(options?: GetAppIconsOptions): Promise<ProviderIconsResult>;

  /**
   * Refresh provider icons, ignoring the cache age.
   */
  refreshAppIcons(options?: GetAppIconsOptions): Promise<ProviderIconsResult>;

  /**
   * Clear cached provider icons.
   */
  clearIconCache(options?: ClearIconCacheOptions): Promise<{
    /**
     * Number of cached icon files removed
     */
    cleared: number;
  }>;

  /**
   * Get the native Capacitor plugin version
   *
   * @returns {Promise<{ id: string }>} an Promise with version for this device
   * @throws An error if the something went wrong
   */
  getPluginVersion(): Promise<{ version: string }>;
}
