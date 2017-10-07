package com.example.bsautermeister.geofencer.geo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

public class GeoLocationUtil {
    /**
     * Checks whether the GPS is enabled on this device.
     * @param context The app context.
     * @return True if enabled, else False.
     */
    public static boolean isGpsEnabled(final Context context) {
        final LocationManager manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean hasGpsPermissions(final Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
