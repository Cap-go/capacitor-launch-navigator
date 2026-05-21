import { WebPlugin } from '@capacitor/core';

import type {
  LaunchNavigatorPlugin,
  NavigateOptions,
  AvailableApp,
  GetAppIconsOptions,
  ProviderIcon,
  ProviderIconFailure,
  ProviderIconsResult,
  IconProvider,
  ClearIconCacheOptions,
} from './definitions';

const DEFAULT_ICON_CACHE_MAX_AGE_MS = 24 * 60 * 60 * 1000;
const ICON_CACHE_NAME = 'capgo-launch-navigator-icons';
const ICON_METADATA_PREFIX = 'capgo-launch-navigator-icon:';

const BUILT_IN_ICON_PROVIDERS: IconProvider[] = [
  { app: 'google_maps', name: 'Google Maps', url: 'https://www.google.com/maps' },
  { app: 'waze', name: 'Waze', url: 'https://www.waze.com' },
  { app: 'citymapper', name: 'Citymapper', url: 'https://citymapper.com' },
  { app: 'uber', name: 'Uber', url: 'https://www.uber.com' },
  { app: 'yandex', name: 'Yandex Navigator', url: 'https://yandex.com/maps' },
  { app: 'sygic', name: 'Sygic', url: 'https://www.sygic.com/gps-navigation' },
  { app: 'here', name: 'HERE Maps', url: 'https://wego.here.com' },
  { app: 'moovit', name: 'Moovit', url: 'https://moovitapp.com' },
  { app: 'lyft', name: 'Lyft', url: 'https://www.lyft.com' },
  { app: 'mapsme', name: 'MAPS.ME', url: 'https://maps.me' },
  { app: 'guru_maps', name: 'Guru Maps', url: 'https://gurumaps.app' },
  { app: 'organic_maps', name: 'Organic Maps', url: 'https://organicmaps.app' },
  { app: 'yandex_maps', name: 'Yandex Maps', url: 'https://yandex.com/maps' },
  { app: 'mapy', name: 'Mapy.com', url: 'https://mapy.com' },
  { app: '2gis', name: '2GIS', url: 'https://2gis.com' },
  { app: 'cabify', name: 'Cabify', url: 'https://cabify.com' },
  { app: 'baidu', name: 'Baidu Maps', url: 'https://map.baidu.com' },
  { app: 'gaode', name: 'Gaode Maps', url: 'https://www.amap.com' },
  { app: 'tesla', name: 'Tesla', url: 'https://www.tesla.com' },
  { app: 'apple_maps', name: 'Apple Maps', url: 'https://www.apple.com/maps/' },
  { app: 'tomtom', name: 'TomTom', url: 'https://www.tomtom.com' },
  { app: 'transit_app', name: 'Transit App', url: 'https://transitapp.com' },
  { app: 'garmin_navigon', name: 'Garmin Navigon', url: 'https://www.garmin.com' },
  { app: '99taxi', name: '99 Taxi', url: 'https://99app.com' },
];

interface StoredIconMetadata {
  app: string;
  name?: string;
  sourceUrl: string;
  mimeType?: string;
  fetchedAt: number;
}

interface CachedWebIcon {
  metadata: StoredIconMetadata;
  localUrl: string;
}

const objectUrls = new Map<string, string>();

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

  async getAppIcons(options: GetAppIconsOptions = {}): Promise<ProviderIconsResult> {
    const providers = resolveIconProviders(options);
    const maxAgeMs = normalizeMaxAge(options.maxAgeMs);
    const forceRefresh = options.forceRefresh === true;
    const icons: ProviderIcon[] = [];
    const failures: ProviderIconFailure[] = [];

    await Promise.all(
      providers.map(async (provider) => {
        try {
          icons.push(await this.getProviderIcon(provider, maxAgeMs, forceRefresh));
        } catch (error) {
          failures.push({
            app: String(provider.app),
            name: provider.name,
            sourceUrl: provider.iconUrl || provider.url,
            message: error instanceof Error ? error.message : String(error),
          });
        }
      }),
    );

    return { icons, failures };
  }

  async refreshAppIcons(options: GetAppIconsOptions = {}): Promise<ProviderIconsResult> {
    return this.getAppIcons({ ...options, forceRefresh: true });
  }

  async clearIconCache(options: ClearIconCacheOptions = {}): Promise<{ cleared: number }> {
    const appIds = options.apps?.map(String);
    const cache = await getCache();
    let cleared = 0;

    if (appIds && appIds.length > 0) {
      for (const app of appIds) {
        cleared += (await deleteCachedIcon(app, cache)) ? 1 : 0;
      }
      return { cleared };
    }

    const storedApps = getStoredAppIds();
    for (const app of storedApps) {
      cleared += (await deleteCachedIcon(app, cache)) ? 1 : 0;
    }

    return { cleared };
  }

  async getPluginVersion(): Promise<{ version: string }> {
    return { version: 'web' };
  }

  private async getProviderIcon(
    provider: IconProvider,
    maxAgeMs: number,
    forceRefresh: boolean,
  ): Promise<ProviderIcon> {
    const app = String(provider.app);
    const cached = await readCachedIcon(app);
    const now = Date.now();

    if (cached && !forceRefresh && now - cached.metadata.fetchedAt < maxAgeMs) {
      return toProviderIcon(cached, true, false);
    }

    try {
      const sourceUrl = await discoverIconUrl(provider);
      const response = await fetch(sourceUrl);
      if (!response.ok) {
        throw new Error(`Icon request failed with status ${response.status}`);
      }

      const blob = await response.blob();
      const mimeType = blob.type || response.headers.get('content-type') || undefined;
      const cache = await getCache();
      const metadata: StoredIconMetadata = {
        app,
        name: provider.name,
        sourceUrl,
        mimeType,
        fetchedAt: now,
      };
      const localUrl = URL.createObjectURL(blob);
      setObjectUrl(app, localUrl);

      if (cache) {
        await cache.put(
          cacheRequest(app),
          new Response(blob, { headers: mimeType ? { 'content-type': mimeType } : undefined }),
        );
      }
      writeMetadata(metadata);

      return {
        app,
        name: metadata.name,
        localUrl,
        sourceUrl,
        mimeType,
        fetchedAt: metadata.fetchedAt,
        fromCache: false,
        stale: false,
      };
    } catch (error) {
      if (cached) {
        return toProviderIcon(cached, true, true);
      }
      throw error;
    }
  }
}

function resolveIconProviders(options: GetAppIconsOptions): IconProvider[] {
  const builtIns = new Map(BUILT_IN_ICON_PROVIDERS.map((provider) => [String(provider.app), provider]));
  const customProviders = options.providers || [];

  for (const provider of customProviders) {
    const app = String(provider.app);
    builtIns.set(app, { ...builtIns.get(app), ...provider, app });
  }

  if (options.apps && options.apps.length > 0) {
    return options.apps.map((app) => builtIns.get(String(app)) || { app: String(app) });
  }

  if (customProviders.length > 0) {
    return customProviders.map((provider) => builtIns.get(String(provider.app)) || provider);
  }

  return [builtIns.get('google_maps') as IconProvider];
}

function normalizeMaxAge(maxAgeMs: number | undefined): number {
  if (typeof maxAgeMs !== 'number' || !Number.isFinite(maxAgeMs) || maxAgeMs < 0) {
    return DEFAULT_ICON_CACHE_MAX_AGE_MS;
  }
  return maxAgeMs;
}

async function discoverIconUrl(provider: IconProvider): Promise<string> {
  if (provider.iconUrl) {
    return absolutizeUrl(provider.iconUrl, provider.url);
  }

  if (!provider.url) {
    throw new Error('Provider url or iconUrl is required');
  }

  const response = await fetch(provider.url);
  if (!response.ok) {
    throw new Error(`Provider page request failed with status ${response.status}`);
  }

  const html = await response.text();
  const linkMatch = html.match(/<link\s+[^>]*rel=["'][^"']*(?:apple-touch-icon|icon)[^"']*["'][^>]*>/i);
  const href = linkMatch?.[0].match(/\shref=["']([^"']+)["']/i)?.[1];

  if (href) {
    return absolutizeUrl(href, provider.url);
  }

  return absolutizeUrl('/favicon.ico', provider.url);
}

function absolutizeUrl(url: string, baseUrl?: string): string {
  try {
    return new URL(url, baseUrl).toString();
  } catch {
    throw new Error(`Invalid icon URL: ${url}`);
  }
}

async function readCachedIcon(app: string): Promise<CachedWebIcon | undefined> {
  const metadata = readMetadata(app);
  const cachedObjectUrl = objectUrls.get(app);

  if (!metadata) {
    return undefined;
  }

  if (cachedObjectUrl) {
    return { metadata, localUrl: cachedObjectUrl };
  }

  const cache = await getCache();
  const response = await cache?.match(cacheRequest(app));
  if (!response) {
    return undefined;
  }

  const blob = await response.blob();
  const localUrl = URL.createObjectURL(blob);
  setObjectUrl(app, localUrl);
  return { metadata, localUrl };
}

function toProviderIcon(cached: CachedWebIcon, fromCache: boolean, stale: boolean): ProviderIcon {
  return {
    app: cached.metadata.app,
    name: cached.metadata.name,
    localUrl: cached.localUrl,
    sourceUrl: cached.metadata.sourceUrl,
    mimeType: cached.metadata.mimeType,
    fetchedAt: cached.metadata.fetchedAt,
    fromCache,
    stale,
  };
}

async function getCache(): Promise<Cache | undefined> {
  if (!('caches' in window)) {
    return undefined;
  }
  return caches.open(ICON_CACHE_NAME);
}

function cacheRequest(app: string): Request {
  return new Request(`https://capgo.local/launch-navigator-icons/${encodeURIComponent(app)}`);
}

function readMetadata(app: string): StoredIconMetadata | undefined {
  try {
    const raw = localStorage.getItem(metadataKey(app));
    return raw ? (JSON.parse(raw) as StoredIconMetadata) : undefined;
  } catch {
    return undefined;
  }
}

function writeMetadata(metadata: StoredIconMetadata): void {
  localStorage.setItem(metadataKey(metadata.app), JSON.stringify(metadata));
}

function getStoredAppIds(): string[] {
  const apps: string[] = [];
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (key?.startsWith(ICON_METADATA_PREFIX)) {
      apps.push(key.slice(ICON_METADATA_PREFIX.length));
    }
  }
  return apps;
}

async function deleteCachedIcon(app: string, cache: Cache | undefined): Promise<boolean> {
  const hadMetadata = localStorage.getItem(metadataKey(app)) !== null;
  localStorage.removeItem(metadataKey(app));
  const objectUrl = objectUrls.get(app);
  if (objectUrl) {
    URL.revokeObjectURL(objectUrl);
    objectUrls.delete(app);
  }
  const deletedFromCache = (await cache?.delete(cacheRequest(app))) || false;
  return hadMetadata || deletedFromCache;
}

function metadataKey(app: string): string {
  return `${ICON_METADATA_PREFIX}${app}`;
}

function setObjectUrl(app: string, url: string): void {
  const existing = objectUrls.get(app);
  if (existing) {
    URL.revokeObjectURL(existing);
  }
  objectUrls.set(app, url);
}
