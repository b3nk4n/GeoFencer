package com.example.bsautermeister.geofencer.geo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by bsautermeister on 06.10.17.
 */

public class GeoLocationProvider {

    public interface GeoLocationCallback {
        void locationUpdated(Location location);
    }

    private static final String TAG = "GeoLocationProvider";

    private Context context;
    private GoogleApiClient googleApiClient;

    private volatile Location lastKnownUserLocation;

    private GeoLocationCallback geoLocationCallback;

    public GeoLocationProvider(Context appContext) {
        this.context = appContext;

        initGoogleApiClient();
    }

    private synchronized void initGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addApi(LocationServices.API)
                .build();
    }

    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG, "Google API connected for GPS location: " + bundle);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lastKnownUserLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            }

            if (lastKnownUserLocation != null && geoLocationCallback != null) {
                geoLocationCallback.locationUpdated(lastKnownUserLocation);
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

    void setGeoLocationCallback(GeoLocationCallback callback) {
        this.geoLocationCallback = callback;

        if (googleApiClient.isConnected() && geoLocationCallback != null) {
            Location lastLocation = null;
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            }

            if (lastLocation != null) {
                lastKnownUserLocation = lastLocation;
            }
            // FIXME bug here?! do not call this when lastKnown has not changed !?
            geoLocationCallback.locationUpdated(lastKnownUserLocation);
        }
    }
}
