#import "LocationPermissionsPlugin.h"
#import "Enums.h"

@implementation LocationPermissionsPlugin {
  CLLocationManager *_locationManager;
  FlutterResult _result;
  PermissionLevel _permissionLevel;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
  FlutterMethodChannel *channel =
      [FlutterMethodChannel methodChannelWithName:@"com.baseflow.flutter/location_permissions"
                                  binaryMessenger:[registrar messenger]];
  LocationPermissionsPlugin *instance = [[LocationPermissionsPlugin alloc] initWithLocationManager];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (instancetype)initWithLocationManager {
  self = [super init];
  if (self) {
    _locationManager = [CLLocationManager new];
    _locationManager.delegate = self;
    _result = nil;
  }

  return self;
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
  if ([@"checkPermissionStatus" isEqualToString:call.method]) {
    [LocationPermissionsPlugin checkPermissionStatus:call result:result];
  } else if ([@"checkServiceStatus" isEqualToString:call.method]) {
    [LocationPermissionsPlugin checkServiceStatus:result];
  } else if ([@"openAppSettings" isEqualToString:call.method]) {
    [LocationPermissionsPlugin openAppSettings:result];
  } else if ([@"requestPermission" isEqualToString:call.method]) {
    if (_result != nil) {
      result([FlutterError errorWithCode:@"ERROR_ALREADY_REQUESTING_PERMISSION"
                                 message:@"A request for permissions is already running, please "
                                         @"wait for it to finish before doing another request."
                                 details:nil]);
      return;
    }

    _result = result;
    [self requestPermission:call];
  } else {
    result(FlutterMethodNotImplemented);
  }
}

+ (void)checkPermissionStatus:(FlutterMethodCall *)call result:(FlutterResult)result {
  PermissionLevel level = [LocationPermissionsPlugin decodePermissionLevel:call];
  PermissionStatus permissionStatus = [LocationPermissionsPlugin getPermissionStatus:level];

  result([[NSNumber alloc] initWithInt:permissionStatus]);
}

+ (void)checkServiceStatus:(FlutterResult)result {
  ServiceStatus serviceStatus =
      [CLLocationManager locationServicesEnabled] ? ServiceStatusEnabled : ServiceStatusDisabled;
  result([[NSNumber alloc] initWithInt:serviceStatus]);
}

+ (PermissionStatus)getPermissionStatus:(PermissionLevel)permissionLevel {
  CLAuthorizationStatus authorizationStatus = [CLLocationManager authorizationStatus];
  PermissionStatus status =
      [LocationPermissionsPlugin determinePermissionStatus:permissionLevel
                                       authorizationStatus:authorizationStatus];

  return status;
}

+ (void)openAppSettings:(FlutterResult)result {
  if (@available(iOS 10, *)) {
    [[UIApplication sharedApplication]
                  openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]
                  options:[[NSDictionary alloc] init]
        completionHandler:^(BOOL success) {
          result([[NSNumber alloc] initWithBool:success]);
        }];
  } else if (@available(iOS 8.0, *)) {
    BOOL success = [[UIApplication sharedApplication]
        openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
    result([[NSNumber alloc] initWithBool:success]);
  } else {
    result(@false);
  }
}

+ (PermissionLevel)decodePermissionLevel:(FlutterMethodCall *)call {
  NSNumber *rawData = call.arguments;

  if (rawData != nil) {
    return [rawData intValue];
  }

  return PermissionLevelLocation;
}

- (void)requestPermission:(FlutterMethodCall *)call {
  PermissionLevel level = [LocationPermissionsPlugin decodePermissionLevel:call];

  PermissionStatus status = [LocationPermissionsPlugin getPermissionStatus:level];
  CLAuthorizationStatus authorizationStatus = [CLLocationManager authorizationStatus];
  if (authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse &&
      level == PermissionLevelLocationAlways) {
    // don't do anything and continue requesting permissions
  } else if (status != PermissionStatusUnknown) {
    _result([[NSNumber alloc] initWithInt:status]);
    _result = nil;
    return;
  }

  if (level == PermissionLevelLocation) {
    if ([[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSLocationAlwaysUsageDescription"] !=
        nil) {
      _permissionLevel = PermissionLevelLocationAlways;
      [_locationManager requestAlwaysAuthorization];
    } else if ([[NSBundle mainBundle]
                   objectForInfoDictionaryKey:@"NSLocationWhenInUseUsageDescription"] != nil) {
      _permissionLevel = PermissionLevelLocationWhenInUse;
      [_locationManager requestWhenInUseAuthorization];
    } else {
      _result([FlutterError
          errorWithCode:@"ERROR_MISSING_PROPERTYKEY"
                message:@"To use location in iOS8 you need to define either "
                        @"NSLocationWhenInUseUsageDescription or NSLocationAlwaysUsageDescription "
                        @"in the app bundle's Info.plist file"
                details:nil]);
      _result = nil;
      return;
    }
  } else if (level == PermissionLevelLocationAlways) {
    if ([[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSLocationAlwaysUsageDescription"] !=
        nil) {
      _permissionLevel = PermissionLevelLocationAlways;
      [_locationManager requestAlwaysAuthorization];
    } else {
      _result([FlutterError
          errorWithCode:@"ERROR_MISSING_PROPERTYKEY"
                message:@"To use location in iOS8 you need to define "
                        @"NSLocationAlwaysUsageDescription in the app bundle's Info.plist file"
                details:nil]);
      _result = nil;
      return;
    }
  } else if (level == PermissionLevelLocationWhenInUse) {
    if ([[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSLocationWhenInUseUsageDescription"] !=
        nil) {
      _permissionLevel = PermissionLevelLocationWhenInUse;
      [_locationManager requestWhenInUseAuthorization];
    } else {
      _result([FlutterError
          errorWithCode:@"ERROR_MISSING_PROPERTYKEY"
                message:@"To use location in iOS8 you need to define "
                        @"NSLocationWhenInUseUsageDescription in the app bundle's Info.plist file"
                details:nil]);
      _result = nil;
      return;
    }
  }
}

- (void)locationManager:(CLLocationManager *)manager
    didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
  if (_result == nil) {
    return;
  }

  if (status == kCLAuthorizationStatusNotDetermined) {
    _result([[NSNumber alloc] initWithInt:PermissionStatusUnknown]);
    _result = nil;
    return;
  }

  PermissionStatus permissionStatus =
      [LocationPermissionsPlugin determinePermissionStatus:_permissionLevel
                                       authorizationStatus:status];

  _result([[NSNumber alloc] initWithInt:permissionStatus]);
  _result = nil;
}

+ (PermissionStatus)determinePermissionStatus:(PermissionLevel)permissionLevel
                          authorizationStatus:(CLAuthorizationStatus)authorizationStatus {
  if (@available(iOS 8.0, *)) {
    if (permissionLevel == PermissionLevelLocationAlways) {
      switch (authorizationStatus) {
        case kCLAuthorizationStatusNotDetermined:
          return PermissionStatusUnknown;
        case kCLAuthorizationStatusRestricted:
          return PermissionStatusRestricted;
        case kCLAuthorizationStatusDenied:
          return PermissionStatusDenied;
        case kCLAuthorizationStatusAuthorizedWhenInUse:
        case kCLAuthorizationStatusAuthorizedAlways:
          return PermissionStatusGranted;
      }
    }

    switch (authorizationStatus) {
      case kCLAuthorizationStatusNotDetermined:
        return PermissionStatusUnknown;
      case kCLAuthorizationStatusRestricted:
        return PermissionStatusRestricted;
      case kCLAuthorizationStatusDenied:
        return PermissionStatusDenied;
      case kCLAuthorizationStatusAuthorizedAlways:
      case kCLAuthorizationStatusAuthorizedWhenInUse:
        return PermissionStatusGranted;
    }
  }

  switch (authorizationStatus) {
    case kCLAuthorizationStatusNotDetermined:
      return PermissionStatusUnknown;
    case kCLAuthorizationStatusRestricted:
      return PermissionStatusRestricted;
    case kCLAuthorizationStatusDenied:
      return PermissionStatusDenied;
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
    case kCLAuthorizationStatusAuthorized:
      return PermissionStatusGranted;
#pragma clang diagnostic warning "-Wdeprecated-declarations"
    default:
      return PermissionStatusUnknown;
  }
}

@end
