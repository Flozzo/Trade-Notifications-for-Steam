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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.oxapps.tradenotifications.main.MainActivity;
import com.oxapps.tradenotifications.model.ApplicationSettings;
import com.oxapps.tradenotifications.model.ApplicationSettingsImpl;
import com.oxapps.tradenotifications.model.BackgroundTaskScheduler;
import com.oxapps.tradenotifications.model.ConnectionUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BackgroundTaskService extends GcmTaskService {

    private static final String TAG = "BackgroundTaskService";

    public static final String LAST_DELETE_KEY = "lastDelete";
    public static final String LAST_CHECK_KEY = "lastChecked";
    private static final String TIME_CREATED_KEY = "time_created";
    private static final String OFFER_STATE_KEY = "trade_offer_state";
    public static final String NOTIFICATION_CLICKED = "clicked";
    private static final int OFFER_STATE_ACTIVE = 2;

    private String ORIGINAL_URL = "https://api.steampowered.com/IEconService/GetTradeOffers/v1/?key=";
    private String URL_OPTIONS_NEW_TRADES = "&format=json&get_sent_offers=0&get_received_offers=1&get_descriptions=0&active_only=1&historical_only=0";
    OkHttpClient client = new OkHttpClient();

    private ApplicationSettings applicationSettings;
    private ConnectionUtils connectionUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationSettings = new ApplicationSettingsImpl(this);
        connectionUtils = new ConnectionUtils(this);
    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        long delay = applicationSettings.getNotificationRefreshDelay();
        String apiKey = applicationSettings.getApiKey();
        if(apiKey.length() == 32 && delay != 0) {
            BackgroundTaskScheduler scheduler = new BackgroundTaskScheduler(this);
            scheduler.scheduleTask(delay);
        }
    }


    @Override
    public int onRunTask(TaskParams taskParams) {
        if(!connectionUtils.isInternetAvailable()) {
            stopSelf();
            return GcmNetworkManager.RESULT_FAILURE;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long lastDeleteTime = prefs.getLong(LAST_DELETE_KEY, 0);
        long lastCheckTime = prefs.getLong(LAST_CHECK_KEY, 0);
        long currentTime = System.currentTimeMillis() / 1000;

        String apiKey = applicationSettings.getApiKey();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(LAST_CHECK_KEY, currentTime);
        editor.apply();

        getReceivedOffers(apiKey, lastCheckTime, lastDeleteTime);

        return GcmNetworkManager.RESULT_SUCCESS;
    }



    private void removeNotification() {
        //No more trade offers so we don't want to be lying to the user
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void getReceivedOffers(String apiKey, long lastCheckTime, long lastDeleteTime) {
        String url = ORIGINAL_URL + apiKey + URL_OPTIONS_NEW_TRADES;

        int newOfferCount = 0;
        int newSinceLastCheckCount = 0;
        int totalOfferCount = 0;

        try {
            String json = getTradeOffers(url);
            if(json.equals("")) {
                return;
            }
            JSONObject response = new JSONObject(json).getJSONObject("response");
            if(response.length() == 0 ) {
                removeNotification();
                return;
            }
            JSONArray tradeOffers = response.getJSONArray("trade_offers_received");
            totalOfferCount = tradeOffers.length();
            for (int i = 0; i < totalOfferCount; i++) {
                JSONObject tradeOffer = tradeOffers.getJSONObject(i);
                long timeCreated = tradeOffer.getLong(TIME_CREATED_KEY);
                int state = tradeOffer.getInt(OFFER_STATE_KEY);
                if(timeCreated > lastDeleteTime) {
                    if(state == OFFER_STATE_ACTIVE) {
                        newOfferCount++;
                        if (timeCreated > lastCheckTime) {
                            newSinceLastCheckCount++;
                        }
                    }
                } else {
                    //They are in chronological order so if the first was too early the rest will be too.
                    break;
                }
            }
        } catch(IOException | JSONException exception) {
            exception.printStackTrace();
        }

        boolean vibrate = newSinceLastCheckCount > 0;
        showNewTradeNotification(totalOfferCount, newOfferCount, vibrate);
    }

    private String getTradeOffers(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        if(response.isSuccessful()) {
            return response.body().string();
        } else {
            if (response.code() == 403) {
                handleBadApiKeyError();
            }
            return "";
        }
    }

    private void showNewTradeNotification(int totalOfferCount, int newOfferCount, boolean vibrate) {
        String titleText = String.valueOf(newOfferCount) + " new trade offer" + ((newOfferCount == 1) ? "" : "s");
        String contentText = "You now have " + String.valueOf(totalOfferCount) + " active trade offer" + ((totalOfferCount == 1) ? "" : "s");

        Intent deleteIntent = new Intent(this, NotificationDeleteReceiver.class);
        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 5, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent clickIntent = new Intent(this, NotificationDeleteReceiver.class);
        clickIntent.putExtra(NOTIFICATION_CLICKED, "clicked");
        PendingIntent pendingContentIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 6, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notif_trade)
                .setContentTitle(titleText)
                .setContentText(contentText)
                .setAutoCancel(true)
                .setDeleteIntent(pendingDeleteIntent)
                .setContentIntent(pendingContentIntent);

        if(vibrate) {
            notificationBuilder.setVibrate(new long[]{300, 300, 300, 300, 300});
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(10101, notificationBuilder.build());
    }

    private void handleBadApiKeyError() {
        showErrorNotification();
        //Cancel scheduled task so we don't get 100s of error notifications
        GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(this);
        gcmNetworkManager.cancelAllTasks(BackgroundTaskService.class);
    }

    private void showErrorNotification() {
        Intent clickIntent = new Intent(this, MainActivity.class);
        clickIntent.putExtra(NOTIFICATION_CLICKED, "clicked");
        PendingIntent pendingContentIntent = PendingIntent.getActivity(this.getApplicationContext(), 6, clickIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notif_error)
                .setContentTitle("Error fetching trade offers")
                .setContentText("Steam API Key incorrect")
                .setAutoCancel(true)
                .setContentIntent(pendingContentIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(403, notificationBuilder.build());
    }
}
