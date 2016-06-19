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

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiKeyActivity extends AppCompatActivity {
    private static final String LOGIN_URL = "https://steamcommunity.com/login/home/?goto=0";
    private static final String API_KEY_URL = "https://steamcommunity.com/dev/registerkey";
    private static final String HOME_URL_HTTP = "http://steamcommunity.com/id/(\\w)+/home";
    private static final String HOME_URL_HTTPS = "https://steamcommunity.com/id/(\\w)+/home";
    private WebView mWebView;
    private LinearLayout mProgressView;
    private ProgressBar mProgressBarWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_key);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_api_key);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        mWebView = (WebView) findViewById(R.id.api_key_web);
        mProgressView = (LinearLayout) findViewById(R.id.progress_layout_api_key);
        mProgressBarWeb = (ProgressBar) findViewById(R.id.web_progress);
        mWebView.clearCache(true);
        mWebView.setWebViewClient(new ApiKeyWebViewClient());
        mWebView.setWebChromeClient(new ProgressWebChromeClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl(LOGIN_URL);

    }

    private class ApiKeyWebViewClient extends WebViewClient {

        @Override public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String cookies = CookieManager.getInstance().getCookie(url);
            if(url.matches(HOME_URL_HTTP)) {
                //Steam redirects us to the insecure version of /id/home, which does not give the steamLoginSecure cookie
                //Since we need this cookie to make HTTPS requests, we need to load up any HTTPS version first, which provides it
                mWebView.setVisibility(View.GONE);
                mProgressView.setVisibility(View.VISIBLE);
                mWebView.loadUrl(url.replace("http://", "https://"));
            } else if(url.matches(HOME_URL_HTTPS)) {
                String[] cookiesSplit = cookies.split("; ");
                for (String cookie : cookiesSplit) {
                    if(cookie.contains("sessionid")) {
                        String sessionId = cookie.split("=")[1];
                        getApiKey(sessionId, cookies);
                        break;
                    }
                }
            }
        }
    }

    private class ProgressWebChromeClient extends WebChromeClient {
        @Override public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            int visibility = newProgress >= 100 ? View.GONE : View.VISIBLE;
            mProgressBarWeb.setVisibility(visibility);
            mProgressBarWeb.setProgress(newProgress);
        }
    }

    private void getApiKey(final String sessionId, final String cookies) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    @Override public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {

                    }

                    @Override public List<Cookie> loadForRequest(HttpUrl url) {
                        ArrayList<Cookie> addedCookie = new ArrayList<>();
                        for(String cookie : cookies.split("; ")) {
                            addedCookie.add(getCookie(cookie));
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
                .url(API_KEY_URL)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new ApiKeyCallback());
    }

    private class ApiKeyCallback implements Callback {
        @Override public void onFailure(Call call, IOException e) {
            e.printStackTrace();
        }

        @Override public void onResponse(Call call, Response response) throws IOException {
            if(response.code() != 200) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
            String bodyString = response.body().string();
            Pattern keyPattern = Pattern.compile("<p>Key: ([0-9A-F]+)</p>");
            Matcher matcher = keyPattern.matcher(bodyString);

            Intent resultIntent = new Intent();

            if(matcher.find()) {
                String apiKey = matcher.group(1);
                resultIntent.putExtra(MainActivity.KEY_API_KEY, apiKey);
                setResult(Activity.RESULT_OK, resultIntent);
            } else {
                setResult(Activity.RESULT_CANCELED);
            }
            finish();
        }
    }

    private Cookie getCookie(String cookie) {
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
