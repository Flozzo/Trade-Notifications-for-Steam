package com.oxapps.tradenotifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothA2dp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class NotificationDeleteReceiver extends BroadcastReceiver {
    private static final String TRADE_OFFERS_URL = "https://steamcommunity.com/id/id/tradeoffers/";
    private static final String TAG = "NotificationDeleteRec";


    @Override
    public void onReceive(Context context, Intent intent) {
        long newRequestTime = System.currentTimeMillis() / 1000;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(BackgroundIntentService.LAST_DELETE_KEY, newRequestTime);
        editor.apply();
        Log.d(TAG, "onReceive: New delete time");

        if(intent.hasExtra(BackgroundIntentService.NOTIFICATION_CLICKED)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(TRADE_OFFERS_URL));
            context.getApplicationContext().startActivity(browserIntent);
        }
    }
}
