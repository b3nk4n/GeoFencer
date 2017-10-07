package com.example.bsautermeister.geofencer.geo;

import android.location.Location;

public interface GeofenceProvider {
    void start(Location homeLocation, double radius);
    void stop();
}
