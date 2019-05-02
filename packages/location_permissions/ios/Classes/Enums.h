//
//  Enums.h
//  Pods
//
//  Created by Maurits van Beusekom on 15/04/2019.
//

typedef NS_ENUM(int, PermissionLevel) {
  PermissionLevelLocation = 0,
  PermissionLevelLocationWhenInUse,
  PermissionLevelLocationAlways,
};

typedef NS_ENUM(int, PermissionStatus) {
  PermissionStatusUnknown = 0,
  PermissionStatusDenied,
  PermissionStatusGranted,
  PermissionStatusRestricted,
};

typedef NS_ENUM(int, ServiceStatus) {
  ServiceStatusUnknown = 0,
  ServiceStatusDisabled,
  ServiceStatusEnabled,
  ServiceStatusNotApplicable,
};
