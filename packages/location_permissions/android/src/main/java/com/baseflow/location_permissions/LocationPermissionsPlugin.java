package com.baseflow.location_permissions;

import static io.flutter.plugin.common.EventChannel.EventSink;
import static io.flutter.plugin.common.EventChannel.StreamHandler;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.IntDef;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** LocationPermissionsPlugin */
public class LocationPermissionsPlugin implements MethodCallHandler, StreamHandler {
  private static final String LOG_TAG = "location_permissions";
  private static final int PERMISSION_CODE = 25;

  //PERMISSION_STATUS
  private static final int PERMISSION_STATUS_UNKNOWN = 0;
  private static final int PERMISSION_STATUS_DENIED = 1;
  private static final int PERMISSION_STATUS_GRANTED = 2;
  private static final int PERMISSION_STATUS_RESTRICTED = 3;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    PERMISSION_STATUS_UNKNOWN,
    PERMISSION_STATUS_DENIED,
    PERMISSION_STATUS_GRANTED,
    PERMISSION_STATUS_RESTRICTED,
  })
  private @interface PermissionStatus {}

  //SERVICE_STATUS
  private static final int SERVICE_STATUS_UNKNOWN = 0;
  private static final int SERVICE_STATUS_DISABLED = 1;
  private static final int SERVICE_STATUS_ENABLED = 2;
  private static final int SERVICE_STATUS_NOT_APPLICABLE = 3;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    SERVICE_STATUS_DISABLED,
    SERVICE_STATUS_ENABLED,
    SERVICE_STATUS_NOT_APPLICABLE,
    SERVICE_STATUS_UNKNOWN,
  })
  private @interface ServiceStatus {}

  private final Registrar mRegistrar;
  private Result mResult;
  private EventSink mEventSink;
  private final IntentFilter mIntentFilter;
  private final LocationServiceBroadcastReceiver mReceiver;

  private LocationPermissionsPlugin(Registrar mRegistrar) {
    this.mRegistrar = mRegistrar;
    mReceiver = new LocationServiceBroadcastReceiver(this);
    mIntentFilter =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            ? new IntentFilter(LocationManager.MODE_CHANGED_ACTION)
            : null;
  }

  /** Plugin registration. */
  public static void registerWith(final Registrar registrar) {
    final MethodChannel channel =
        new MethodChannel(registrar.messenger(), "com.baseflow.flutter/location_permissions");
    final EventChannel eventChannel =
        new EventChannel(registrar.messenger(), "com.baseflow.flutter/location_permissions_events");
    final LocationPermissionsPlugin locationPermissionsPlugin =
        new LocationPermissionsPlugin(registrar);
    channel.setMethodCallHandler(locationPermissionsPlugin);
    eventChannel.setStreamHandler(locationPermissionsPlugin);

    registrar.addRequestPermissionsResultListener(
        new PluginRegistry.RequestPermissionsResultListener() {
          @Override
          public boolean onRequestPermissionsResult(
              int id, String[] permissions, int[] grantResults) {
            if (id == PERMISSION_CODE) {
              locationPermissionsPlugin.handlePermissionsRequest(permissions, grantResults);
              return true;
            } else {
              return false;
            }
          }
        });
  }

  private void emitLocationServiceStatus(boolean enabled) {
    if (mEventSink != null) {
      mEventSink.success(enabled);
    }
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    final Context context = mRegistrar.context();

    if (context == null) {
      Log.d(LOG_TAG, "Unable to detect current Activity or App Context.");
      result.error(
          "ERROR_MISSING_CONTEXT", "Unable to detect current Activity or Active Context.", null);
      return;
    }

    switch (call.method) {
      case "checkPermissionStatus":
        @PermissionStatus
        final int permissionStatus = LocationPermissionsPlugin.checkPermissionStatus(context);
        result.success(permissionStatus);
        break;
      case "checkServiceStatus":
        @ServiceStatus
        final int serviceStatus = LocationPermissionsPlugin.checkServiceStatus(context);
        result.success(serviceStatus);
        break;
      case "requestPermission":
        if (mResult != null) {
          result.error(
              "ERROR_ALREADY_REQUESTING_PERMISSIONS",
              "A request for permissions is already running, please wait for it to finish before doing another request (note that you can request multiple permissions at the same time).",
              null);
          return;
        }

        mResult = result;
        requestPermissions();
        break;
      case "shouldShowRequestPermissionRationale":
        final Activity activity = mRegistrar.activity();
        final boolean shouldShow =
            LocationPermissionsPlugin.shouldShowRequestPermissionRationale(activity);

        result.success(shouldShow);
        break;
      case "openAppSettings":
        boolean isOpen = LocationPermissionsPlugin.openAppSettings(context);
        result.success(isOpen);
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onListen(Object arguments, EventSink events) {
    events.success(isLocationServiceEnabled(mRegistrar.context()));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      mRegistrar.context().registerReceiver(mReceiver, mIntentFilter);
      mEventSink = events;
    } else {
      throw new UnsupportedOperationException(
          "Location service availability stream requires at least Android K.");
    }
  }

  @Override
  public void onCancel(Object arguments) {
    if (mEventSink != null) {
      mRegistrar.context().unregisterReceiver(mReceiver);
      mEventSink = null;
    }
  }

  @PermissionStatus
  private static int checkPermissionStatus(Context context) {
    final List<String> names = LocationPermissionsPlugin.getManifestNames(context);

    if (names == null) {
      Log.d(LOG_TAG, "No android specific permissions needed for: $permission");

      return PERMISSION_STATUS_GRANTED;
    }

    //if no permissions were found then there is an issue and permission is not set in Android manifest
    if (names.size() == 0) {
      Log.d(LOG_TAG, "No permissions found in manifest for: $permission");
      return PERMISSION_STATUS_UNKNOWN;
    }

    if (context == null) {
      return PERMISSION_STATUS_UNKNOWN;
    }

    final boolean targetsMOrHigher =
        context.getApplicationInfo().targetSdkVersion >= android.os.Build.VERSION_CODES.M;

    for (String name : names) {
      if (targetsMOrHigher) {
        final int permissionStatus = ContextCompat.checkSelfPermission(context, name);
        if (permissionStatus == PackageManager.PERMISSION_DENIED) {
          return PERMISSION_STATUS_DENIED;
        } else if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
          return PERMISSION_STATUS_UNKNOWN;
        }
      }
    }

    return PERMISSION_STATUS_GRANTED;
  }

  @ServiceStatus
  private static int checkServiceStatus(Context context) {

    if (context == null) {
      return SERVICE_STATUS_UNKNOWN;
    }

    return isLocationServiceEnabled(context) ? SERVICE_STATUS_ENABLED : SERVICE_STATUS_DISABLED;
  }

  private void requestPermissions() {
    final Activity activity = mRegistrar.activity();

    if (activity == null) {
      Log.d(LOG_TAG, "Unable to detect current Activity.");

      processResult(PERMISSION_STATUS_UNKNOWN);
      return;
    }

    @PermissionStatus final int permissionStatus = checkPermissionStatus(activity);
    if (permissionStatus != PERMISSION_STATUS_GRANTED) {
      final List<String> names = getManifestNames(activity);

      //check to see if we can find manifest names
      //if we can't add as unknown and continue
      if (names == null || names.isEmpty()) {
        processResult(PERMISSION_STATUS_UNKNOWN);
        return;
      }

      ActivityCompat.requestPermissions(
          mRegistrar.activity(), names.toArray(new String[0]), PERMISSION_CODE);
    } else {
      processResult(PERMISSION_STATUS_GRANTED);
    }
  }

  private void handlePermissionsRequest(String[] permissions, int[] grantResults) {
    if (mResult == null) {
      Log.e(LOG_TAG, "Flutter result object is null.");
      return;
    }

    for (int i = 0; i < permissions.length; i++) {
      if (LocationPermissionsPlugin.isLocationPermission(permissions[i])) {
        @PermissionStatus int permissionStatus = toPermissionStatus(grantResults[i]);

        processResult(permissionStatus);
        return;
      }
    }

    processResult(PERMISSION_STATUS_UNKNOWN);
  }

  private static Boolean isLocationPermission(String permission) {
    return permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
            || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)
            || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION));
  }

  @PermissionStatus
  private int toPermissionStatus(int grantResult) {
    return grantResult == PackageManager.PERMISSION_GRANTED
        ? PERMISSION_STATUS_GRANTED
        : PERMISSION_STATUS_DENIED;
  }

  private void processResult(@PermissionStatus int status) {
    mResult.success(status);
    mResult = null;
  }

  private static List<String> getManifestNames(Context context) {
    final ArrayList<String> permissionNames = new ArrayList<>();

    if (hasPermissionInManifest(Manifest.permission.ACCESS_COARSE_LOCATION, context)) {
      permissionNames.add(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    if (hasPermissionInManifest(Manifest.permission.ACCESS_FINE_LOCATION, context)) {
      permissionNames.add(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && hasPermissionInManifest(Manifest.permission.ACCESS_BACKGROUND_LOCATION, context)) {
      permissionNames.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    }

    return permissionNames;
  }

  private static boolean hasPermissionInManifest(String permission, Context context) {
    try {
      PackageInfo info =
          context
              .getPackageManager()
              .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);

      if (info == null) {
        Log.d(
            LOG_TAG,
            "Unable to get Package info, will not be able to determine permissions to request.");
        return false;
      }

      final List<String> manifestPermissions =
          new ArrayList<>(Arrays.asList(info.requestedPermissions));
      for (String r : manifestPermissions) {
        if (r.equals(permission)) {
          return true;
        }
      }
    } catch (Exception ex) {
      Log.d(LOG_TAG, "Unable to check manifest for permission: ", ex);
    }
    return false;
  }

  @SuppressWarnings("deprecation")
  private static boolean isLocationServiceEnabled(Context context) {
    if (Build.VERSION.SDK_INT >= 28) {
      final LocationManager locationManager = context.getSystemService(LocationManager.class);
      if (locationManager == null) {
        return false;
      }

      return locationManager.isLocationEnabled();
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      final int locationMode;

      try {
        locationMode =
            Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
      } catch (Settings.SettingNotFoundException e) {
        e.printStackTrace();
        return false;
      }

      return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    } else {
      final String locationProviders =
          Settings.Secure.getString(
              context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
      return !TextUtils.isEmpty(locationProviders);
    }
  }

  private static boolean shouldShowRequestPermissionRationale(Activity activity) {
    if (activity == null) {
      Log.e(LOG_TAG, "Unable to detect current Activity.");
      return false;
    }

    List<String> names = getManifestNames(activity);

    // if isn't an android specific group then go ahead and return false;
    if (names == null) {
      Log.d(LOG_TAG, "No android specific permissions needed for: $permission");
      return false;
    }

    if (names.isEmpty()) {
      Log.d(
          LOG_TAG,
          "No permissions found in manifest for: $permission no need to show request rationale");
      return false;
    }

    //noinspection LoopStatementThatDoesntLoop
    for (String name : names) {
      return ActivityCompat.shouldShowRequestPermissionRationale(activity, name);
    }

    return false;
  }

  private static boolean openAppSettings(Context context) {
    try {
      Intent settingsIntent = new Intent();
      settingsIntent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
      settingsIntent.addCategory(Intent.CATEGORY_DEFAULT);
      settingsIntent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
      settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
      settingsIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

      context.startActivity(settingsIntent);

      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  private static class LocationServiceBroadcastReceiver extends BroadcastReceiver {
    private final LocationPermissionsPlugin locationPermissionsPlugin;

    private LocationServiceBroadcastReceiver(LocationPermissionsPlugin locationPermissionsPlugin) {
      this.locationPermissionsPlugin = locationPermissionsPlugin;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      locationPermissionsPlugin.emitLocationServiceStatus(isLocationServiceEnabled(context));
    }
  }
}
