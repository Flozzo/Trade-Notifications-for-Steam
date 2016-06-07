/*
 * Copyright 2016 Flynn van Os
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class MainActivity extends AppCompatActivity implements DelayDialogFragment.OnDelaySetListener {

    private static final String API_KEY_URL = "https://steamcommunity.com/dev/apikey";
    private static final String TRADE_URL = "https://steamcommunity.com/tradeoffer/new/?partner=72233084&token=NWnuxGBb";
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
        apiKeyButton = (Button) findViewById(R.id.button_get_api_key);
        if(!apiKey.equals("") && apiKey.length() == 32) {
            //Prevent accidental editing of API Key
            apiKeyView.setText(apiKey);
            apiKeyLocked = true;
            apiKeyView.setFocusable(false);
            apiKeyView.setEnabled(false);
            apiKeyButton.setText(R.string.api_key_edit);
        }
        delayView = (TextView) findViewById(R.id.tv_delay);
        delay = prefs.getLong(PREFS_KEY_DELAY, 900000);
        delayView.setText(getDelayText(delay));
    }

    private String getDelayText(long delay) {
        if(delay == 0) {
            return getString(R.string.disabled);
        } else if (delay < (120 * 60000)) {
            return getString(R.string.delay_min, delay / 60000);
        } else {
            return getString(R.string.delay_hours, delay / 3600000);
        }
    }

    @Override
    protected void onPause() {
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
            if(delay == 0) {
                alarmManager.cancel(pendingIntent);
            } else {
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime(),
                        delay, pendingIntent);
            }
        }
    }

    public void getApiKey(View v) {
        if(apiKeyLocked) {
            apiKeyView.setFocusableInTouchMode(true);
            apiKeyButton.setText(R.string.api_key_get);
            apiKeyLocked = false;
            apiKeyView.setEnabled(true);
        } else {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(API_KEY_URL));
        }

    }

    public void showIntervalDialog(View v) {
        DelayDialogFragment ddf = new DelayDialogFragment();
        ddf.setOnDelaySetListener(MainActivity.this);
        ddf.show(getSupportFragmentManager(), "tag");
    }

    @Override public void onDelaySet(String text, long delay) {
        delayView.setText(text);
        this.delay = delay;
    }

    public void onDonationClicked(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(TRADE_URL));
        startActivity(browserIntent);
    }
}
