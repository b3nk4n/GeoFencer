package de.bsautermeister.geofencer.utils;

import android.support.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceUtils {
    public static String transitionString(@NonNull final GeofencingEvent geofencingEvent) {
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
