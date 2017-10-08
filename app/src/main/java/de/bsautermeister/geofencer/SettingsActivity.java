package de.bsautermeister.geofencer;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import de.bsautermeister.geofencer.geo.GeoLocationProvider;
import de.bsautermeister.geofencer.geo.GeofenceSettings;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private static final int PLACE_PICKER_REQUEST = 1;

    public static final int MIN_RADIUS = 50;
    public static final int MAX_RADIUS = 1000;
    public static final int RADIUS_STEP = 25;

    private Spinner providerSpinner;

    private SeekBar radiusEnterSeekBar;
    private TextView radiusEnterText;
    private SeekBar radiusExitSeekBar;
    private TextView radiusExitText;
    private EditText latitudeText;
    private EditText longitudeText;
    private CheckBox pollingCheckBox;
    private CheckBox initialTriggerCheckBox;

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

        int enterRadius = (int)settings.getEnterRadius();
        radiusEnterText = (TextView) findViewById(R.id.radiusEnterText);
        radiusEnterSeekBar = (SeekBar) findViewById(R.id.radiusEnterSeekBar);
        initSeekBar(radiusEnterSeekBar, radiusEnterText, enterRadius);

        int exitRadius = (int)settings.getExitRadius();
        radiusExitText = (TextView) findViewById(R.id.radiusExitText);
        radiusExitSeekBar = (SeekBar) findViewById(R.id.radiusExitSeekBar);
        initSeekBar(radiusExitSeekBar, radiusExitText, exitRadius);

        latitudeText = (EditText)findViewById(R.id.latitudeText);
        longitudeText = (EditText)findViewById(R.id.longitudeText);
        updateHomeLocationText(settings.getHomeLocation());

        pollingCheckBox = (CheckBox)findViewById(R.id.pollingCheckBox);
        pollingCheckBox.setChecked(settings.isGpsPollingEnabled());

        initialTriggerCheckBox = (CheckBox)findViewById(R.id.initialTriggerCheckBox);
        initialTriggerCheckBox.setChecked(settings.isInitialTriggerEnabled());
    }

    private static void initSeekBar(SeekBar seekBar, final TextView textView, int radius) {
        seekBar.setMax(radiusToProgress(MAX_RADIUS));
        seekBar.setProgress(radiusToProgress(radius));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
                updateRadiusText(textView, radius);
            }
        });
        updateRadiusText(textView, radius);
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

    private static void updateRadiusText(TextView textView, int radius) {
        textView.setText(String.valueOf(radius) + "m");
    }

    private void updateHomeLocationText(Location location) {
        if (location == null)
            return;

        latitudeText.setText(String.valueOf(location.getLatitude()));
        longitudeText.setText(String.valueOf(location.getLongitude()));
    }

    private static int progressToRadius(int progress) {
        return MIN_RADIUS + progress * RADIUS_STEP;
    }

    private static int radiusToProgress(int radius) {
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
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        if (!geoLocationProvider.tryRetrieveLocation()) {
            Toast.makeText(getApplicationContext(),
                    "Failed to get retieve your location.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void pickLocationClicked(View view) {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Play services not available.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, intent);
                Location location = new Location("");
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
                updateHomeLocationText(location);
            }
        }
    }

    public void showCurrentPositionClicked(View view) {
        Location location = getLocationFromUi();
        if (location == null)
            return;

        Uri gmmIntentUri = Uri.parse(String.format(Locale.ENGLISH, "geo:%f,%f?z=14",
                location.getLatitude(),
                location.getLongitude()));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    public void saveClicked(View view) {
        Location location = getLocationFromUi();
        if (location == null) {
            Toast.makeText(getApplicationContext(),
                    "Failed to save.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        settings.setGeofenceProvider(providerSpinner.getSelectedItem().toString());
        settings.setEnterRadius(progressToRadius(radiusEnterSeekBar.getProgress()));
        settings.setExitRadius(progressToRadius(radiusExitSeekBar.getProgress()));
        settings.setHomeLocation(location.getLatitude(), location.getLongitude());
        settings.setGpsPollingEnabled(pollingCheckBox.isChecked());
        settings.setInitialTriggerEnabled(initialTriggerCheckBox.isChecked());
        finish();
    }

    private Location getLocationFromUi() {
        try {
            double latitude = Double.valueOf(latitudeText.getText().toString());
            double longitude = Double.valueOf(longitudeText.getText().toString());
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            return location;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
