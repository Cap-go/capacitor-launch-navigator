# @capgo/capacitor-launch-navigator
<a href="https://capgo.app/"><img src="https://capgo.app/readme-banner.svg?repo=Cap-go/capacitor-launch-navigator" alt="Capgo - Instant updates for Capacitor" /></a>

<div align="center">
  <h2><a href="https://capgo.app/?ref=plugin_launch_navigator"> ➡️ Get Instant updates for your App with Capgo</a></h2>
  <h2><a href="https://capgo.app/consulting/?ref=plugin_launch_navigator"> Missing a feature? We’ll build the plugin for you 💪</a></h2>
</div>

Capacitor plugin for launching navigation apps to navigate to a destination.

This plugin is a Capacitor port of the popular phonegap-launch-navigator plugin, supporting navigation with latitude/longitude coordinates only (no address support - use [@capgo/capacitor-nativegeocoder](https://github.com/Cap-go/capacitor-nativegeocoder) for address geocoding).

## Documentation

The most complete doc is available here: https://capgo.app/docs/plugins/launch-navigator/

## Compatibility

| Plugin version | Capacitor compatibility | Maintained |
| -------------- | ----------------------- | ---------- |
| v8.\*.\*       | v8.\*.\*                | ✅          |
| v7.\*.\*       | v7.\*.\*                | On demand   |
| v6.\*.\*       | v6.\*.\*                | ❌          |
| v5.\*.\*       | v5.\*.\*                | ❌          |

> **Note:** The major version of this plugin follows the major version of Capacitor. Use the version that matches your Capacitor installation (e.g., plugin v8 for Capacitor 8). Only the latest major version is actively maintained.

## Install

You can use our AI-Assisted Setup to install the plugin. Add the Capgo skills to your AI tool using the following command:

```bash
npx skills add https://github.com/cap-go/capacitor-skills --skill capacitor-plugins
```

Then use the following prompt:

```text
Use the `capacitor-plugins` skill from `cap-go/capacitor-skills` to install the `@capgo/capacitor-launch-navigator` plugin in my project.
```

If you prefer Manual Setup, install the plugin by running the following commands and follow the platform-specific instructions below:

```bash
npm install @capgo/capacitor-launch-navigator
npx cap sync
```

## iOS Setup

Add URL schemes to your `Info.plist` to detect installed navigation apps:

```xml
<key>LSApplicationQueriesSchemes</key>
<array>
    <string>comgooglemaps</string>
    <string>waze</string>
    <string>citymapper</string>
    <string>navigon</string>
    <string>transit</string>
    <string>yandexnavi</string>
    <string>uber</string>
    <string>tomtomgo</string>
    <string>com.sygic.aura</string>
    <string>here-route</string>
    <string>moovit</string>
    <string>lyft</string>
    <string>mapsme</string>
    <string>guru</string>
    <string>om</string>
    <string>yandexmaps</string>
    <string>dgis</string>
    <string>cabify</string>
    <string>baidumap</string>
    <string>iosamap</string>
    <string>tesla</string>
    <string>taxis99</string>
</array>
```

## Android Setup

**No additional setup required!** The plugin automatically handles Android 11+ (API level 30+) package visibility and is fully backward compatible with earlier Android versions.

The plugin's manifest includes `<queries>` declarations for all supported navigation apps, which are automatically merged into your app's manifest during the build process. On Android 10 and below, the `<queries>` element is safely ignored.

<details>
<summary>For reference: Package queries included in the plugin</summary>

The following navigation apps are declared in the plugin's manifest:

```xml
<queries>
    <package android:name="com.google.android.apps.maps" />
    <package android:name="com.waze" />
    <package android:name="com.citymapper.app.release" />
    <package android:name="com.ubercab" />
    <package android:name="ru.yandex.yandexnavi" />
    <package android:name="com.sygic.aura" />
    <package android:name="com.here.app.maps" />
    <package android:name="com.tranzmate" />
    <package android:name="me.lyft.android" />
    <package android:name="com.mapswithme.maps.pro" />
    <package android:name="com.tomtom.gplay.navapp" />
    <package android:name="com.bodunov.galileo" />
    <package android:name="com.bodunov.GalileoPro" />
    <package android:name="app.organicmaps" />
    <package android:name="ru.yandex.yandexmaps" />
    <package android:name="cz.seznam.mapy" />
    <package android:name="ru.dublgis.dgismobile" />
    <package android:name="com.cabify.rider" />
    <package android:name="com.baidu.BaiduMap" />
    <package android:name="com.taxis99" />
    <package android:name="com.autonavi.minimap" />
    <package android:name="com.teslamotors.tesla" />

    <intent>
        <action android:name="android.intent.action.VIEW" />
        <data android:scheme="geo" />
    </intent>
</queries>
```

**Compatibility:**
- **Android 11+ (API 30+):** Queries are required and enforced for package visibility
- **Android 10 and below (API 29-):** Queries element is ignored; app detection works without restrictions

This ensures the plugin works correctly across all Android versions from API 24+ without any code changes.

</details>

## Usage

```typescript
import { LaunchNavigator, IOSNavigationApp, AndroidNavigationApp, TransportMode } from '@capgo/capacitor-launch-navigator';

// Navigate to a location using default app
await LaunchNavigator.navigate({
    destination: [37.7749, -122.4194] // San Francisco coordinates
});

// Navigate with options
await LaunchNavigator.navigate({
    destination: [37.7749, -122.4194],
    options: {
        start: [37.7849, -122.4094], // Starting point
        app: 'google_maps', // Specific app to use
        transportMode: TransportMode.DRIVING
    }
});

// Check if an app is available
const { available } = await LaunchNavigator.isAppAvailable({
    app: IOSNavigationApp.GOOGLE_MAPS
});

// Get list of available navigation apps
const { apps } = await LaunchNavigator.getAvailableApps();
apps.forEach(app => {
    console.log(`${app.name} is ${app.available ? 'available' : 'not installed'}`);
});

// Fetch local provider icons for display
const { icons, failures } = await LaunchNavigator.getAppIcons({
    apps: ['google_maps', 'waze']
});

icons.forEach(icon => {
    const image = document.querySelector<HTMLImageElement>(`img[data-app="${icon.app}"]`);
    if (image) {
        image.src = icon.localUrl; // Uses the local cache after the first fetch
    }
});

// Force a refresh when an icon needs to be repaired
await LaunchNavigator.refreshAppIcons({
    apps: ['waze']
});

console.log('Icon failures:', failures);
```

## Important Notes

- **Coordinates Only**: This plugin only accepts latitude/longitude coordinates for navigation. Address strings are not supported.
- **Address Geocoding**: If you need to convert addresses to coordinates, use [@capgo/capacitor-nativegeocoder](https://github.com/Cap-go/capacitor-nativegeocoder).
- **Tesla**: `app: 'tesla'` shares a Google Maps position text to the Tesla app. Android targets the Tesla app directly; iOS opens the native share sheet because iOS does not allow selecting another app's share extension programmatically.
- **PhoneGap app identifiers**: App IDs now match `phonegap-launch-navigator` for Navigon, HERE Maps, MAPS.ME, and 99 Taxi. Legacy IDs (`garmin_navigon`, `here`, `mapsme`, `99taxi`) are still accepted.
- **Android geo apps**: `app: 'geo'` opens the native Android chooser for any installed app that handles the `geo:` URI scheme. `getAvailableApps()` also returns discovered `geo:` apps by package name, so apps like Locus Map, Komoot, and OsmAnd can be listed when installed.
- **99 Taxi**: `app: 'taxis_99'` follows the PhoneGap app identifier. A start coordinate is required.

## Supported Navigation Apps

### iOS
- Apple Maps
- Google Maps
- Waze
- Citymapper
- Garmin Navigon
- Transit App
- Yandex Navigator
- Uber
- TomTom
- Sygic
- HERE Maps
- Moovit
- Lyft
- MAPS.ME
- Guru Maps
- Organic Maps
- Yandex Maps
- 2GIS
- Cabify
- Baidu Maps
- Gaode Maps
- Tesla
- 99 Taxi

### Android
- Geo intent chooser
- Google Maps
- Waze
- Citymapper
- Uber
- Yandex Navigator
- Sygic
- HERE Maps
- Moovit
- Lyft
- MAPS.ME
- TomTom GO
- Guru Maps
- Organic Maps
- Yandex Maps
- Mapy.com
- 2GIS
- Cabify
- Baidu Maps
- 99 Taxi
- Gaode Maps
- Tesla
- Any installed app that supports the Android `geo:` URI scheme

## API

<docgen-index>

* [`navigate(...)`](#navigate)
* [`isAppAvailable(...)`](#isappavailable)
* [`getAvailableApps()`](#getavailableapps)
* [`getSupportedApps()`](#getsupportedapps)
* [`getDefaultApp()`](#getdefaultapp)
* [`getAppIcons(...)`](#getappicons)
* [`refreshAppIcons(...)`](#refreshappicons)
* [`clearIconCache(...)`](#cleariconcache)
* [`getPluginVersion()`](#getpluginversion)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

Main plugin interface

### navigate(...)

```typescript
navigate(options: { destination: [number, number]; options?: NavigateOptions; }) => Promise<void>
```

Navigate to a location using latitude and longitude

| Param         | Type                                                                                                      | Description                                     |
| ------------- | --------------------------------------------------------------------------------------------------------- | ----------------------------------------------- |
| **`options`** | <code>{ destination: [number, number]; options?: <a href="#navigateoptions">NavigateOptions</a>; }</code> | Navigation options with destination coordinates |

--------------------


### isAppAvailable(...)

```typescript
isAppAvailable(options: { app: IOSNavigationApp | AndroidNavigationApp | string; }) => Promise<{ available: boolean; }>
```

Check if a specific navigation app is available

| Param         | Type                          | Description                       |
| ------------- | ----------------------------- | --------------------------------- |
| **`options`** | <code>{ app: string; }</code> | Options containing app identifier |

**Returns:** <code>Promise&lt;{ available: boolean; }&gt;</code>

--------------------


### getAvailableApps()

```typescript
getAvailableApps() => Promise<{ apps: AvailableApp[]; }>
```

Get list of available navigation apps on the device

**Returns:** <code>Promise&lt;{ apps: AvailableApp[]; }&gt;</code>

--------------------


### getSupportedApps()

```typescript
getSupportedApps() => Promise<{ apps: string[]; }>
```

Get list of supported apps for the current platform

**Returns:** <code>Promise&lt;{ apps: string[]; }&gt;</code>

--------------------


### getDefaultApp()

```typescript
getDefaultApp() => Promise<{ app: string; }>
```

Get the name of the default app for navigation

**Returns:** <code>Promise&lt;{ app: string; }&gt;</code>

--------------------


### getAppIcons(...)

```typescript
getAppIcons(options?: GetAppIconsOptions | undefined) => Promise<ProviderIconsResult>
```

Fetch provider icons and cache them locally.

The native implementations revalidate cached icons after 24 hours by default.
Pass `forceRefresh: true` to bypass the cache when an icon must be repaired.

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#getappiconsoptions">GetAppIconsOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#providericonsresult">ProviderIconsResult</a>&gt;</code>

--------------------


### refreshAppIcons(...)

```typescript
refreshAppIcons(options?: GetAppIconsOptions | undefined) => Promise<ProviderIconsResult>
```

Refresh provider icons, ignoring the cache age.

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#getappiconsoptions">GetAppIconsOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#providericonsresult">ProviderIconsResult</a>&gt;</code>

--------------------


### clearIconCache(...)

```typescript
clearIconCache(options?: ClearIconCacheOptions | undefined) => Promise<{ cleared: number; }>
```

Clear cached provider icons.

| Param         | Type                                                                    |
| ------------- | ----------------------------------------------------------------------- |
| **`options`** | <code><a href="#cleariconcacheoptions">ClearIconCacheOptions</a></code> |

**Returns:** <code>Promise&lt;{ cleared: number; }&gt;</code>

--------------------


### getPluginVersion()

```typescript
getPluginVersion() => Promise<{ version: string; }>
```

Get the native Capacitor plugin version

**Returns:** <code>Promise&lt;{ version: string; }&gt;</code>

--------------------


### Interfaces


#### NavigateOptions

Options for navigation

| Prop                  | Type                                                         | Description                                                           |
| --------------------- | ------------------------------------------------------------ | --------------------------------------------------------------------- |
| **`start`**           | <code>[number, number]</code>                                | Starting location coordinates [latitude, longitude]                   |
| **`startName`**       | <code>string</code>                                          | Starting location name                                                |
| **`destinationName`** | <code>string</code>                                          | Destination name (will be ignored since we only support coordinates)  |
| **`transportMode`**   | <code><a href="#transportmode">TransportMode</a></code>      | Transport mode                                                        |
| **`app`**             | <code>string</code>                                          | Specific app to launch (if not specified, will use default or prompt) |
| **`launchMode`**      | <code><a href="#launchmode">LaunchMode</a></code>            | Launch mode                                                           |
| **`extras`**          | <code><a href="#record">Record</a>&lt;string, any&gt;</code> | Additional parameters specific to certain apps                        |
| **`enableDebug`**     | <code>boolean</code>                                         | Enable debug logging                                                  |


#### AvailableApp

Result of checking app availability

| Prop            | Type                 | Description                                |
| --------------- | -------------------- | ------------------------------------------ |
| **`app`**       | <code>string</code>  | App identifier                             |
| **`name`**      | <code>string</code>  | Display name of the app                    |
| **`available`** | <code>boolean</code> | Whether the app is available on the device |


#### ProviderIconsResult

Result of fetching provider icons.

| Prop           | Type                               | Description                                                    |
| -------------- | ---------------------------------- | -------------------------------------------------------------- |
| **`icons`**    | <code>ProviderIcon[]</code>        | Icons available from cache or freshly downloaded               |
| **`failures`** | <code>ProviderIconFailure[]</code> | Providers that could not be fetched and had no cached fallback |


#### ProviderIcon

Cached icon for a navigation provider.

| Prop            | Type                 | Description                                                           |
| --------------- | -------------------- | --------------------------------------------------------------------- |
| **`app`**       | <code>string</code>  | Navigation app identifier                                             |
| **`name`**      | <code>string</code>  | Display name for the provider                                         |
| **`localUrl`**  | <code>string</code>  | URL that can be used directly in an image element inside the WebView. |
| **`sourceUrl`** | <code>string</code>  | Web URL used to download the cached image                             |
| **`mimeType`**  | <code>string</code>  | MIME type reported for the cached image, when known                   |
| **`fetchedAt`** | <code>number</code>  | Unix timestamp in milliseconds when the icon was last fetched         |
| **`fromCache`** | <code>boolean</code> | Whether the icon came from the local cache without a network refresh  |
| **`stale`**     | <code>boolean</code> | Whether a stale cached icon was returned because refresh failed       |


#### ProviderIconFailure

Icon fetch failure for a provider.

| Prop            | Type                | Description                     |
| --------------- | ------------------- | ------------------------------- |
| **`app`**       | <code>string</code> | Navigation app identifier       |
| **`name`**      | <code>string</code> | Display name for the provider   |
| **`sourceUrl`** | <code>string</code> | Web URL that failed, when known |
| **`message`**   | <code>string</code> | Failure message                 |


#### GetAppIconsOptions

Options for fetching navigation provider icons.

| Prop               | Type                        | Description                                                                            |
| ------------------ | --------------------------- | -------------------------------------------------------------------------------------- |
| **`apps`**         | <code>string[]</code>       | App identifiers to fetch. Defaults to all built-in providers for the current platform. |
| **`providers`**    | <code>IconProvider[]</code> | Provider definitions to fetch or override built-in provider websites.                  |
| **`maxAgeMs`**     | <code>number</code>         | Cache revalidation interval in milliseconds. Defaults to 24 hours.                     |
| **`forceRefresh`** | <code>boolean</code>        | Ignore the current cache and fetch icons again.                                        |


#### IconProvider

Web source used to discover or download a provider icon.

| Prop          | Type                | Description                                                                                                 |
| ------------- | ------------------- | ----------------------------------------------------------------------------------------------------------- |
| **`app`**     | <code>string</code> | Navigation app identifier                                                                                   |
| **`name`**    | <code>string</code> | Display name for the provider                                                                               |
| **`url`**     | <code>string</code> | Provider website used to discover favicon metadata                                                          |
| **`iconUrl`** | <code>string</code> | Direct image URL. When provided, the plugin downloads this URL instead of discovering a favicon from `url`. |


#### ClearIconCacheOptions

Options for clearing cached provider icons.

| Prop       | Type                  | Description                                             |
| ---------- | --------------------- | ------------------------------------------------------- |
| **`apps`** | <code>string[]</code> | App identifiers to clear. Defaults to all cached icons. |


### Type Aliases


#### Record

Construct a type with a set of properties K of type T

<code>{ [P in K]: T; }</code>


### Enums


#### TransportMode

| Members         | Value                    |
| --------------- | ------------------------ |
| **`DRIVING`**   | <code>'driving'</code>   |
| **`WALKING`**   | <code>'walking'</code>   |
| **`BICYCLING`** | <code>'bicycling'</code> |
| **`TRANSIT`**   | <code>'transit'</code>   |


#### IOSNavigationApp

| Members                | Value                       |
| ---------------------- | --------------------------- |
| **`APPLE_MAPS`**       | <code>'apple_maps'</code>   |
| **`GOOGLE_MAPS`**      | <code>'google_maps'</code>  |
| **`WAZE`**             | <code>'waze'</code>         |
| **`CITYMAPPER`**       | <code>'citymapper'</code>   |
| **`NAVIGON`**          | <code>'navigon'</code>      |
| **`GARMIN_NAVIGON`**   | <code>'navigon'</code>      |
| **`TRANSIT_APP`**      | <code>'transit_app'</code>  |
| **`YANDEX_NAVIGATOR`** | <code>'yandex'</code>       |
| **`UBER`**             | <code>'uber'</code>         |
| **`TOMTOM`**           | <code>'tomtom'</code>       |
| **`SYGIC`**            | <code>'sygic'</code>        |
| **`HERE_MAPS`**        | <code>'here_maps'</code>    |
| **`MOOVIT`**           | <code>'moovit'</code>       |
| **`LYFT`**             | <code>'lyft'</code>         |
| **`MAPS_ME`**          | <code>'maps_me'</code>      |
| **`GURU_MAPS`**        | <code>'guru_maps'</code>    |
| **`ORGANIC_MAPS`**     | <code>'organic_maps'</code> |
| **`YANDEX_MAPS`**      | <code>'yandex_maps'</code>  |
| **`TWO_GIS`**          | <code>'2gis'</code>         |
| **`CABIFY`**           | <code>'cabify'</code>       |
| **`BAIDU`**            | <code>'baidu'</code>        |
| **`GAODE`**            | <code>'gaode'</code>        |
| **`TESLA`**            | <code>'tesla'</code>        |
| **`TAXIS_99`**         | <code>'taxis_99'</code>     |
| **`TAXI_99`**          | <code>'taxis_99'</code>     |


#### AndroidNavigationApp

| Members            | Value                       |
| ------------------ | --------------------------- |
| **`GEO`**          | <code>'geo'</code>          |
| **`GOOGLE_MAPS`**  | <code>'google_maps'</code>  |
| **`WAZE`**         | <code>'waze'</code>         |
| **`CITYMAPPER`**   | <code>'citymapper'</code>   |
| **`UBER`**         | <code>'uber'</code>         |
| **`YANDEX`**       | <code>'yandex'</code>       |
| **`SYGIC`**        | <code>'sygic'</code>        |
| **`HERE_MAPS`**    | <code>'here_maps'</code>    |
| **`MOOVIT`**       | <code>'moovit'</code>       |
| **`LYFT`**         | <code>'lyft'</code>         |
| **`MAPS_ME`**      | <code>'maps_me'</code>      |
| **`TOMTOM`**       | <code>'tomtom'</code>       |
| **`GURU_MAPS`**    | <code>'guru_maps'</code>    |
| **`ORGANIC_MAPS`** | <code>'organic_maps'</code> |
| **`YANDEX_MAPS`**  | <code>'yandex_maps'</code>  |
| **`MAPY`**         | <code>'mapy'</code>         |
| **`TWO_GIS`**      | <code>'2gis'</code>         |
| **`CABIFY`**       | <code>'cabify'</code>       |
| **`BAIDU`**        | <code>'baidu'</code>        |
| **`GAODE`**        | <code>'gaode'</code>        |
| **`TAXIS_99`**     | <code>'taxis_99'</code>     |
| **`TAXI_99`**      | <code>'taxis_99'</code>     |
| **`TESLA`**        | <code>'tesla'</code>        |


#### LaunchMode

| Members            | Value                       |
| ------------------ | --------------------------- |
| **`MAPS`**         | <code>'maps'</code>         |
| **`TURN_BY_TURN`** | <code>'turn_by_turn'</code> |
| **`GEO`**          | <code>'geo'</code>          |

</docgen-api>

This plugin was inspired by the work of https://github.com/dpa99c/phonegap-launch-navigator
