package com.example.remoteconfig;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            // register listener
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            String j;
            String js;
            try {
                // only working with bool or string
                if (s.equals("auto-accept") || s.equals("auto-reconnect") || s.equals("send-mgrs")) {
                    boolean value = sharedPreferences.getBoolean(s, false);
                    j = "JsonStr={\"" + s + "\" : \"" + String.valueOf(value) + "\"}";
                    js = URLEncoder.encode(j, StandardCharsets.UTF_8.name());
                } else {
                    String value = sharedPreferences.getString(s, "could not retrieve settings");
                    j = "JsonStr={\""+ s + "\" : \"" + value + "\"}";
                    js = URLEncoder.encode(j, StandardCharsets.UTF_8.name());
                }
                // make request on thread to avoid RequestOnMain Thread errors
                new Thread(() -> changeSettings(js)).start();


            }catch(UnsupportedEncodingException e) {
                e.printStackTrace();

            }
        }

        private void changeSettings(String js) {

                    try{
                    // TODO make this dynamic so that i can add any ip address and port
                    URL url = new URL("http://192.168.1.91:8001/config");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");

                    // send data with post request
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = js.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    // read response
                    String input;
                    StringBuilder sb = new StringBuilder();

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    while ((input = in.readLine()) != null) {
                        sb.append(input);
                    }

                    // shut down buffered reader and http connection
                    connection.disconnect();
                    in.close();

                } catch(IOException e){
                    e.printStackTrace();
                }
        }
    }
}