package de.bsautermeister.geofencer.geo;

import android.location.Location;

public interface GeofenceProvider {
    void start(Location homeLocation, double enterRadius, double exitRadius, boolean usePolling);
    void stop();
    String getName();
}
