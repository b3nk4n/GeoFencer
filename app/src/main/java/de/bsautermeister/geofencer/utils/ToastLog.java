package de.bsautermeister.geofencer.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ToastLog {
    public static void logShort(Context context, String tag, String msg) {
        Toast.makeText(context,
                msg,
                Toast.LENGTH_SHORT).show();
        Log.i(tag, msg);
    }

    public static void logLong(Context context, String tag, String msg) {
        Toast.makeText(context,
                msg,
                Toast.LENGTH_LONG).show();
        Log.i(tag, msg);
    }

    public static void warnShort(Context context, String tag, String msg) {
        Toast.makeText(context,
                msg,
                Toast.LENGTH_SHORT).show();
        Log.w(tag, msg);
    }

    public static void warnLong(Context context, String tag, String msg) {
        Toast.makeText(context,
                msg,
                Toast.LENGTH_LONG).show();
        Log.w(tag, msg);
    }
}
