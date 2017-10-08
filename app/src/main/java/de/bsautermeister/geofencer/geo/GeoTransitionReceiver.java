package de.bsautermeister.geofencer.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import de.bsautermeister.geofencer.notification.SimpleNotification;
import de.bsautermeister.geofencer.utils.DateUtils;
import de.bsautermeister.geofencer.utils.ToastLog;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.Random;

public class GeoTransitionReceiver extends BroadcastReceiver {
    private static final String TAG = "GeoTransitionReceiver";

    SimpleNotification notification;

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        notification = new SimpleNotification(context);

        if (geofencingEvent != null && !geofencingEvent.hasError()) {
            handleReceivedEventOnBackgroundThread(geofencingEvent);
        } else {
            // we end up here e.g. when the user deactivates GPS on the device
            ToastLog.warnLong(context, TAG, "Error: " + geofencingEvent.getErrorCode());
        }
    }

    // TODO missing background thread here in Comfylight project? Or are broadcast-receivers generally in background thread?
    private void handleReceivedEventOnBackgroundThread(@NonNull final GeofencingEvent geofencingEvent) {
        String content = String.format("%s: %s", DateUtils.getTimestamptString(), transitionString(geofencingEvent));
        notification.show(new Random().nextInt(), content);
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
