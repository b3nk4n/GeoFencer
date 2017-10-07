package de.bsautermeister.geofencer.geo;

import android.location.Location;

public interface GeofenceProvider {
    void start(Location homeLocation, double radius, boolean usePolling);
    void stop();
}
