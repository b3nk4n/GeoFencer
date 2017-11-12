package de.bsautermeister.geofencer.geo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;

import java.util.Locale;

import de.bsautermeister.geofencer.utils.ToastLog;

public class TimedGPSFixReceiver extends BroadcastReceiver {
    private static final String TAG = "TimedGPSFixReceiver";

    private GeofenceSettings settings;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!GeoLocationUtil.isGpsEnabled(context) || !GeoLocationUtil.hasGpsPermissions(context))
            return;

        settings = new GeofenceSettings(context);

        if (!settings.isGpsPollingEnabled())
            return;

        if (settings.getGpsPollingImplementation().equals("LocationManager")) {
            pollUsingLocationManger(context);
        } else {
            pollUsingFusedLocationManager(context);
        }

        // TODO: maybe it is better to use the old-school LocationManager here without any GooglePlay Services connection constraints?
        pollUsingFusedLocationManager(context);
    }

    private void notifyLocationUpdated(Location location, Context context) {
        Location home = settings.getHomeLocation();
        double distance = -1;
        if (home != null)
            distance = home.distanceTo(location);

        String accuracyString = (location.hasAccuracy() ? String.valueOf(location.getAccuracy()) : "?");
        String message = String.format(Locale.getDefault(), "Accuracy: %s Distance: %.2f", accuracyString, distance);
        ToastLog.logLong(context, TAG, message);
    }

    private void pollUsingFusedLocationManager(final Context context) {
        ToastLog.logShort(context, TAG, "Timed fix: FusedLocationApi");

        GeoLocationProvider geoLocationProvider = new GeoLocationProvider(context);
        geoLocationProvider.connect(); // FIXME do we have to disconnect at the end?
        geoLocationProvider.setGeoLocationCallback(new GeoLocationProvider.GeoLocationCallback() {
            @Override
            public void locationUpdated(Location location) {
                notifyLocationUpdated(location, context);
            }
        });
        geoLocationProvider.tryRetrieveLocation();
    }

    private void pollUsingLocationManger(final Context context) {
        ToastLog.logShort(context, TAG, "Timed fix: LocationManager");

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    notifyLocationUpdated(location, context);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            }, null);
        } catch (SecurityException sex) {
            ToastLog.logLong(context, TAG, "SecurityException: " + sex.getMessage());
        }
    }

    public static void start(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TimedGPSFixReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000L, 60000L, pi);
    }

    public static void stop(Context context)
    {
        Intent intent = new Intent(context, TimedGPSFixReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
