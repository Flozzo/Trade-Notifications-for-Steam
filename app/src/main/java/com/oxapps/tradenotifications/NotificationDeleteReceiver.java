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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.oxapps.tradenotifications.model.ApplicationSettings;
import com.oxapps.tradenotifications.model.ApplicationSettingsImpl;
import com.oxapps.tradenotifications.model.IntentConsts;

public class NotificationDeleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ApplicationSettings applicationSettings = new ApplicationSettingsImpl(context);

        long newRequestTime = System.currentTimeMillis() / 1000;
        long lastCheckTime = applicationSettings.getLastCheckTime();
        if(lastCheckTime == 0) {
            lastCheckTime = newRequestTime;
        }
        applicationSettings.setLastDeleteTime(lastCheckTime);

        if(intent.getBooleanExtra(IntentConsts.NOTIFICATION_CLICKED, false)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            String username = applicationSettings.getUsername();
            boolean isProfile = applicationSettings.isProfileUrl();
            String identifier = isProfile ? "profile/" : "id/";
            String url = "https://steamcommunity.com/" + identifier + username + "/tradeoffers";
            browserIntent.setData(Uri.parse(url));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(browserIntent);
        }
    }
}
