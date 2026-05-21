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
  LaunchNavigatorPluginIcons,
} from './definitions';

const DEFAULT_ICON_CACHE_MAX_AGE_MS = 24 * 60 * 60 * 1000;
const ICON_CACHE_NAME = 'capgo-launch-navigator-icons';
const ICON_METADATA_PREFIX = 'capgo-launch-navigator-icon:';

const BUILT_IN_ICON_PROVIDERS: IconProvider[] = [
  iconProvider('google_maps', 'Google Maps', 'https://www.google.com/maps'),
  iconProvider('waze', 'Waze', 'https://www.waze.com'),
  iconProvider('citymapper', 'Citymapper', 'https://citymapper.com'),
  iconProvider('uber', 'Uber', 'https://www.uber.com'),
  iconProvider('yandex', 'Yandex Navigator', 'https://yandex.com/maps'),
  iconProvider('sygic', 'Sygic', 'https://www.sygic.com/gps-navigation'),
  iconProvider('here', 'HERE Maps', 'https://wego.here.com'),
  iconProvider('moovit', 'Moovit', 'https://moovitapp.com'),
  iconProvider('lyft', 'Lyft', 'https://www.lyft.com'),
  iconProvider('mapsme', 'MAPS.ME', 'https://maps.me'),
  iconProvider('guru_maps', 'Guru Maps', 'https://gurumaps.app'),
  iconProvider('organic_maps', 'Organic Maps', 'https://organicmaps.app'),
  iconProvider('yandex_maps', 'Yandex Maps', 'https://yandex.com/maps'),
  iconProvider('mapy', 'Mapy.com', 'https://mapy.com'),
  iconProvider('2gis', '2GIS', 'https://2gis.com'),
  iconProvider('cabify', 'Cabify', 'https://cabify.com'),
  iconProvider('baidu', 'Baidu Maps', 'https://map.baidu.com'),
  iconProvider('gaode', 'Gaode Maps', 'https://www.amap.com'),
  iconProvider('tesla', 'Tesla', 'https://www.tesla.com'),
  iconProvider('apple_maps', 'Apple Maps', 'https://www.apple.com/maps/'),
  iconProvider('tomtom', 'TomTom', 'https://www.tomtom.com'),
  iconProvider('transit_app', 'Transit App', 'https://transitapp.com'),
  iconProvider('garmin_navigon', 'Garmin Navigon', 'https://www.garmin.com'),
  iconProvider('99taxi', '99 Taxi', 'https://99app.com'),
];

function iconProvider(app: string, name: string, url: string): IconProvider {
  return {
    app,
    name,
    url,
    iconUrl: `https://favicone.com/${new URL(url).hostname}?s=128`,
  };
}

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

export class LaunchNavigatorWeb extends WebPlugin implements LaunchNavigatorPlugin, LaunchNavigatorPluginIcons {
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

    const storedApps = await getStoredAppIds(cache);
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
      const sourceUrl = provider.iconUrl
        ? absolutizeUrl(provider.iconUrl, provider.url)
        : await discoverIconUrl(provider);
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
        try {
          await cache.put(
            cacheRequest(app),
            new Response(blob, { headers: mimeType ? { 'content-type': mimeType } : undefined }),
          );
        } catch (error) {
          console.warn('Unable to persist provider icon in Cache Storage', error);
        }
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

  return Array.from(builtIns.values());
}

function normalizeMaxAge(maxAgeMs: number | undefined): number {
  if (typeof maxAgeMs !== 'number' || !Number.isFinite(maxAgeMs) || maxAgeMs < 0) {
    return DEFAULT_ICON_CACHE_MAX_AGE_MS;
  }
  return maxAgeMs;
}

async function discoverIconUrl(provider: IconProvider): Promise<string> {
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
  try {
    return await caches.open(ICON_CACHE_NAME);
  } catch (error) {
    console.warn('Unable to open provider icon cache', error);
    return undefined;
  }
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
  try {
    localStorage.setItem(metadataKey(metadata.app), JSON.stringify(metadata));
  } catch (error) {
    console.warn('Unable to persist provider icon metadata', error);
  }
}

async function getStoredAppIds(cache: Cache | undefined): Promise<string[]> {
  const apps = new Set<string>();
  try {
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key?.startsWith(ICON_METADATA_PREFIX)) {
        apps.add(key.slice(ICON_METADATA_PREFIX.length));
      }
    }
  } catch (error) {
    console.warn('Unable to read provider icon metadata', error);
  }

  const cachesToRead = new Map<string, Cache>();
  if (cache) {
    cachesToRead.set(ICON_CACHE_NAME, cache);
  }

  try {
    if ('caches' in window) {
      const cacheNames = await caches.keys();
      for (const cacheName of cacheNames) {
        if (cacheName === ICON_CACHE_NAME && !cachesToRead.has(cacheName)) {
          cachesToRead.set(cacheName, await caches.open(cacheName));
        }
      }
    }
  } catch (error) {
    console.warn('Unable to enumerate provider icon caches', error);
  }

  try {
    for (const iconCache of cachesToRead.values()) {
      const requests = await iconCache.keys();
      for (const request of requests) {
        const app = appFromCacheRequest(request);
        if (app) {
          apps.add(app);
        }
      }
    }
  } catch (error) {
    console.warn('Unable to read provider icon cache entries', error);
  }

  return Array.from(apps);
}

async function deleteCachedIcon(app: string, cache: Cache | undefined): Promise<boolean> {
  let hadMetadata = false;
  try {
    hadMetadata = localStorage.getItem(metadataKey(app)) !== null;
    localStorage.removeItem(metadataKey(app));
  } catch (error) {
    console.warn('Unable to clear provider icon metadata', error);
  }
  const objectUrl = objectUrls.get(app);
  if (objectUrl) {
    URL.revokeObjectURL(objectUrl);
    objectUrls.delete(app);
  }
  let deletedFromCache = false;
  try {
    deletedFromCache = (await cache?.delete(cacheRequest(app))) || false;
  } catch (error) {
    console.warn('Unable to clear provider icon cache entry', error);
  }
  return hadMetadata || deletedFromCache;
}

function metadataKey(app: string): string {
  return `${ICON_METADATA_PREFIX}${app}`;
}

function appFromCacheRequest(request: Request): string | undefined {
  try {
    const url = new URL(request.url);
    const prefix = '/launch-navigator-icons/';
    if (url.origin !== 'https://capgo.local' || !url.pathname.startsWith(prefix)) {
      return undefined;
    }

    const app = decodeURIComponent(url.pathname.slice(prefix.length));
    return app || undefined;
  } catch {
    return undefined;
  }
}

function setObjectUrl(app: string, url: string): void {
  const existing = objectUrls.get(app);
  if (existing) {
    URL.revokeObjectURL(existing);
  }
  objectUrls.set(app, url);
}
