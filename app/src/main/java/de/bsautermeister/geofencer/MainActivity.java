package de.bsautermeister.geofencer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import de.bsautermeister.geofencer.geo.GeoLocationUtil;
import de.bsautermeister.geofencer.geo.GeofenceProvider;
import de.bsautermeister.geofencer.geo.GeofenceSettings;
import de.bsautermeister.geofencer.geo.PlayGeofenceProvider;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 0;

    private GeofenceSettings settings;

    private ProgressBar runningProgress;
    private TextView runningProvider;

    private FloatingActionButton startButton;
    private FloatingActionButton stopButton;

    private GeofenceProvider provider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = new GeofenceSettings(getApplicationContext());

        if (settings.getGeofenceProvider().equals("Play")) {
            provider = new PlayGeofenceProvider(getApplicationContext());
        } else if (settings.getGeofenceProvider().equals("PathSense")){
            // TODO
        }

        startButton = (FloatingActionButton)findViewById(R.id.startButton);
        stopButton = (FloatingActionButton)findViewById(R.id.stopButton);
        updateFloatingButtons();

        runningProgress = (ProgressBar)findViewById(R.id.runningProgress);
        runningProvider = (TextView)findViewById(R.id.runningProvider);
        updateRunningProgress();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GeoLocationUtil.askForLocationPermission(this, LOCATION_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                if (settings.isGeofencingActive()) {
                    Toast.makeText(this, "Stop geofencing first...", Toast.LENGTH_SHORT).show();
                    break;
                }

                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
            }
        }
    }

    private void updateFloatingButtons() {
        startButton.setVisibility(settings.isGeofencingActive() ? View.GONE : View.VISIBLE);
        stopButton.setVisibility(settings.isGeofencingActive() ? View.VISIBLE : View.GONE);
    }

    private void updateRunningProgress() {
        if (settings.isGeofencingActive()) {
            runningProgress.setVisibility(View.VISIBLE);
            runningProvider.setText(provider.getName());
        } else {
            runningProgress.setVisibility(View.INVISIBLE);
            runningProvider.setText("");
        }
    }

    public void startClicked(View view) {
        Location home = settings.getHomeLocation();
        float radius = settings.getRadius();
        boolean usePolling = settings.isGpsPollingEnabled();

        if (home == null) {
            Toast.makeText(this, "Set a home in settings first...", Toast.LENGTH_SHORT).show();
            return;
        }

        provider.start(home, radius, usePolling);
        settings.setGeofencingActive(true);
        updateFloatingButtons();
        updateRunningProgress();
    }

    public void stopClicked(View view) {
        provider.stop();
        settings.setGeofencingActive(false);
        updateFloatingButtons();
        updateRunningProgress();
    }
}
