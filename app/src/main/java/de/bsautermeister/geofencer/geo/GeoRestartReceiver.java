package de.bsautermeister.geofencer.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import de.bsautermeister.geofencer.utils.ToastLog;

public class GeoRestartReceiver extends BroadcastReceiver {
    private static final String TAG = "GeoRestartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!GeoLocationUtil.isGpsEnabled(context) || !GeoLocationUtil.hasGpsPermissions(context))
            return;

        GeofenceSettings settings = new GeofenceSettings(context);

        if (!settings.isGeofencingActive()) {
            ToastLog.logLong(context, TAG, "No provider active.");
            return;
        }

        ToastLog.logLong(context, TAG, "Re-registering provider...");

        Location homeLocation = settings.getHomeLocation();
        double enterRadius = settings.getEnterRadius();
        double exitRadius = settings.getExitRadius();
        String activeGeofenceProvider = settings.getGeofenceProvider();
        boolean usePolling = settings.isGpsPollingEnabled();

        if (homeLocation != null && activeGeofenceProvider != null) {
            GeofenceProvider geofenceProvider = null;
            if (activeGeofenceProvider.equals("Play")) {
                geofenceProvider = new PlayGeofenceProvider(context);
            } else if (activeGeofenceProvider.equals("PathSense")) {
                // TODO
            }

            if (geofenceProvider != null) {
                geofenceProvider.start(homeLocation, enterRadius, exitRadius, usePolling);
            }
        }
    }
}
