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

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.oxapps.tradenotifications.ApiKeyActivity;
import com.oxapps.tradenotifications.DelayDialogFragment;
import com.oxapps.tradenotifications.R;
import com.oxapps.tradenotifications.model.ApplicationSettingsImpl;
import com.oxapps.tradenotifications.model.BackgroundTaskScheduler;
import com.oxapps.tradenotifications.model.ConnectionUtils;
import com.oxapps.tradenotifications.model.IntentConsts;

public class MainActivity extends AppCompatActivity implements MainActivityView, DelayDialogFragment.OnDelaySetListener {


    /**
     * Presenter associated with this activity
     */
    private MainActivityPresenter presenter;

    private static final String TRADE_URL = "https://steamcommunity.com/tradeoffer/new/?partner=72233084&token=NWnuxGBb";

    /**
     * Text field holding the API Key
     */
    private EditText apiKeyView;

    /**
     * TextView holding the delay text
     */
    private TextView delayView;

    /**
     * Button for API Key logic
     */
    private Button apiKeyButton;

    /**
     * API Key text field's locked status
     */
    private boolean apiKeyLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        presenter = new MainActivityPresenter(this, new ApplicationSettingsImpl(this), new ConnectionUtils(this));
        presenter.initialize();
    }

    /**
     * Initialise views used
     */
    @Override
    public void initViews() {
        apiKeyView = (EditText) findViewById(R.id.et_api_key);
        apiKeyButton = (Button) findViewById(R.id.button_get_api_key);
        delayView = (TextView) findViewById(R.id.tv_delay);
        apiKeyView.addTextChangedListener(mApiKeyErrorWatcher);
    }

    /**
     * Changes the API Key view
     *
     * @param apiKey the text to display
     */
    @Override
    public void showApiKey(String apiKey) {
        apiKeyView.setText(apiKey);
    }

    /**
     * Changes the refresh delay view
     *
     * @param refreshDelay the text to display
     */
    @Override
    public void showRefreshDelay(long refreshDelay) {
        delayView.setText(getDelayText(refreshDelay));
    }

    /**
     * Changes a delay time to a human-readable format
     *
     * @param delay the delay to convert
     * @return a human-readable version of the delay
     */
    private String getDelayText(long delay) {
        if (delay == 0) {
            return getString(R.string.disabled);
        } else if (delay < (120 * 60)) {
            return getString(R.string.delay_min, delay / 60);
        } else {
            return getString(R.string.delay_hours, delay / 3600);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentConsts.API_KEY_REQUEST) {
            if (resultCode == RESULT_OK) {
                String apiKey = data.getStringExtra(IntentConsts.API_KEY);
                presenter.updateApiKey(apiKey);
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.api_key_error, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    /**
     * Handles clicking of the Get API Key button
     *
     * @param view the clicked view
     */
    public void onGetApiKeyClicked(View view) {
        presenter.onGetApiKeyClicked();
    }

    /**
     * Toggles the locking of the API Key text field
     *
     * @param shouldLock whether or not the field should be locked
     */
    @Override
    public void lockApiKeyText(boolean shouldLock) {
        apiKeyLocked = shouldLock;
        apiKeyView.setFocusable(!shouldLock);
        apiKeyView.setFocusableInTouchMode(!shouldLock);
        apiKeyView.setEnabled(!shouldLock);
        int buttonTextRes = shouldLock ? R.string.api_key_edit : R.string.api_key_get;
        apiKeyButton.setText(buttonTextRes);
    }

    /**
     * Launches the API Key retrieval activity
     */
    @Override
    public void startApiKeyActivity() {
        Intent intent = new Intent(this, ApiKeyActivity.class);
        startActivityForResult(intent, IntentConsts.API_KEY_REQUEST);
    }

    /**
     * Displays a {@link Snackbar} with a given message
     *
     * @param message the message to display
     */
    @Override
    public void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    /**
     * Displays a {@link Snackbar} with a given message
     *
     * @param messageResId the string id of the message to display
     */
    @Override
    public void showSnackbar(int messageResId) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), messageResId, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    /**
     * Toggles the error message on the API Key field
     *
     * @param showError
     */
    @Override
    public void showInvalidApiKeyError(boolean showError) {
        String error = showError ? getString(R.string.invalid_api_key) : null;
        apiKeyView.setError(error);
    }

    /**
     * @return whether the API Key text field is locked
     */
    @Override
    public boolean isApiKeyLocked() {
        return apiKeyLocked;
    }

    /**
     * @return the text of the API Key textfield
     */
    @Override
    public String getApiKeyText() {
        return apiKeyView.getText().toString().trim();
    }

    /**
     * Shows the interval selector dialog
     *
     * @param v the view that was clicked
     */
    public void showIntervalDialog(View v) {
        DelayDialogFragment ddf = new DelayDialogFragment();
        ddf.setOnDelaySetListener(MainActivity.this);
        ddf.show(getSupportFragmentManager(), "tag");
    }

    /**
     * Handles the delay being set from the dialog
     *
     * @param delay the delay that was set
     */
    @Override
    public void onDelaySet(long delay) {
        presenter.updateDelay(delay);
    }

    /**
     * Handles the donation button click
     *
     * @param view the view that was clicked
     */
    public void onDonationClicked(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(TRADE_URL));
        startActivity(browserIntent);
    }

    @Override
    protected void onPause() {
        presenter.setBackgroundTaskScheduler(new BackgroundTaskScheduler(this));
        presenter.onActivityExiting();
        super.onPause();
    }

    private void showInvalidApiKeyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.invalid_api_key)
                .setMessage(R.string.invalid_api_key_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * TextWatcher for the API Key text field
     */
    private TextWatcher mApiKeyErrorWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            presenter.onApiKeyTextUpdated(editable);
        }
    };
}
