package com.example.bsautermeister.geofencer.geo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class PlayGeofenceProvider implements GeofenceProvider {
    private static final String TAG = "PlayGeofenceProvider";

    private static final String GEO_ENTER_LISTENER_KEY = PlayGeofenceProvider.class.getName() + ".ENTER";
    //private static final String GEO_EXIT_LISTENER_KEY = PlayGeofenceProvider.class.getName() + ".EXIT";

    private Context context;
    private GoogleApiClient googleApiClient;
    private PendingIntent pendingIntent;

    private Location homeLocation;
    private double radius;

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

        googleApiClient.connect();
    }

    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            if (homeLocation != null) {
                startInternal(homeLocation, pendingIntent);

                homeLocation = null;
            }
        }

        @Override
        public void onConnectionSuspended(int i) {}
    };

    private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.w(TAG, "Google Play API failed " + connectionResult);
            // TODO handle all possible error codes
            switch (connectionResult.getErrorCode()) {
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED: // SERVICE_VERSION_UPDATE_REQUIRED
                    break;
            }
        }
    };

    @Override
    public void start(Location homeLocation, double radius, boolean usePolling) {
        this.homeLocation = homeLocation;
        this.radius = radius;

        if (GeoLocationUtil.hasGpsPermissions(context)) {
            PendingIntent pendingIntent = getGeofencingPendingIntent(context);
            Log.i(TAG, "Starting Play geofencing...");

            if (googleApiClient.isConnected()) {
                startInternal(homeLocation, pendingIntent);
            } else {
                // delay internal start (called via onConnected() callback)
                this.homeLocation = homeLocation;
                this.pendingIntent = pendingIntent;
            }

            if (usePolling)
                TimedGPSFixReceiver.start(context);
        }
    }

    // According to documentation there are cases when registered Geofences have to be re-registered, among others:
    // - The app has received a GEOFENCE_NOT_AVAILABLE alert. This typically happens after NLP (Android's Network Location Provider) is disabled.
    // (https://developer.android.com/training/location/geofencing.html #Re-register geofences only when required)
    private  void startInternal(final Location homeLocation, PendingIntent pendingIntent) {
        GeofencingRequest geofenceRequest = createGeofenceRequest(homeLocation);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    geofenceRequest,
                    pendingIntent
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    Log.i(TAG, String.format("Set Geofence to (%f, %f) result: %s",
                        homeLocation.getLatitude(),
                        homeLocation.getLongitude(),
                        status.getStatusMessage()));
                }
            });
        }
    }

    @NonNull
    private GeofencingRequest createGeofenceRequest(Location location) {

        List<Geofence> fences = new ArrayList<>();
        // TODO: for multiple home support different requestIds for different homes have to be used!
        fences.add(new Geofence.Builder()
                .setRequestId(GEO_ENTER_LISTENER_KEY)

                .setCircularRegion(
                        location.getLatitude(),
                        location.getLongitude(),
                        (float)radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(20 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        );
        /*fences.add(new Geofence.Builder()
                .setRequestId(GEO_EXIT_LISTENER_KEY)

                .setCircularRegion(
                        location.getLatitude(),
                        location.getLongitude(),
                        EXIT_RADIUS_IN_METER
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(20 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        );*/

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // we do not use ANY initial trigger, because this causes the strange behavior that the app
        // could deactivate the secure/alarm mode automatically when the app is launched, due to
        // the reason that the user is currently at home. This should not happen because of:
        // a) The user should be able to use the security system  while he is sleeping in the bed room
        // b) DEMO show cases
        // Also, this worked only reliable for ENTER.
        builder.setInitialTrigger(0); // set zero or DWELL because ENTER is the default!

        builder.addGeofences(fences);

        return builder.build();
    }

    @Override
    public void stop() {
        Log.i(TAG, "Stopping Play geofencing...");
        PendingIntent pendingIntent = getGeofencingPendingIntent(context);
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, pendingIntent);

        TimedGPSFixReceiver.stop(context);
    }

    private static PendingIntent getGeofencingPendingIntent(final Context context) {
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences()
        Intent intent = new Intent(context.getApplicationContext(), GeoTransitionReceiver.class);
        return PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
