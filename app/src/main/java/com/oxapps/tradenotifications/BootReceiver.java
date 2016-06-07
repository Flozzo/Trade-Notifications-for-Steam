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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String apiKey = prefs.getString(MainActivity.PREFS_KEY_API_KEY, "");
            long delay = prefs.getLong(MainActivity.PREFS_KEY_DELAY, 900000);
            if(apiKey.length() == 32) {
                Intent backgroundIntent = new Intent(context.getApplicationContext(), BackgroundIntentService.class);
                backgroundIntent.putExtra(MainActivity.PREFS_KEY_API_KEY, apiKey);
                PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, backgroundIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime(),
                        delay, pendingIntent);
            }
        }
    }
}
