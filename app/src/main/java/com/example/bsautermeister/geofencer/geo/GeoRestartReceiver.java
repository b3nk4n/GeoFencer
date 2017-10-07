package com.example.bsautermeister.geofencer.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

public class GeoRestartReceiver extends BroadcastReceiver {
    private static final String TAG = "GeoRestartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!GeoLocationUtil.isGpsEnabled(context) || !GeoLocationUtil.hasGpsPermissions(context))
            return;

        Log.d(TAG, "Re-registering geofences");

        GeofenceSettings settings = new GeofenceSettings(context);

        Location homeLocation = settings.getHomeLocation();
        double radius = settings.getRadius();
        String activeGeofenceProvider = settings.getGeofenceProvider();
        boolean usePolling = settings.isGpsPollingEnabled();

        if (homeLocation != null && activeGeofenceProvider != null) {
            GeofenceProvider geofenceProvider = null;
            if (activeGeofenceProvider.equals("Play")) {
                geofenceProvider = new PlayGeofenceProvider(context);
            } else if (activeGeofenceProvider.equals("PathSense")) {
                // TODO
            }

            if (geofenceProvider != null)
                geofenceProvider.start(homeLocation, radius, usePolling);
        }
    }
}
