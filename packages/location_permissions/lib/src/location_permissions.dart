import 'dart:async';

import 'package:flutter/services.dart';
import 'package:location_permissions/src/permission_enums.dart';

class LocationPermissions {
  static const MethodChannel _channel =
      const MethodChannel('com.baseflow.flutter/location_permissions');

  /// Check current permission status.
  ///
  /// Returns a [Future] containing the current permission status for the supplied [LocationPermissionLevel].
  static Future<PermissionStatus> checkPermissionStatus(
      {LocationPermissionLevel level =
          LocationPermissionLevel.location}) async {
    final int status =
        await _channel.invokeMethod('checkPermissionStatus', level.index);

    return PermissionStatus.values[status];
  }

  /// Check current service status.
  ///
  /// Returns a [Future] containing the current service status for the supplied [LocationPermissionLevel].
  static Future<ServiceStatus> checkServiceStatus(
      {LocationPermissionLevel level =
          LocationPermissionLevel.location}) async {
    final int status =
        await _channel.invokeMethod('checkServiceStatus', level.index);

    return ServiceStatus.values[status];
  }
}
