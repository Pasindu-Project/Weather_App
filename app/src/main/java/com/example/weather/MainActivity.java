package com.example.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SETTINGS = 1;
    private String location;
    private String unit;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve settings from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        location = prefs.getString(SettingsActivity.KEY_LOCATION, "Colombo");
        unit = prefs.getString(SettingsActivity.KEY_UNIT, "C");

        // Fetch data using the default location and unit
        new FetchData().execute();

        // Set up UI for user sign-in
        TextView textView = findViewById(R.id.name);
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userName = user.getDisplayName();
            textView.setText("Welcome, " + userName);
        } else {
            textView.setText("Please sign in.");
        }

        // Set up logout button
        Button signOutButton = findViewById(R.id.logout_button);
        signOutButton.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> signOutAndStartSignInActivity())
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(settingsIntent, REQUEST_CODE_SETTINGS);
            return true;
        } else if (id == R.id.action_about) {
            Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
            if (data != null) {
                location = data.getStringExtra(SettingsActivity.KEY_LOCATION);
                unit = data.getStringExtra(SettingsActivity.KEY_UNIT);

                // Refresh data with updated settings
                new FetchData().execute();
            }
        }
    }

    private void signOutAndStartSignInActivity() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Optional: Update UI or show a message to the user
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private class FetchData extends AsyncTask<String, Void, String> {
        private BufferedReader reader;

        @Override
        protected String doInBackground(String... strings) {
            try {
                // Build the URL with the updated location and unit
                final String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast?q=" + location + "&cnt=20&appid=8b218f3582289bbb003350b6ef27c24f&units=" + (unit.equals("C") ? "metric" : "imperial");
                URL url = new URL(BASE_URL);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                return buffer.length() == 0 ? null : buffer.toString();

            } catch (IOException e) {
                Log.e(TAG, "Error", e);
                return null;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject weatherObject = new JSONObject(result);
                    JSONArray dataList = weatherObject.getJSONArray("list");

                    String[] date_list = new String[dataList.length()];
                    String[] temp_list = new String[dataList.length()];
                    String[] humidity_list = new String[dataList.length()]; // Add humidity list
                    Integer[] icon_list = new Integer[dataList.length()];

                    for (int i = 0; i < dataList.length(); i++) {
                        JSONObject valueObject = dataList.getJSONObject(i);
                        date_list[i] = valueObject.getString("dt_txt");

                        JSONObject mainObject = valueObject.getJSONObject("main");
                        temp_list[i] = mainObject.getString("temp");
                        humidity_list[i] = mainObject.getString("humidity"); // Extract humidity

                        JSONArray weatherArray = valueObject.getJSONArray("weather");
                        JSONObject weatherArrayObject = weatherArray.getJSONObject(0);
                        icon_list[i] = getApplicationContext().getResources().getIdentifier("pic_" + weatherArrayObject.getString("icon"), "drawable", getApplicationContext().getPackageName());
                    }

                    CustomListAdapter adapter = new CustomListAdapter(MainActivity.this, date_list, temp_list, humidity_list, icon_list, unit);
                    ListView listView = findViewById(R.id.list_view);
                    listView.setAdapter(adapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
