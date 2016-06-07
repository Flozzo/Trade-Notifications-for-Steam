package com.oxapps.tradenotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class NotificationDeleteReceiver extends BroadcastReceiver {
    private static final String TRADE_OFFERS_URL = "https://steamcommunity.com/id/id/tradeoffers/";


    @Override
    public void onReceive(Context context, Intent intent) {
        long newRequestTime = System.currentTimeMillis() / 1000;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(BackgroundIntentService.LAST_DELETE_KEY, newRequestTime);
        editor.apply();

        if(intent.hasExtra(BackgroundIntentService.NOTIFICATION_CLICKED)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(TRADE_OFFERS_URL));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(browserIntent);
        }
    }
}
