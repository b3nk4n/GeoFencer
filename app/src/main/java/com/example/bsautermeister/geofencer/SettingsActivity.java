package com.example.bsautermeister.geofencer;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bsautermeister.geofencer.geo.GeoLocationProvider;
import com.example.bsautermeister.geofencer.geo.GeofenceSettings;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    public static final int MIN_RADIUS = 50;
    public static final int MAX_RADIUS = 1000;
    public static final int RADIUS_STEP = 25;

    private Spinner providerSpinner;

    private SeekBar radiusSeekBar;
    private TextView radiusText;
    private EditText latitudeText;
    private EditText longitudeText;

    private GeoLocationProvider geoLocationProvider;
    private GeofenceSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        geoLocationProvider = new GeoLocationProvider(getApplicationContext());
        settings = new GeofenceSettings(getApplicationContext());

        providerSpinner = (Spinner) findViewById(R.id.providerSpinner);
        ArrayAdapter<CharSequence> providerAdapter = ArrayAdapter.createFromResource(this,
                R.array.provider_array, android.R.layout.simple_spinner_item);
        providerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        providerSpinner.setAdapter(providerAdapter);
        providerSpinner.setSelection(settings.getGeofenceProvider().equals("Play") ? 0 : 1);

        radiusText = (TextView) findViewById(R.id.radiusText);
        radiusSeekBar = (SeekBar) findViewById(R.id.radiusSeekBar);
        radiusSeekBar.setMax(radiusToProgress(MAX_RADIUS));
        int radius = (int)settings.getRadius();
        radiusSeekBar.setProgress(radiusToProgress(radius));
        updateRadiusText(radius);
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                int radius = progressToRadius(progress);
                updateRadiusText(radius);
            }
        });

        latitudeText = (EditText)findViewById(R.id.latitudeText);
        longitudeText = (EditText)findViewById(R.id.longitudeText);
        updateHomeLocationText(settings.getHomeLocation());
    }

    @Override
    protected void onResume() {
        super.onResume();
        geoLocationProvider.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        geoLocationProvider.disconnect();
    }

    private void updateRadiusText(int radius) {
        radiusText.setText(String.valueOf(radius) + "m");
    }

    private void updateHomeLocationText(Location location) {
        if (location == null)
            return;

        latitudeText.setText(String.valueOf(location.getLatitude()));
        longitudeText.setText(String.valueOf(location.getLongitude()));
    }

    private int progressToRadius(int progress) {
        return MIN_RADIUS + progress * RADIUS_STEP;
    }

    private int radiusToProgress(int radius) {
        return (radius - MIN_RADIUS) / RADIUS_STEP;
    }

    public void loadCurrentPositionClicked(View view) {

        geoLocationProvider.setGeoLocationCallback(new GeoLocationProvider.GeoLocationCallback() {
            @Override
            public void locationUpdated(final Location location) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateHomeLocationText(location);

                        Toast.makeText(getApplicationContext(),
                                "Location loaded with accuracy: " + location.getAccuracy(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        if (!geoLocationProvider.tryRetrieveLocation()) {
            Toast.makeText(getApplicationContext(),
                    "Failed to get retieve your location.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void showCurrentPositionClicked(View view) {
        double latitude;
        double longitude;

        try {
            latitude = Double.valueOf(latitudeText.getText().toString());
            longitude = Double.valueOf(longitudeText.getText().toString());
        } catch (NumberFormatException ex) {
            latitude = Double.NaN;
            longitude = Double.NaN;
        }

        if (latitude == Double.NaN)
            return;

        Uri gmmIntentUri = Uri.parse(String.format(Locale.ENGLISH, "geo:%f,%f?z=14", latitude, longitude));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    public void saveClicked(View view) {
        settings.setGeofenceProvider(providerSpinner.getSelectedItem().toString());
        settings.setRadius(progressToRadius(radiusSeekBar.getProgress()));

        double latitude;
        double longitude;

        try {
            latitude = Double.valueOf(latitudeText.getText().toString());
            longitude = Double.valueOf(longitudeText.getText().toString());
        } catch (NumberFormatException ex) {
            latitude = Double.NaN;
            longitude = Double.NaN;
        }

        if (latitude == Double.NaN)
            return;

        settings.setHomeLocation(latitude, longitude);
        finish();
    }
}
