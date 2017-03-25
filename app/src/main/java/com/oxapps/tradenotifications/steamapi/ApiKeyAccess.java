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

package com.oxapps.tradenotifications.steamapi;

import com.oxapps.tradenotifications.apikey.UrlConsts;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Access for the Steam API Keys
 */

public class ApiKeyAccess {

    /**
     * Callback for the API Key retrieval request
     */
    private Callback callback;

    public ApiKeyAccess(Callback callback) {
        this.callback = callback;
    }

    /**
     * Builds and executes a HTTP Request to retrieve a Steam API Key to the given callback
     *
     * @param sessionId the session ID to use
     * @param cookies   the cookies to add to the request
     */
    public void getApiKey(final String sessionId, final String cookies) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        ArrayList<Cookie> addedCookie = new ArrayList<>();
                        for (String cookie : cookies.split("; ")) {
                            addedCookie.add(getCookieFromString(cookie));
                        }
                        return addedCookie;
                    }
                })
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("agreeToTerms", "agreed")
                .add("domain", "localhost")
                .add("Submit", "Register")
                .add("sessionid", sessionId)
                .build();
        Request request = new Request.Builder()
                .url(UrlConsts.API_KEY_URL)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * Creates a Cookie object from a cookie string
     *
     * @param cookie the string containing the cookies
     * @return a Cookie containing the relevant information
     */
    private Cookie getCookieFromString(String cookie) {
        String name = cookie.split("=")[0];
        String value = cookie.split("=")[1];
        return new Cookie.Builder()
                .domain("steamcommunity.com")
                .path("/")
                .name(name)
                .value(value)
                .build();
    }
}
