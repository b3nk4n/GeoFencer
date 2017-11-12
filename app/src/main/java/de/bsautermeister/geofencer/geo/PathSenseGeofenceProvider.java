package de.bsautermeister.geofencer.geo;

import android.content.Context;
import android.location.Location;

import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;

import de.bsautermeister.geofencer.utils.ToastLog;

public class PathSenseGeofenceProvider implements GeofenceProvider {
    private static final String TAG = "PathSenseGeofenceProvider";

    public static final String GEOFENCE_ENTER_KEY = "ENTER";
    public static final String GEOFENCE_EXIT_KEY = "EXIT";
    public static final String GEOFENCE_BOTH_KEY = "BOTH";

    private final Context context;

    private final PathsenseLocationProviderApi api;

    public PathSenseGeofenceProvider(Context appContext) {
        this.context = appContext;
        api = PathsenseLocationProviderApi.getInstance(context);
    }

    @Override
    public void start(Location homeLocation, double enterRadius, double exitRadius, boolean initTrigger, boolean usePolling) {
        ToastLog.logShort(context, TAG, "Starting PathSense geofencing...");

        if (enterRadius == exitRadius) {
            api.addGeofence(GEOFENCE_BOTH_KEY,
                    homeLocation.getLatitude(),
                    homeLocation.getLongitude(),
                    (int)enterRadius,
                    PathSenseTransitionReceiver.class);
        } else {
            api.addGeofence(GEOFENCE_ENTER_KEY,
                    homeLocation.getLatitude(),
                    homeLocation.getLongitude(),
                    (int)enterRadius,
                    PathSenseTransitionReceiver.class);
            api.addGeofence(GEOFENCE_EXIT_KEY,
                    homeLocation.getLatitude(),
                    homeLocation.getLongitude(),
                    (int)exitRadius,
                    PathSenseTransitionReceiver.class);
        }

        if (usePolling)
            TimedGPSFixReceiver.start(context);
    }

    @Override
    public void stop() {
        ToastLog.logShort(context, TAG, "Stopping PathSense geofencing...");
        api.removeGeofences();
        api.destroy();
        TimedGPSFixReceiver.stop(context);
    }

    @Override
    public String getName() {
        return "PathSense Geofencing";
    }
}
