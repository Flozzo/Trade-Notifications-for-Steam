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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Implementation of the model for storing and retrieiving application settings
 */
public class ApplicationSettingsImpl implements ApplicationSettings {

    /**
     * The {@link SharedPreferences} used to store and retrieve values
     */
    private final SharedPreferences mPrefs;

    /**
     * Default constructor
     *
     * @param context the context to retrieve the default SharedPreferences
     */
    public ApplicationSettingsImpl(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Retrieves the saved Steam API Key from the application's preferences
     *
     * @return the Steam API Key
     */
    @Override
    public String getApiKey() {
        return mPrefs.getString(SharedPreferenceConsts.API_KEY, "");
    }

    /**
     * Retrieves the saved notification refresh delay from the application's preferences
     *
     * @return the saved notification refresh delay
     */
    @Override
    public long getNotificationRefreshDelay() {
        return mPrefs.getLong(SharedPreferenceConsts.NOTIF_REFRESH_DELAY, SharedPreferenceConsts.DEFAULT_REFRESH_DELAY);
    }

    /**
     * Retrieves the user's Steam username from application preferences
     *
     * @return the user's Steam username
     */
    @Override
    public String getUsername() {
        return mPrefs.getString(SharedPreferenceConsts.USERNAME, "");
    }

    /**
     * Retrieves whether the user's URL is a custom profile from application preferences
     *
     * @return whether the user's URL is a custom profile
     */
    @Override
    public boolean isProfileUrl() {
        return mPrefs.getBoolean(SharedPreferenceConsts.PROFILE, true);
    }

    /**
     * Sets the Steam username in the app settings
     *
     * @param username the username to store
     */
    @Override
    public void setUsername(String username) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(SharedPreferenceConsts.USERNAME, username);
        editor.apply();
    }


    /**
     * Stores whether the user's URL is a custom profile in application settings
     *
     * @param isProfileUrl whether the user has a custom profile URL or not
     */
    @Override
    public void setProfileUrl(boolean isProfileUrl) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(SharedPreferenceConsts.USERNAME, isProfileUrl);
        editor.apply();
    }

    /**
     * Stores the Steam API Key in application's preferences
     *
     * @param apiKey the Steam API Key to be used
     */
    @Override
    public void setApiKey(String apiKey) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(SharedPreferenceConsts.API_KEY, apiKey);
        editor.apply();
    }

    /**
     * Stores a trade checking delay in preferences
     *
     * @param notificationRefreshDelay the delay to be stored
     */
    @Override
    public void setNotificationRefreshDelay(long notificationRefreshDelay) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putLong(SharedPreferenceConsts.NOTIF_REFRESH_DELAY, notificationRefreshDelay);
        editor.apply();
    }


}
