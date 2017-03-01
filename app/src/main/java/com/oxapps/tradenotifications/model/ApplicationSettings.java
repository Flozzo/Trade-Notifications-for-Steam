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
     * Sets the Steam API key into app settings
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
}
