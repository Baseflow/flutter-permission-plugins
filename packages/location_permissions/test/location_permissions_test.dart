import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:location_permissions/location_permissions.dart';

void main() {
  const MethodChannel channel = MethodChannel('location_permissions');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await LocationPermissions.platformVersion, '42');
  });
}
