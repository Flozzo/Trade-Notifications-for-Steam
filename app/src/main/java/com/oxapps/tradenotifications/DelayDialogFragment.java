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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;


public class DelayDialogFragment extends DialogFragment {
    OnDelaySetListener delaySetListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Resources resources = getResources();
        final String[] delays = resources.getStringArray(R.array.delay_names);
        final int[] delayMinutes = resources.getIntArray(R.array.delay_times);
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
