import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:location_permissions/src/permission_enums.dart';
import 'package:meta/meta.dart';

class LocationPermissions {
  factory LocationPermissions() {
    if (_instance == null) {
      const MethodChannel methodChannel =
          MethodChannel('com.baseflow.flutter/location_permissions');
      final EventChannel? eventChannel = Platform.isAndroid
          ? const EventChannel(
              'com.baseflow.flutter/location_permissions_events')
          : null;

      _instance = LocationPermissions.private(methodChannel, eventChannel);
    }
    return _instance!;
  }

  @visibleForTesting
  LocationPermissions.private(this._methodChannel, this._eventChannel);

  static LocationPermissions? _instance;

  final MethodChannel _methodChannel;
  final EventChannel? _eventChannel;

  /// Check current permission status.
  ///
  /// Returns a [Future] containing the current permission status for the supplied [LocationPermissionLevel].
  Future<PermissionStatus> checkPermissionStatus(
      {LocationPermissionLevel level =
          LocationPermissionLevel.location}) async {
    final int status =
        await _methodChannel.invokeMethod('checkPermissionStatus', level.index);

    return PermissionStatus.values[status];
  }

  /// Check current service status.
  ///
  /// Returns a [Future] containing the current service status for the supplied [LocationPermissionLevel].
  Future<ServiceStatus> checkServiceStatus(
      {LocationPermissionLevel level =
          LocationPermissionLevel.location}) async {
    final int status =
        await _methodChannel.invokeMethod('checkServiceStatus', level.index);

    return ServiceStatus.values[status];
  }

  /// Open the App settings page.
  ///
  /// Returns [true] if the app settings page could be opened, otherwise [false] is returned.
  Future<bool> openAppSettings() async {
    final bool? hasOpened =
        await _methodChannel.invokeMethod('openAppSettings');

    return hasOpened ?? false;
  }

  /// Request the user for access to the location services.
  ///
  /// Returns a [Future<PermissionStatus>] containing the permission status.
  Future<PermissionStatus> requestPermissions(
      {LocationPermissionLevel permissionLevel =
          LocationPermissionLevel.location}) async {
    final int status = await _methodChannel.invokeMethod(
        'requestPermission', permissionLevel.index);

    return PermissionStatus.values[status];
  }

  /// Request to see if you should show a rationale for requesting permission.
  ///
  /// This method is only implemented on Android, calling this on iOS always
  /// returns [false].
  Future<bool> shouldShowRequestPermissionRationale(
      {LocationPermissionLevel permissionLevel =
          LocationPermissionLevel.location}) async {
    if (!Platform.isAndroid) {
      return false;
    }

    final bool? shouldShowRationale = await _methodChannel.invokeMethod(
        'shouldShowRequestPermissionRationale', permissionLevel.index);

    return shouldShowRationale ?? false;
  }

  /// Allows listening to the enabled/disabled state of the location service, currently only on Android.
  ///
  /// This is basically the stream version of [checkPermissionStatus()].
  Stream<ServiceStatus> get serviceStatus {
    assert(Platform.isAndroid,
        'Listening to service state changes is only supported on Android.');

    return _eventChannel!.receiveBroadcastStream().asBroadcastStream().map(
        (dynamic status) =>
            status ? ServiceStatus.enabled : ServiceStatus.disabled);
  }
}
