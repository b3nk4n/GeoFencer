package com.example.bsautermeister.geofencer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.bsautermeister.geofencer.geo.GeofenceSettings;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinner;

    private SeekBar radiusSeekBar;
    private TextView radiusText;
    public static final int MIN_RADIUS = 50;
    public static final int MAX_RADIUS = 1000;
    public static final int RADIUS_STEP = 25;

    private GeofenceSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = new GeofenceSettings(getApplicationContext());

        spinner = (Spinner) findViewById(R.id.providerSpinner);
        ArrayAdapter<CharSequence> providerAdapter = ArrayAdapter.createFromResource(this,
                R.array.provider_array, android.R.layout.simple_spinner_item);
        providerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(providerAdapter);

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
    }

    private void updateRadiusText(int radius) {
        radiusText.setText(String.valueOf(radius) + "m");
    }

    private int progressToRadius(int progress) {
        return MIN_RADIUS + progress * RADIUS_STEP;
    }

    private int radiusToProgress(int radius) {
        return (radius - MIN_RADIUS) / RADIUS_STEP;
    }
}
