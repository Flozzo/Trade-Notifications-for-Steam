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
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BackgroundIntentService extends IntentService {
    public static final String LAST_DELETE_KEY = "lastDelete";
    public static final String LAST_CHECK_KEY = "lastChecked";
    private static final String TIME_CREATED_KEY = "time_created";
    private static final String OFFER_STATE_KEY = "trade_offer_state";
    public static final String NOTIFICATION_CLICKED = "clicked";

    private String ORIGINAL_URL = "https://api.steampowered.com/IEconService/GetTradeOffers/v1/?key=";
    private String URL_OPTIONS = "&format=json&get_sent_offers=0&get_received_offers=1&get_descriptions=0&active_only=1&historical_only=0";
    OkHttpClient client = new OkHttpClient();



    public BackgroundIntentService() {
        super("BackgroundIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if(!isInternetAvailable()) {
            stopSelf();
            return;
        }
        String apiKey = intent.getStringExtra(MainActivity.PREFS_KEY_API_KEY);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long lastDeleteTime = prefs.getLong(LAST_DELETE_KEY, 0);
        long lastCheckTime = prefs.getLong(LAST_CHECK_KEY, 0);
        long currentTime = System.currentTimeMillis() / 1000;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(LAST_CHECK_KEY, currentTime);
        editor.apply();

        String url = ORIGINAL_URL + apiKey + URL_OPTIONS;
        int newOfferCount = 0;
        int newSinceLastCheckCount = 0;
        int totalOfferCount = 0;

        try {
            String json = getTradeOffers(url);
            if(json.equals("")) {
                return;
            }
            JSONObject response = new JSONObject(json).getJSONObject("response");
            if(!response.has("trade_offers_received") ) {
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
                    if(state == 2) {
                        //state 2 is active, any other state is useless to us for initial purposes.
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

        if(newSinceLastCheckCount > 0) {
            showNewTradeNotification(totalOfferCount, newOfferCount);
        }
    }

    private void removeNotification() {
        //No more trade offers so we don't want to be lying to the user
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }


    private void showNewTradeNotification(int totalOfferCount, int newOfferCount) {
        String titleText = String.valueOf(newOfferCount) + " new trade offer" + ((newOfferCount == 1) ? "" : "s");
        String contentText = "You now have " + String.valueOf(totalOfferCount) + " active trade offer" + ((totalOfferCount == 1) ? "" : "s");

        Intent deleteIntent = new Intent(this, NotificationDeleteReceiver.class);
        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 5, deleteIntent, 0);

        Intent clickIntent = new Intent(this, NotificationDeleteReceiver.class);
        clickIntent.putExtra(NOTIFICATION_CLICKED, "clicked");
        PendingIntent pendingContentIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 6, clickIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notif_trade)
                .setContentTitle(titleText)
                .setContentText(contentText)
                .setAutoCancel(true)
                .setVibrate(new long[]{300, 300, 300, 300, 300})
                .setDeleteIntent(pendingDeleteIntent)
                .setContentIntent(pendingContentIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(10101, notificationBuilder.build());
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

    private boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    private void handleBadApiKeyError() {
        showErrorNotification();
        //Cancel scheduled task so we don't get 100s of error notifications
        Intent backgroundIntent = new Intent(getApplicationContext(), BackgroundIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, backgroundIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
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
