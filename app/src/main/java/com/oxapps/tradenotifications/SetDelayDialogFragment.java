package com.oxapps.tradenotifications;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by flynn on 5/06/16.
 */
public class SetDelayDialogFragment extends DialogFragment {
    OnDelaySetListener delaySetListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] delays = {"5 minutes", "10 minutes", "15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours"};
        final int[] delayMinutes = {5, 10, 15, 30, 60, 120, 240};
        builder.setTitle("Checking Interval")

                .setItems(delays, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        long delay = delayMinutes[which] * 60 * 1000;
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putLong(MainActivity.PREFS_KEY_DELAY, delay);
                        editor.apply();
                        if(delaySetListener != null) {
                            delaySetListener.onDelaySet(delays[which], delay);
                        }
                    }
                });
        return builder.create();
    }

    public void setOnDelaySetListener(OnDelaySetListener delaySetListener) {
        this.delaySetListener = delaySetListener;
    }

    public interface OnDelaySetListener {
        void onDelaySet(String text, long delay);
    }
}
