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

package com.oxapps.tradenotifications.apikey;

/**
 * View interface for interaction from the presenter
 */
public interface ApiKeyActivityView {

    /**
     * Initialises views with values from their settings
     */
    void initViews();

    /**
     * Loads a URL into the view
     *
     * @param url the URL to load
     */
    void showUrl(String url);

    /**
     * Sets the WebViewClient
     *
     * @param apiKeyWebViewClient the client to assign
     */
    void setWebViewClient(ApiKeyWebViewClient apiKeyWebViewClient);

    /**
     * Hides the webview and instead displays a progress spinner
     */
    void hideWebShowProgress();

    /**
     * Finishes the API Key retrieval with a canceled result
     */
    void cancelAndFinish();

    /**
     * Finishes the API Key retrieval with a successful result
     */
    void successfullyFinish(String apiKey);
}
