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

package com.oxapps.tradenotifications.main;

import android.text.Editable;

import com.oxapps.tradenotifications.R;
import com.oxapps.tradenotifications.model.ApplicationSettings;
import com.oxapps.tradenotifications.model.BackgroundTaskScheduler;
import com.oxapps.tradenotifications.model.ConnectionUtils;

/**
 * Presenter for the main settings activity
 */

public class MainActivityPresenter {

    /**
     * Model used to access settings
     */
    private final ApplicationSettings mAppSettings;

    /**
     * Used for conenction related checks
     */
    private ConnectionUtils connectionUtils;

    /**
     * View associated with this presenter
     */
    private MainActivityView mView;

    /**
     * Scheduler for scheduling the background task
     */
    private BackgroundTaskScheduler scheduler;

    /**
     * Delay set from refreshing trade notifications
     */
    private long delay;

    public MainActivityPresenter(MainActivityView view, ApplicationSettings appSettings, ConnectionUtils connectionUtils) {
        this.mView = view;
        this.mAppSettings = appSettings;
        this.connectionUtils = connectionUtils;
    }

    /**
     * Initialises views with values from their settings
     */
    public void initialize() {
        mView.initViews();
        String apiKey = mAppSettings.getApiKey();
        mView.showApiKey(apiKey);
        mView.showRefreshDelay(mAppSettings.getNotificationRefreshDelay());
    }

    /**
     * Updates the api key logic based on a previous result
     *
     * @param apiKey the api key to be used
     */
    public void updateApiKey(String apiKey) {
        mView.showApiKey(apiKey);
        if (isValidApiKey(apiKey)) {
            mView.lockApiKeyText(true);
        }
    }

    /**
     * Updates the refresh delay based on a previous result
     *
     * @param delay the refresh delay used
     */
    public void updateDelay(long delay) {
        this.delay = delay;
        mView.showRefreshDelay(delay);
    }

    /**
     * Does a basic check to see if an API Key could be valid
     *
     * @param apiKey the api key to be checked
     * @return whether the API Key is a 32-digit alphanumeric string
     */
    private boolean isValidApiKey(String apiKey) {
        return apiKey.matches("[A-z0-9]{32}");
    }

    /**
     * Handles the presenter logic of the Get API Key button being triggered
     */
    public void onGetApiKeyClicked() {
        if (mView.isApiKeyLocked()) {
            mView.lockApiKeyText(false);
        } else if (connectionUtils.isInternetAvailable()) {
            mView.startApiKeyActivity();
        } else {
            mView.showSnackbar(R.string.no_internet);
        }
    }

    /**
     * Handles logic of the API Key Text being updated
     *
     * @param editable the text that was updated
     */
    public void onApiKeyTextUpdated(Editable editable) {
        boolean showError = isValidApiKey(editable.toString());
        //mView.showInvalidApiKeyError(showError);
    }

    /**
     * @return whether the displayed API Key differs from the saved version
     */
    public boolean hasApiKeyChanged() {
        String apiKeyFromText = mView.getApiKeyText();
        return !mAppSettings.getApiKey().equals(apiKeyFromText);
    }

    private boolean hasDelayChanged() {
        return mAppSettings.getNotificationRefreshDelay() != delay;
    }

    public void setBackgroundTaskScheduler(BackgroundTaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void onActivityExiting() {
        if (hasApiKeyChanged() || hasDelayChanged()) {
            String apiKey = mView.getApiKeyText();
            if (isValidApiKey(apiKey)) {
                mAppSettings.setApiKey(apiKey);
                mAppSettings.setNotificationRefreshDelay(delay);

                if (delay != 0) {
                    scheduler.scheduleTask(delay);
                } else {
                    scheduler.cancelTask();
                }
            }
        }
    }

}
