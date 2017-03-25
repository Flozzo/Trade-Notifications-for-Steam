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

/**
 * Model for storing and retrieiving application settings
 */
public interface ApplicationSettings {

    /**
     * @return the Steam API key
     */
    String getApiKey();

    /**
     * @return the notification refresh delay
     */
    long getNotificationRefreshDelay();

    /**
     * @return the user's Steam username
     */
    String getUsername();

    /**
     * @return whether the user's URL is a custom profile
     */
    boolean isProfileUrl();

    /**
     * @return the last time the user's trades were checked in Unix time format
     */
    long getLastCheckTime();

    /**
     * @return the last time a trade notification was removed in Unix time format
     */
    long getLastDeleteTime();

    /**
     * Sets the Steam username in the app settings
     *
     * @param username the username to store
     */
    void setUsername(String username);

    /**
     * Sets whether the user's URL is a custom profile in settings
     *
     * @param isProfileUrl whether the user has a custom profile URL or not
     */
    void setProfileUrl(boolean isProfileUrl);

    /**
     * Sets the Steam API key in the app settings
     *
     * @param apiKey the Steam API Key to be used
     */
    void setApiKey(String apiKey);

    /**
     * Sets the notification checking interval into app settings
     *
     * @param refreshDelay the delay to be used
     */
    void setNotificationRefreshDelay(long refreshDelay);

    /**
     * Sets the last time the application checked the user's trade offers
     *
     * @param lastCheckTime the time to set the setting to in Unix time format
     */
    void setLastCheckTime(long lastCheckTime);

    /**
     * Sets the last time a trade notification was removed
     *
     * @param lastDeleteTime the time to set the setting to in Unix time format
     */
    void setLastDeleteTime(long lastDeleteTime);


}
