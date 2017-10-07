package com.example.bsautermeister.geofencer.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

public class TimedGPSFixReceiver extends BroadcastReceiver {
    private static final String TAG = "TimedGPSFixReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!GeoLocationUtil.isGpsEnabled(context) || !GeoLocationUtil.hasGpsPermissions(context))
            return;

        GeoLocationProvider geoLocationProvider = new GeoLocationProvider(context);
        geoLocationProvider.setGeoLocationCallback(new GeoLocationProvider.GeoLocationCallback() {
            @Override
            public void locationUpdated(Location location) {
                GeofenceSettings settings = new GeofenceSettings(context);
                Location home = settings.getHomeLocation();
                double distance = -1;
                if (home != null)
                    distance = home.distanceTo(location);

                String accuracyString = (location.hasAccuracy() ? String.valueOf(location.getAccuracy()) : "?");
                Log.d(TAG, String.format("GPS: Acc: %s; Dist: %.2f", accuracyString, distance));
                Toast.makeText(context, accuracyString, Toast.LENGTH_LONG).show();
            }
        });
        geoLocationProvider.tryRetrieveLocation();
    }
}
