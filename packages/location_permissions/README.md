# Flutter Location Permissions Plugin

[![pub package](https://img.shields.io/pub/v/location_permissions.svg)](https://pub.dartlang.org/packages/location_permissions)

The Location Permissions plugin for Flutter. This plugin provides a cross-platform (iOS, Android) API to check and request permissions to access location services.

Branch  | Build Status 
------- | ------------
develop | [![Build Status](https://travis-ci.com/BaseflowIT/flutter-permission-plugins.svg?branch=develop)](https://travis-ci.com/BaseflowIT/flutter-permission-plugins)
master  | [![Build Status](https://travis-ci.com/BaseflowIT/flutter-permission-plugins.svg?branch=master)](https://travis-ci.com/BaseflowIT/flutter-permission-plugins)

## Features

* Check if permission to access location services is granted.
* Request permission to access location services.
* Open app settings so the user can allow permission to the location services.
* Show a rationale for requesting permission to access location services (Android).

## Usage

To use this plugin, add `location_permissions` as a [dependency in your pubspec.yaml file](https://flutter.io/platform-plugins/). For example:

```yaml
dependencies:
  location_permissions: ^3.0.0
```

> **NOTE:** The location_permissions plugin uses the AndroidX version of the Android Support Libraries. This means you need to make sure your Android project is also upgraded to support AndroidX. Detailed instructions can be found [here](https://flutter.dev/docs/development/packages-and-plugins/androidx-compatibility). 
>
>The TL;DR version is:
>
>1. Add the following to your "gradle.properties" file:
>
>```
>android.useAndroidX=true
>android.enableJetifier=true
>```
>2. Make sure you set the `compileSdkVersion` in your "android/app/build.gradle" file to 28 or higher:
>
>```
>android {
>  compileSdkVersion 28
>
>  ...
>}
>```
>3. Make sure you replace all the `android.` dependencies to their AndroidX counterparts (a full list can be found here: https://developer.android.com/jetpack/androidx/migrate).

## API

### Requesting permission

```dart
import 'package:location_permissions/location_permissions.dart';

PermissionStatus permission = await LocationPermissions().requestPermissions();
```

### Checking permission

```dart
import 'package:location_permissions/location_permissions.dart';

PermissionStatus permission = await LocationPermissions().checkPermissionStatus();
```

### Checking service status

```dart
import 'package:location_permissions/location_permissions.dart';

ServiceStatus serviceStatus = await LocationPermissions().checkServiceStatus();
```

### Open app settings

```dart
import 'package:location_permissions/location_permissions.dart';

bool isOpened = await LocationPermissions().openAppSettings();
```

### Show a rationale for requesting permission (Android only)

```dart
import 'package:location_permissions/location_permissions.dart';

bool isShown = await LocationPermissions().shouldShowRequestPermissionRationale();
```

This will always return `false` on iOS.

### List of available permissions levels (only applicable for iOS)

Defines the location permission levels for which can be used on iOS to distinguish between permission to access location services when the app is in use or always. 

```dart
enum LocationPermissionLevel {
  /// Android: Fine and Coarse Location
  /// iOS: CoreLocation (Always and WhenInUse)
  location,
  
  /// Android: Fine and Coarse Location
  /// iOS: CoreLocation - Always
  locationAlways,

  /// Android: Fine and Coarse Location
  /// iOS: CoreLocation - WhenInUse
  locationWhenInUse,
}
```

### Status of the permission

Defines the state of a location permissions

```dart
enum PermissionStatus {
  /// Permission to access the location services is denied by the user.
  denied,

  /// Permission to access the location services is granted by the user.
  granted,

  /// The user granted restricted access to the location services (only on iOS).
  restricted,

  /// Permission is in an unknown state
  unknown
}
```

### Overview of possible service statuses

Defines the state of the location services on the platform

```dart
/// Defines the state of the location services
enum ServiceStatus {
  /// The unknown service status indicates the state of the location services could not be determined.
  unknown,

  /// Location services are not available on the device.
  notApplicable,

  /// The location services are disabled.
  disabled,

  /// The location services are enabled.
  enabled
}
```

## Issues

Please file any issues, bugs or feature request as an issue on our [GitHub](https://github.com/BaseflowIT/flutter-permission-handlers/issues) page.

## Want to contribute

If you would like to contribute to the plugin (e.g. by improving the documentation, solving a bug or adding a cool new feature), please carefully review our [contribution guide](https://github.com/Baseflow/flutter-permission-plugins/blob/develop/CONTRIBUTING.md) and send us your [pull request](https://github.com/BaseflowIT/flutter-permission-handlers/pulls).

## Author

This Permission handler plugin for Flutter is developed by [Baseflow](https://baseflow.com). You can contact us at <hello@baseflow.com>
