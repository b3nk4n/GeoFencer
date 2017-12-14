package de.bsautermeister.geofencer.geo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import de.bsautermeister.geofencer.utils.ToastLog;

public class PlayGeofenceProvider implements GeofenceProvider {
    private static final String TAG = "PlayGeofenceProvider";

    private static final String GEO_ENTER_LISTENER_KEY = PlayGeofenceProvider.class.getName() + ".ENTER";
    private static final String GEO_EXIT_LISTENER_KEY = PlayGeofenceProvider.class.getName() + ".EXIT";
    private static final String GEO_BOTH_LISTENER_KEY = PlayGeofenceProvider.class.getName() + ".BOTH";

    private Context context;
    private GoogleApiClient googleApiClient;
    private PendingIntent pendingIntent;

    private Location homeLocation;
    private double enterRadius;
    private double exitRadius;
    private boolean initTrigger;

    public PlayGeofenceProvider(Context appContext) {
        this.context = appContext;
        initGoogleApiClient();
    }

    private void initGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        if (!googleApiClient.isConnected() || !googleApiClient.isConnecting())
            googleApiClient.connect();
    }

    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            ToastLog.logShort(context, TAG, "Google API connected.");

            if (homeLocation != null) {
                startInternal(homeLocation, pendingIntent);

                homeLocation = null;
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            ToastLog.warnLong(context, TAG, "Google API suspended.");
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            ToastLog.warnShort(context, TAG, "Google API failed.");
            switch (connectionResult.getErrorCode()) {
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED: // SERVICE_VERSION_UPDATE_REQUIRED
                    break;
            }
        }
    };

    @Override
    public void start(Location homeLocation, double enterRadius, double exitRadius,
                      boolean initTrigger, boolean usePolling) {
        this.homeLocation = homeLocation;
        this.enterRadius = enterRadius;
        this.exitRadius = exitRadius;
        this.initTrigger = initTrigger;

        if (GeoLocationUtil.hasGpsPermissions(context)) {
            PendingIntent pendingIntent = getGeofencingPendingIntent(context);

            if (googleApiClient.isConnected()) {
                startInternal(homeLocation, pendingIntent);
            } else {
                // delay internal start (called via onConnected() callback)
                this.pendingIntent = pendingIntent;
            }

            if (usePolling)
                TimedGPSFixReceiver.start(context);
        } else {
            ToastLog.warnShort(context, TAG, "GPS permission required.");
        }
    }

    // According to documentation there are cases when registered Geofences have to be re-registered, among others:
    // - The app has received a GEOFENCE_NOT_AVAILABLE alert. This typically happens after NLP (Android's Network Location Provider) is disabled.
    // (https://developer.android.com/training/location/geofencing.html #Re-register geofences only when required)
    private void startInternal(final Location homeLocation, PendingIntent pendingIntent) {
        GeofencingRequest geofenceRequest = createGeofenceRequest(homeLocation);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ToastLog.logShort(context, TAG, "Starting Play geofencing...");

            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    geofenceRequest,
                    pendingIntent
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.getStatusMessage() != null) {
                        ToastLog.warnShort(context, TAG, status.getStatusMessage());
                    }
                }
            });
        }
    }

    @NonNull
    private GeofencingRequest createGeofenceRequest(Location location) {

        List<Geofence> fences = new ArrayList<>();
        // TODO: for multiple home support different requestIds for different homes have to be used!
        if (enterRadius == exitRadius) {
            fences.add(new Geofence.Builder()
                    .setRequestId(GEO_BOTH_LISTENER_KEY)
                    .setCircularRegion(
                            location.getLatitude(),
                            location.getLongitude(),
                            (float)enterRadius
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(20 * 1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            );
        } else {
            fences.add(new Geofence.Builder()
                    .setRequestId(GEO_ENTER_LISTENER_KEY)
                    .setCircularRegion(
                            location.getLatitude(),
                            location.getLongitude(),
                            (float)enterRadius
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(20 * 1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            );
            fences.add(new Geofence.Builder()
                    .setRequestId(GEO_EXIT_LISTENER_KEY)

                    .setCircularRegion(
                            location.getLatitude(),
                            location.getLongitude(),
                            (float)exitRadius
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(20 * 1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            );
        }

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // we do not use ANY initial trigger, because this causes the strange behavior that the app
        // could deactivate the secure/alarm mode automatically when the app is launched, due to
        // the reason that the user is currently at home. This should not happen because of:
        // a) The user should be able to use the security system  while he is sleeping in the bed room
        // b) DEMO show cases
        // Also, this worked only reliable for ENTER.
        int init = 0; // set ZERO because ENTER is the default!
        if (initTrigger)
            init = GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT;
        builder.setInitialTrigger(init);

        builder.addGeofences(fences);

        return builder.build();
    }

    @Override
    public void stop() {
        ToastLog.logShort(context, TAG, "Stopping Play geofencing...");
        PendingIntent pendingIntent = getGeofencingPendingIntent(context);
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, pendingIntent);

        TimedGPSFixReceiver.stop(context);
    }

    @Override
    public String getName() {
        return "Play Geofencing";
    }

    private static PendingIntent getGeofencingPendingIntent(final Context context) {
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences()
        // Important: Use a intent with named action, that has assigned max. priority
        Intent intent = new Intent("de.bsautermeister.geofencer.geo.ACTION_RECEIVE_GEOFENCE");
        return PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
