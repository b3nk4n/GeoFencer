package com.example.bsautermeister.geofencer.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

public class GeoTransitionReceiver extends BroadcastReceiver {
    private static final String TAG = "GeoTransitionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent != null && !geofencingEvent.hasError()) {
            int transition = geofencingEvent.getGeofenceTransition();
            Log.d(TAG, "Geofence Transition: " + transition);
            handleReceivedEventOnBackgroundThread(geofencingEvent);
        } else {
            // we end up here e.g. when the user deactivates GPS on the device
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, "Retrieved Geofence Error for Home :" + errorMessage);
        }
    }

    // TODO missing background thread here in Comfylight project?
    void handleReceivedEventOnBackgroundThread(@NonNull final GeofencingEvent geofencingEvent) {
        //notificationHandler.handleReceivedTransitionEvent(geofencingEvent, homeId);
    }
}
