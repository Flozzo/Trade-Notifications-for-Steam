/*
 * Copyright 2017 Flynn van Os
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

package com.oxapps.tradenotifications.model;

import android.content.Context;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.oxapps.tradenotifications.BackgroundTaskService;

/**
 * Scheduler for the background task
 */

public class BackgroundTaskScheduler {

    /**
     * Tag for the scheduled task
     */
    private static final String TRADE_OFFER_TAG = "tradeoffers";

    /**
     * GcmNetworkManager that manages the tasks
     */
    private GcmNetworkManager networkManager;

    public BackgroundTaskScheduler(Context context) {
        networkManager = GcmNetworkManager.getInstance(context);
    }

    /**
     * Registers a task at a given delay with the {@link GcmNetworkManager}
     *
     * @param delay the delay between task execution
     */
    public void scheduleTask(long delay) {
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(BackgroundTaskService.class)
                .setTag(TRADE_OFFER_TAG)
                .setPeriod(delay)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .build();
        networkManager.schedule(task);
    }

    /**
     * Cancels the task scheduled
     */
    public void cancelTask() {
        networkManager.cancelTask(TRADE_OFFER_TAG, BackgroundTaskService.class);
    }
}
