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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.oxapps.tradenotifications.R;
import com.oxapps.tradenotifications.model.ApplicationSettingsImpl;
import com.oxapps.tradenotifications.model.IntentConsts;

/**
 * Activity for retrieving or creating the API Key for the user
 */
public class ApiKeyActivity extends AppCompatActivity implements ApiKeyActivityView {

    private WebView mWebView;
    private LinearLayout mProgressView;
    private ProgressBar mProgressBarWeb;

    /**
     * Presenter used for API Key logic
     */
    private ApiKeyPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_key);
        presenter = new ApiKeyPresenter(this, new ApplicationSettingsImpl(this));
        presenter.initialize();
    }

    /**
     * Initialises views with values from their settings
     */
    @Override
    public void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_api_key);
        setSupportActionBar(toolbar);
        mWebView = (WebView) findViewById(R.id.api_key_web);
        mProgressView = (LinearLayout) findViewById(R.id.progress_layout_api_key);
        mProgressBarWeb = (ProgressBar) findViewById(R.id.web_progress);
        mWebView.clearCache(true);
        mWebView.setWebChromeClient(new ProgressWebChromeClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    /**
     * Loads a URL into the WebView
     *
     * @param url the URL to load
     */
    @Override
    public void showUrl(String url) {
        mWebView.loadUrl(url);
    }

    /**
     * Sets the WebViewClient on the WebView
     *
     * @param apiKeyWebViewClient the client to assign
     */
    @Override
    public void setWebViewClient(ApiKeyWebViewClient apiKeyWebViewClient) {
        mWebView.setWebViewClient(apiKeyWebViewClient);
    }

    /**
     * Hides the webview and instead displays a progress spinner
     */
    @Override
    public void hideWebShowProgress() {
        mWebView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
    }

    /**
     * Finishes the API Key retrieval with a canceled result
     */
    @Override
    public void cancelAndFinish() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    /**
     * Finishes the API Key retrieval with a successful result
     */
    @Override
    public void successfullyFinish(String apiKey) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(IntentConsts.API_KEY, apiKey);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * WebChromeClient for handling progress updates
     */
    private class ProgressWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            int visibility = newProgress >= 100 ? View.GONE : View.VISIBLE;
            mProgressBarWeb.setVisibility(visibility);
            mProgressBarWeb.setProgress(newProgress);
        }
    }


}
