package com.oxapps.tradenotifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SetDelayDialogFragment.OnDelaySetListener {

    private static final String API_KEY_URL = "https://steamcommunity.com/dev/apikey";
    public static final String PREFS_KEY_API_KEY = "api_key";
    public static final String PREFS_KEY_DELAY = "delay";
    private EditText apiKeyView;
    private SharedPreferences prefs;
    private TextView delayView;
    private Button apiKeyButton;
    private long delay;
    private boolean apiKeyLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: Prefetch using Chrome Custom tabs instead of this way
        initViews();
    }

    private void initViews() {
        apiKeyView = (EditText) findViewById(R.id.et_api_key);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String apiKey = prefs.getString(PREFS_KEY_API_KEY, "");
        if(!apiKey.equals("") && apiKey.length() == 32) {
            //Prevent accidental editing of API Key
            apiKeyView.setText(apiKey);
            apiKeyLocked = true;
            apiKeyView.setFocusable(false);
            apiKeyView.setEnabled(false);
            apiKeyButton = (Button) findViewById(R.id.button_get_api_key);
            apiKeyButton.setText("Edit API Key");
        }
        delayView = (TextView) findViewById(R.id.tv_delay);
        delay = prefs.getLong(PREFS_KEY_DELAY, 900000);
        delayView.setText(String.valueOf(delay / 60000) + " minutes");
    }

    @Override
    protected void onPause() {
        //only set if not already set
        super.onPause();
        String apiKey = apiKeyView.getText().toString().trim();
        if(apiKey.length() == 32) {
            //Valid API Key, supposedly
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREFS_KEY_API_KEY, apiKey);
            editor.apply();

            Intent backgroundIntent = new Intent(this, BackgroundIntentService.class);
            backgroundIntent.putExtra(PREFS_KEY_API_KEY, apiKey);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, backgroundIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    delay, pendingIntent);
        }
    }

    public void getApiKey(View v) {
        if(apiKeyLocked) {
            apiKeyView.setFocusableInTouchMode(true);
            apiKeyButton.setText("Get API Key");
            apiKeyLocked = false;
            apiKeyView.setEnabled(true);
        } else {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(API_KEY_URL));
        }

    }

    public void showIntervalDialog(View v) {
        SetDelayDialogFragment sddf = new SetDelayDialogFragment();
        sddf.setOnDelaySetListener(MainActivity.this);
        sddf.show(getSupportFragmentManager(), "tag");
    }

    @Override public void onDelaySet(String text, long delay) {
        delayView.setText(text);
        this.delay = delay;
    }
}
