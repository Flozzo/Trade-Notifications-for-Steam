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

import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * WebViewClient used in API Key WebView
 */
public class ApiKeyWebViewClient extends WebViewClient {

    /**
     * The callback when home pages are loaded
     */
    private PageFinishedCallback callback;

    public ApiKeyWebViewClient(PageFinishedCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (url.matches(UrlConsts.HOME_URL_HTTP)) {
            callback.onHttpUrlLoaded(url);
        } else if (url.matches(UrlConsts.HOME_URL_HTTPS)) {
            String cookies = CookieManager.getInstance().getCookie(url);
            callback.onHttpsUrlLoaded(cookies);
        }
    }

    /**
     * Callback for relevant WebView pages having loaded
     */
    public interface PageFinishedCallback {
        void onHttpUrlLoaded(String url);

        void onHttpsUrlLoaded(String cookies);
    }
}
