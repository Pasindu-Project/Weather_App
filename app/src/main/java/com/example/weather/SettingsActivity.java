package com.example.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "WeatherPrefs";
    public static final String KEY_UNIT = "unit";
    public static final String KEY_LOCATION = "location";

    private EditText locationInput;
    private Switch unitSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        locationInput = findViewById(R.id.locationInput);
        unitSwitch = findViewById(R.id.unitSwitch);
        Button saveButton = findViewById(R.id.saveButton);

        // Load saved preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        locationInput.setText(prefs.getString(KEY_LOCATION, "Colombo,lk"));
        boolean isCelsius = "C".equals(prefs.getString(KEY_UNIT, "C"));
        unitSwitch.setChecked(isCelsius);
        unitSwitch.setText(isCelsius ? "Celsius (C)" : "Fahrenheit (F)");

        unitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            unitSwitch.setText(isChecked ? "Celsius (C)" : "Fahrenheit (F)");
        });

        saveButton.setOnClickListener(v -> {
            // Save preferences
            SharedPreferences.Editor editor = prefs.edit();
            String location = locationInput.getText().toString().trim().replaceAll(" ", ",");
            editor.putString(KEY_LOCATION, location);
            editor.putString(KEY_UNIT, unitSwitch.isChecked() ? "C" : "F");
            editor.apply();

            // Return result to MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra(KEY_LOCATION, location);
            resultIntent.putExtra(KEY_UNIT, unitSwitch.isChecked() ? "C" : "F");
            setResult(RESULT_OK, resultIntent);
            finish();  // Close SettingsActivity
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
