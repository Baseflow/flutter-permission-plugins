## 3.0.0+1

* Android: fix bug where the plugin didn't differentiate between `locationWhenInUse` and `locationAlways` (see issue [#41](https://github.com/Baseflow/flutter-permission-plugins/issues/41) )
* iOS: fix bug where the plugin reports `granted` for `locationAlways` permission when user only allowed `locationWhenInUse`.

## 3.0.0

* Android: migrate to the version 2 of the Android Plugin API (for details see: https://flutter.dev/docs/development/packages-and-plugins/plugin-api-migration)

## 2.0.5

* iOS: fixed bug where permssions for locationWhenInUse are reported as denied incorrectly.

## 2.0.4+1

* Fixed merge conflict

## 2.0.4

* Synchronized Gradle and Gradle Wrapper with Flutter stable (1.12.13+hotfix.5)
* Added support for Flutter analysis

## 2.0.3

* Added support for Android 10 background permissions;
* Ensure `shouldShowRequestPermissionRationale` is executed on the Android Activity.

## 2.0.2

* Fixed bug where method `shouldShowRequestPermissionRationale` always returns `false`.

## 2.0.1

* Fixed a bug where permissions on iOS are not requested in some cases;
* Updated examples in the README to match the instance methods introduced in version 2.0.0.

## 2.0.0

* **breaking** Changed from static to instance methods to improve testability;
* Added support for CocoaPods `staticframework`;
* Cleaned up some duplicated code to make code-base easier to maintain.

## 1.1.0

* Adds support to listen for location service availability using a stream on Android (on iOS this is ignored, since it isn't supported by Apple);
* Fixed multi-dex and AndroidX support.

## 1.0.2

* Use the correct homepage in the pubspec.yaml

## 1.0.1

* Ignore warnings on iOS for calling deprecated methods (these are only called on older iOS platforms).

## 1.0.0

* Initial release
