package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DayViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view);

        Toolbar toolbar = findViewById(R.id.toolbar_day_view);
        setSupportActionBar(toolbar);
        // Enable the Up button in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Weather Details");
        }
        Intent intent = getIntent();

        TextView txtDate = findViewById(R.id.txtDate2);
        TextView txtTemp = findViewById(R.id.txtTemp2);
        TextView txtHumidity = findViewById(R.id.txtHumidity2);
        ImageView imgIcon = findViewById(R.id.imgIcon2);

        if (intent != null) {
            String date = intent.getStringExtra("date");
            String temperature = intent.getStringExtra("temperature");
            String humidity = intent.getStringExtra("humidity");
            int iconResId = intent.getIntExtra("icon", -1); // Default to -1 if not found
            String unit = intent.getStringExtra("unit");

            txtDate.setText(date != null ? date : "No date available");
            txtTemp.setText(temperature != null ? temperature + (unit.equals("C") ? " °C" : " °F") : "No temperature available");
            txtHumidity.setText(humidity != null ? "Humidity: " + humidity + "%" : "No humidity available");
            imgIcon.setImageResource(iconResId != -1 ? iconResId : R.drawable.pic_01d); // Default icon
        }
    }
}
