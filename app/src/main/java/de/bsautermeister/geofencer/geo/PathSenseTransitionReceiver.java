package de.bsautermeister.geofencer.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pathsense.android.sdk.location.PathsenseGeofenceEvent;

import java.util.Random;

import de.bsautermeister.geofencer.notification.SimpleNotification;
import de.bsautermeister.geofencer.utils.DateUtils;
import de.bsautermeister.geofencer.utils.ToastLog;

public class PathSenseTransitionReceiver extends BroadcastReceiver {
    private static final String TAG = "PathSenseTransitionReceiver";

    SimpleNotification notification;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PathsenseGeofenceEvent geofenceEvent = PathsenseGeofenceEvent.fromIntent(intent);
        notification = new SimpleNotification(context);

        if (geofenceEvent != null)
        {
            boolean invalidIngress = geofenceEvent.isIngress() &&
                    geofenceEvent.getGeofenceId().equals(PathSenseGeofenceProvider.GEOFENCE_EXIT_KEY);
            boolean invalidEgress = geofenceEvent.isEgress() &&
                    geofenceEvent.getGeofenceId().equals(PathSenseGeofenceProvider.GEOFENCE_ENTER_KEY);

            // check for invalid ingress/egress to support 2 different radii (which might no be really
            // necessary when using PathSense, because they advertise with having less false positives)
            if (invalidEgress || invalidIngress)
                return;

            String type = "UNKNOWN";
            if (geofenceEvent.isIngress())
            {
                type = "INGRESS";
            }
            else if (geofenceEvent.isEgress())
            {
                type = "EGRESS";
            }

            String content = String.format("%s: %s",
                    DateUtils.getTimestamptString(),
                    type);
            notification.show(new Random().nextInt(), content);
        } else {
            // we end up here e.g. when the user deactivates GPS on the device
            ToastLog.warnLong(context, TAG, "Unknown error.");
        }
    }
}
