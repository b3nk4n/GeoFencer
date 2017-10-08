package de.bsautermeister.geofencer.geo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.SystemClock;

import java.util.Locale;

import de.bsautermeister.geofencer.utils.ToastLog;

public class TimedGPSFixReceiver extends BroadcastReceiver {
    private static final String TAG = "TimedGPSFixReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!GeoLocationUtil.isGpsEnabled(context) || !GeoLocationUtil.hasGpsPermissions(context))
            return;

        // TODO: maybe it is better to use the old-school LocationManager here without any GooglePlay Services connection constraints?
        GeoLocationProvider geoLocationProvider = new GeoLocationProvider(context);
        geoLocationProvider.connect(); // FIXME do we have to disconnect at the end?
        geoLocationProvider.setGeoLocationCallback(new GeoLocationProvider.GeoLocationCallback() {
            @Override
            public void locationUpdated(Location location) {
                GeofenceSettings settings = new GeofenceSettings(context);
                Location home = settings.getHomeLocation();
                double distance = -1;
                if (home != null)
                    distance = home.distanceTo(location);

                String accuracyString = (location.hasAccuracy() ? String.valueOf(location.getAccuracy()) : "?");
                String message = String.format(Locale.getDefault(), "Accuracy: %s Distance: %.2f", accuracyString, distance);
                ToastLog.logLong(context, TAG, message);
            }
        });
        geoLocationProvider.tryRetrieveLocation();
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
