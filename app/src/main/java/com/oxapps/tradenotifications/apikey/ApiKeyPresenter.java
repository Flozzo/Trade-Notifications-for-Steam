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

import com.oxapps.tradenotifications.steamapi.ApiKeyAccess;
import com.oxapps.tradenotifications.model.ApplicationSettings;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Presenter for the API key retrieval
 */
public class ApiKeyPresenter implements ApiKeyWebViewClient.PageFinishedCallback {

    private final ApiKeyActivityView mView;
    private ApplicationSettings settings;

    public ApiKeyPresenter(ApiKeyActivityView view, ApplicationSettings settings) {
        this.mView = view;
        this.settings = settings;
    }

    /**
     * Initialises views with values from their settings
     */
    public void initialize() {
        mView.initViews();
        mView.setWebViewClient(new ApiKeyWebViewClient(this));
        mView.showUrl(UrlConsts.LOGIN_URL);
    }

    /**
     * Extracts information from the initial HTTP home URL loaded
     * <p>
     * Since the application needs the steamLoginSecure cookie, it reloads the page as HTTPS
     *
     * @param url the URL loaded
     */
    @Override
    public void onHttpUrlLoaded(String url) {
        boolean isProfile = url.matches("http://steamcommunity.com/profiles/(\\w)+/home");
        Pattern keyPattern = Pattern.compile("/(\\w+)/home");
        Matcher matcher = keyPattern.matcher(url);
        if (matcher.find()) {
            String username = matcher.group(1);
            settings.setUsername(username);
            settings.setProfileUrl(isProfile);
        }
        mView.hideWebShowProgress();
        mView.showUrl(url.replace("http://", "https://"));
    }

    /**
     * Extracts information from the HTTPS home URL loaded
     *
     * @param cookies the cookies from the result
     */
    @Override
    public void onHttpsUrlLoaded(String cookies) {
        String[] cookiesSplit = cookies.split("; ");
        for (String cookie : cookiesSplit) {
            if (cookie.contains("sessionid")) {
                String sessionId = cookie.split("=")[1];
                ApiKeyAccess access = new ApiKeyAccess(new ApiKeyCallback());
                access.getApiKey(sessionId, cookies);
                return;
            }
        }
        mView.cancelAndFinish();
    }

    /**
     * Callback for the result of the API Key request
     */
    public class ApiKeyCallback implements Callback {

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.code() != 200) {
                mView.cancelAndFinish();
            }
            String bodyString = response.body().string();
            Pattern keyPattern = Pattern.compile("<p>Key: ([0-9A-F]+)</p>");
            Matcher matcher = keyPattern.matcher(bodyString);

            if (matcher.find()) {
                String apiKey = matcher.group(1);
                mView.successfullyFinish(apiKey);
            } else {
                mView.cancelAndFinish();
            }
        }
    }
}
