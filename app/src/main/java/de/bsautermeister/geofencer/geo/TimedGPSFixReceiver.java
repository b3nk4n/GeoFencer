package de.bsautermeister.geofencer.geo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class TimedGPSFixReceiver extends BroadcastReceiver {
    private static final String TAG = "TimedGPSFixReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!GeoLocationUtil.isGpsEnabled(context) || !GeoLocationUtil.hasGpsPermissions(context))
            return;

        GeoLocationProvider geoLocationProvider = new GeoLocationProvider(context);
        geoLocationProvider.setGeoLocationCallback(new GeoLocationProvider.GeoLocationCallback() {
            @Override
            public void locationUpdated(Location location) {
                GeofenceSettings settings = new GeofenceSettings(context);
                Location home = settings.getHomeLocation();
                double distance = -1;
                if (home != null)
                    distance = home.distanceTo(location);

                String accuracyString = (location.hasAccuracy() ? String.valueOf(location.getAccuracy()) : "?");
                Log.d(TAG, String.format("GPS: Acc: %s; Dist: %.2f", accuracyString, distance));
                Toast.makeText(context, accuracyString, Toast.LENGTH_LONG).show();
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
