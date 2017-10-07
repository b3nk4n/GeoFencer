package de.bsautermeister.geofencer.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    public static String getTimestamptString() {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        java.util.Date currenTimeZone=new java.util.Date((long)1379487711*1000);
        return sdf.format(currenTimeZone);
    }
}
