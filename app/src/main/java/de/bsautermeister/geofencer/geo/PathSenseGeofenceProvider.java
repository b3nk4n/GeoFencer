package de.bsautermeister.geofencer.geo;

import android.content.Context;
import android.location.Location;

import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;

public class PathSenseGeofenceProvider implements GeofenceProvider {

    public static final String GEOFENCE_ENTER_KEY = "ENTER";
    public static final String GEOFENCE_EXIT_KEY = "EXIT";
    public static final String GEOFENCE_BOTH_KEY = "BOTH";

    private final Context context;

    PathsenseLocationProviderApi api;

    public PathSenseGeofenceProvider(Context appContext) {
        this.context = appContext;
        api = PathsenseLocationProviderApi.getInstance(context);
    }

    @Override
    public void start(Location homeLocation, double enterRadius, double exitRadius, boolean initTrigger, boolean usePolling) {
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
        api.removeGeofences();
        TimedGPSFixReceiver.stop(context);
    }

    @Override
    public String getName() {
        return "PathSense Geofencing";
    }
}
