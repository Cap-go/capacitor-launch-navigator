# @capgo/capacitor-launch-navigator
 <a href="https://capgo.app/"><img src='https://raw.githubusercontent.com/Cap-go/capgo/main/assets/capgo_banner.png' alt='Capgo - Instant updates for capacitor'/></a>

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
    <string>cabify</string>
    <string>baidumap</string>
    <string>iosamap</string>
    <string>99app</string>
</array>
```

## Android Setup

**No additional setup required!** The plugin automatically handles Android 11+ (API level 30+) package visibility.

The plugin's manifest includes the necessary `<queries>` declarations for all supported navigation apps, which are automatically merged into your app's manifest during the build process.

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
    <package android:name="com.cabify.rider" />
    <package android:name="com.baidu.BaiduMap" />
    <package android:name="com.autonavi.minimap" />
</queries>
```

These queries enable the plugin to detect installed navigation apps on Android 11+ devices, as required by [Android's package visibility changes](https://developer.android.com/training/package-visibility).

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
```

## Important Notes

- **Coordinates Only**: This plugin only accepts latitude/longitude coordinates for navigation. Address strings are not supported.
- **Address Geocoding**: If you need to convert addresses to coordinates, use [@capgo/capacitor-nativegeocoder](https://github.com/Cap-go/capacitor-nativegeocoder).

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
- Cabify
- Baidu Maps
- Gaode Maps
- 99 Taxi

### Android
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
- Cabify
- Baidu Maps
- Gaode Maps

## API

<docgen-index>

* [`navigate(...)`](#navigate)
* [`isAppAvailable(...)`](#isappavailable)
* [`getAvailableApps()`](#getavailableapps)
* [`getSupportedApps()`](#getsupportedapps)
* [`getDefaultApp()`](#getdefaultapp)
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

| Members                | Value                         |
| ---------------------- | ----------------------------- |
| **`APPLE_MAPS`**       | <code>'apple_maps'</code>     |
| **`GOOGLE_MAPS`**      | <code>'google_maps'</code>    |
| **`WAZE`**             | <code>'waze'</code>           |
| **`CITYMAPPER`**       | <code>'citymapper'</code>     |
| **`GARMIN_NAVIGON`**   | <code>'garmin_navigon'</code> |
| **`TRANSIT_APP`**      | <code>'transit_app'</code>    |
| **`YANDEX_NAVIGATOR`** | <code>'yandex'</code>         |
| **`UBER`**             | <code>'uber'</code>           |
| **`TOMTOM`**           | <code>'tomtom'</code>         |
| **`SYGIC`**            | <code>'sygic'</code>          |
| **`HERE_MAPS`**        | <code>'here'</code>           |
| **`MOOVIT`**           | <code>'moovit'</code>         |
| **`LYFT`**             | <code>'lyft'</code>           |
| **`MAPS_ME`**          | <code>'mapsme'</code>         |
| **`CABIFY`**           | <code>'cabify'</code>         |
| **`BAIDU`**            | <code>'baidu'</code>          |
| **`GAODE`**            | <code>'gaode'</code>          |
| **`TAXI_99`**          | <code>'99taxi'</code>         |


#### AndroidNavigationApp

| Members           | Value                      |
| ----------------- | -------------------------- |
| **`GOOGLE_MAPS`** | <code>'google_maps'</code> |
| **`WAZE`**        | <code>'waze'</code>        |
| **`CITYMAPPER`**  | <code>'citymapper'</code>  |
| **`UBER`**        | <code>'uber'</code>        |
| **`YANDEX`**      | <code>'yandex'</code>      |
| **`SYGIC`**       | <code>'sygic'</code>       |
| **`HERE_MAPS`**   | <code>'here'</code>        |
| **`MOOVIT`**      | <code>'moovit'</code>      |
| **`LYFT`**        | <code>'lyft'</code>        |
| **`MAPS_ME`**     | <code>'mapsme'</code>      |
| **`CABIFY`**      | <code>'cabify'</code>      |
| **`BAIDU`**       | <code>'baidu'</code>       |
| **`GAODE`**       | <code>'gaode'</code>       |


#### LaunchMode

| Members            | Value                       |
| ------------------ | --------------------------- |
| **`MAPS`**         | <code>'maps'</code>         |
| **`TURN_BY_TURN`** | <code>'turn_by_turn'</code> |
| **`GEO`**          | <code>'geo'</code>          |

</docgen-api>

This plugin was inspired by the work of https://github.com/dpa99c/phonegap-launch-navigator
