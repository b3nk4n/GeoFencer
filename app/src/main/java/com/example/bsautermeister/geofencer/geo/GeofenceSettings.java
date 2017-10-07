package com.example.bsautermeister.geofencer.geo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.Nullable;

public class GeofenceSettings {

    public static final String DEFAULT_PROVIDER = "Play";
    public static final float DEFAULT_RADIUS = 100.0f;

    private SharedPreferences prefs;

    public GeofenceSettings(Context appContext) {
        prefs = appContext.getSharedPreferences("com.example.bsautermeister.geofencer",
                                                Context.MODE_PRIVATE);
    }

    public float getRadius() {
        return prefs.getFloat("radius", DEFAULT_RADIUS);
    }

    public void setRadius(float radius) {
        prefs.edit().putFloat("radius", radius).apply();
    }

    @Nullable
    public Location getHomeLocation() {
        float lat = prefs.getFloat("lat", -1.0f);
        float lng = prefs.getFloat("lng", -1.0f);

        if (lat == -1.0f || lng == -1.0f)
            return null;

        Location homeLocation = new Location("");
        homeLocation.setLatitude(lat);
        homeLocation.setLongitude(lng);
        return homeLocation;
    }

    public void setHomeLocation(Location location) {
        prefs.edit().putFloat("lat", (float)location.getLatitude())
                    .putFloat("lng", (float)location.getLongitude())
                    .apply();
    }

    @Nullable
    public String getGeofenceProvider() {
        return prefs.getString("provider", DEFAULT_PROVIDER);
    }

    public void setGeofenceProvider(String provider) {
        prefs.edit().putString("provider", provider).apply();
    }
}
