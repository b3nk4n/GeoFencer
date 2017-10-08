package de.bsautermeister.geofencer.geo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.Nullable;

public class GeofenceSettings {

    public static final String DEFAULT_PROVIDER = "Play";
    public static final float DEFAULT_RADIUS = 100.0f;

    private SharedPreferences prefs;

    public GeofenceSettings(Context appContext) {
        prefs = appContext.getSharedPreferences("de.bsautermeister.geofencer",
                                                Context.MODE_PRIVATE);
    }

    public float getEnterRadius() {
        return prefs.getFloat("radius-enter", DEFAULT_RADIUS);
    }

    public void setEnterRadius(float radius) {
        prefs.edit().putFloat("radius-enter", radius).apply();
    }

    public float getExitRadius() {
        return prefs.getFloat("radius-exit", DEFAULT_RADIUS);
    }

    public void setExitRadius(float radius) {
        prefs.edit().putFloat("radius-exit", radius).apply();
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

    public void setHomeLocation(double lat, double lng) {
        Location homeLocation = new Location("");
        homeLocation.setLatitude(lat);
        homeLocation.setLongitude(lng);
        setHomeLocation(homeLocation);
    }

    public void setHomeLocation(Location location) {
        prefs.edit().putFloat("lat", (float)location.getLatitude())
                    .putFloat("lng", (float)location.getLongitude())
                    .apply();
    }

    public String getGeofenceProvider() {
        return prefs.getString("provider", DEFAULT_PROVIDER);
    }

    public void setGeofenceProvider(String provider) {
        prefs.edit().putString("provider", provider).apply();
    }

    public boolean isGpsPollingEnabled() {
        return prefs.getBoolean("gps-polling", false);
    }

    public void setGpsPollingEnabled(boolean polling) {
        prefs.edit().putBoolean("gps-polling", polling).apply();
    }

    public boolean isGeofencingActive() {
        return prefs.getBoolean("active", false);
    }

    public void setGeofencingActive(boolean active) {
        prefs.edit().putBoolean("active", active).apply();
    }
}
