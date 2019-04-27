#import <CoreLocation/CoreLocation.h>
#import <Flutter/Flutter.h>
#import <Foundation/Foundation.h>

@interface LocationPermissionsPlugin
    : NSObject<FlutterPlugin, CLLocationManagerDelegate>
@end
