package de.bsautermeister.geofencer.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    public static String getTimestamptString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        java.util.Date currenTimeZone=new java.util.Date(System.currentTimeMillis());
        return sdf.format(currenTimeZone);
    }
}
