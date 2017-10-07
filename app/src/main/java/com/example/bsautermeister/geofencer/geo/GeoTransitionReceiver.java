package com.example.bsautermeister.geofencer.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.bsautermeister.geofencer.notification.SimpleNotification;
import com.example.bsautermeister.geofencer.utils.DateUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

public class GeoTransitionReceiver extends BroadcastReceiver {
    private static final String TAG = "GeoTransitionReceiver";

    SimpleNotification notification;

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        notification = new SimpleNotification(context);

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
    private void handleReceivedEventOnBackgroundThread(@NonNull final GeofencingEvent geofencingEvent) {
        String content = String.format("%s: %s", DateUtils.getTimestamptString(), transitionString(geofencingEvent));
        notification.show(0, content);
    }

    private String transitionString(@NonNull final GeofencingEvent geofencingEvent) {
        int transitionCode = geofencingEvent.getGeofenceTransition();
        if (transitionCode == Geofence.GEOFENCE_TRANSITION_EXIT)
            return "EXIT";
        else if (transitionCode == Geofence.GEOFENCE_TRANSITION_ENTER)
            return "ENTER";
        else if (transitionCode == Geofence.GEOFENCE_TRANSITION_DWELL)
            return "DWELL";
        else
            return "UNKNOWN";
    }
}
