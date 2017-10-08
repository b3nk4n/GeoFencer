package de.bsautermeister.geofencer.geo;

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

public class GeoLocationProvider {

    public interface GeoLocationCallback {
        void locationUpdated(Location location);
    }

    private static final String TAG = "GeoLocationProvider";

    private Context context;
    private GoogleApiClient googleApiClient;

    //private volatile Location lastKnownUserLocation;

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
            Location lastLocation = getLastKnowLocation();

            if (lastLocation != null && geoLocationCallback != null && locationCalllbackPending) {
                locationCalllbackPending = false;
                geoLocationCallback.locationUpdated(lastLocation);
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

    public void setGeoLocationCallback(GeoLocationCallback callback) {
        this.geoLocationCallback = callback;
    }

    public Location getLastKnowLocation() {
        if (googleApiClient.isConnected() &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }
        return null;
    }

    private volatile boolean locationCalllbackPending = false;
    public boolean tryRetrieveLocation() {
        if (googleApiClient.isConnected() && geoLocationCallback != null) {
            Location lastLocation = getLastKnowLocation();

            if (lastLocation != null) {
                geoLocationCallback.locationUpdated(lastLocation);
                return true;
            }
        } else {
            locationCalllbackPending = true;
        }
        return false;
    }

    public void connect() {
        if (!googleApiClient.isConnected() || !googleApiClient.isConnecting())
            googleApiClient.connect();
    }

    public void disconnect() {
        if (googleApiClient.isConnected() || googleApiClient.isConnecting())
            this.googleApiClient.disconnect();
    }
}
