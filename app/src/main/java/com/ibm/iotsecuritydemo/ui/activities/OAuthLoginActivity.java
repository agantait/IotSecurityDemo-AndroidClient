package com.ibm.iotsecuritydemo.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ibm.iotsecuritydemo.R;
import com.ibm.iotsecuritydemo.core.DeviceIoTDemoApplication;
import com.ibm.iotsecuritydemo.monitor.MonitoredDevicesInformation;
import com.ibm.iotsecuritydemo.monitor.auth.CustomAuthenticationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AuthorizationManager;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class OAuthLoginActivity
        extends AppCompatActivity
        implements
            LoaderCallbacks<Cursor> {

    private static final String TAG = OAuthLoginActivity.class.getSimpleName();

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    private static final int MIN_PASSWORD_LENGTH = 4;

    /**
     * A dummy authentication store containing known user names and passwords.
     * This is used for dummy test of the UI with a DUMMY_RESPONSE instead of connecting to server.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "test@test.com:test", "bar@example.com:world"
    };

    /**
     * A dummy response mimicking /iotf/devices API response from the server.
     * This is used for dummy test of the UI.
     */
    private static final String DUMMY_RESPONSE = "{\n" +
            "    \"docs\": [\n" +
            "        {\n" +
            "            \"_id\": \"j.patra\",\n" +
            "            \"_rev\": \"1608-4ae9dadb392a116b629f1dcc1963c710\",\n" +
            "            \"recordType\": \"status\",\n" +
            "            \"deviceType\": \"Android\",\n" +
            "            \"deviceId\": \"j.patra\",\n" +
            "            \"topic\": \"iot-2/type/Android/id/j.patra/mon\",\n" +
            "            \"timestamp\": null,\n" +
            "            \"payload\": \"1fce96b12265717f3e0bbe4db0c71588b0408abad5e4c92638e9174297847c21c418d291c3d9c00581dd6c74ef21c993f353ffeb067db86b93ee9fe521339647b6416bc4e1cbd9bebda4e5cbe6a05a04d2e8b4da9fa191819cdec4f84e9ede3a82449eebfb2d50c4fe19916690a6bd13b410dd127f24cd611a1a56cf54d756c6603a9e77f40f8a7ec0ce46aa206082744ab1bf3b22b911e4ef7d9fd6951fb1f6b097579f494f0761b06b8cb0608532cdc4a40199750de3c01b4a43b427d1427ad38075c0dc7035af86ee5067c8937951e5248dff25170c4e8312c415d1d573d8303606ab66f15bf2b5076a1a8f96f21358a4c2e02b4a316a330103758d17e3f88a18ac991a8b4dd35e57985790f3f706ffb0bda37a9e190670f094b63d5ddf13f12751201f080be89082562ed262c60153250d9556eb0aeab1f944d0bdcec60e543ddadf7356ea3f634f9631d0bfdbc47e102964ca7b92eecfc337d9ee79fc4d4ed602000a9c20cb890f5177e14f59aff6cb6e723ee845e6bd0082b267248f8e7115f92bdbf43767ed4ca4105a0b0d83ca41cb2b3e0b\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"_id\": \"m.patra\",\n" +
            "            \"_rev\": \"1618-bd589af3bf14877f4fea710504cdbe1e\",\n" +
            "            \"recordType\": \"status\",\n" +
            "            \"deviceType\": \"Android\",\n" +
            "            \"deviceId\": \"m.patra\",\n" +
            "            \"topic\": \"iot-2/type/Android/id/m.patra/mon\",\n" +
            "            \"timestamp\": null,\n" +
            "            \"payload\": \"1fce96b12265717f3e0bbe4db0c71588b0408abad5e4c92638e9174297847c21c418d291c3d9c00581dd6c77ee21c89cf352f1eb017db86b93ea9fe521339647b6416bc4e1cbd9bebda4e5cbe6a35b02d2e8bbdc9fa7949987dbd7e64c90bd74a84d99fadc1d3bbafc01d57e91adec12b24cd53f2b01d16a011105c640cd19c2732fce3af601fa33e2d516e4226b9a7d41a5ac3551fe21f4fe6daedcb81ebafba19d4c98041b053df1619eb93fc03cbd949b0799620dec8d4d485bf974c84723d3ce718180723bdad7de512080916104b434d3ee6708064bcb1d9c00cc8337da3e554b8667fa56e3821a5316c0c9f00b4ab6c1e6374c2e773015657ce11de4fb8018a3841a924cb04c219a59f3bddb19f580b78470d8460473e281a4332f9820e7294c6d4b0a139cdab31e289d6fc70a5e34078e51a413abb5b8549cb1d1cc19113b839d7e1efb77750a9722c7bb90947c1227168d4893cdd2f52bdea930fe4059d602010c9c20cb890f5177e14f59aff6cb6f6728e632b683068e847b129389365cfb27cbed22728812ed46683417908d36987c3c4cc006bbf7\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_login);
        setupActionBar();

        // Set up the login form.
        mLoginFormView = findViewById(R.id.oauth_login_form);
        mProgressView = findViewById(R.id.login_progress);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        try {
            // Initialize Watson IoT SDK with IBM Bluemix application ID and route
            // You can find your backendRoute and backendGUID in the Mobile Options section on top of your Bluemix application dashboard

            BMSClient.getInstance().initialize(
                    getApplicationContext(),
                    DeviceIoTDemoApplication.APPLICATION_ROUTE,
                    DeviceIoTDemoApplication.APPLICATION_ID);

            Log.d(TAG, "BMSClient initialized.");

            Logger.setSDKInternalLoggingEnabled(true);
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "Error initializing Bluemix Mobile Service Client: "+e);
        }

    } // end onCreate()

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        }
        else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(
                        ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                        ProfileQuery.PROJECTION,
                        // Select only email addresses.
                        ContactsContract.Contacts.Data.MIMETYPE +
                            " = ?",
                        new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},
                        // Show primary email addresses first. Note that there won't be
                        // a primary email address if the user hasn't specified one.
                        ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
                );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(OAuthLoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called.");
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private class BMSResponseListener implements ResponseListener {

        @Override
        public void onSuccess (final Response response) {
            Log.d(TAG, "onSuccess() entered: " + response.getResponseText());
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                        }
                    }
            );
        }
        @Override
        public void onFailure (final Response response, Throwable t, JSONObject extendedInfo) {
            final String errorMessage =
                    (null != response ? response.getResponseText(): "")+"\n"
                            +(null != t ? t.toString(): "")+"\n"
                            +(null != extendedInfo ? extendedInfo.toString(): "")+"\n";

            Log.e(TAG, "onFailure(): \n"+errorMessage);

            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            //mWebView.loadData(webViewMessage, "text/plain", null);
                            Toast.makeText(
                                    OAuthLoginActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG).show();
                            showProgress(false);
                        }
                    }
            );
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String TAG = UserLoginTask.class.getSimpleName();

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // to make the authorization happen next time
            AuthorizationManager.getInstance().clearAuthorizationData();

            Log.d(TAG, "Cleared authorization data.");

            BMSClient.getInstance().registerAuthenticationListener(
                    DeviceIoTDemoApplication.APPLICATION_OAUTH_REALM,
                    new CustomAuthenticationListener(OAuthLoginActivity.this, mEmail, mPassword)
            );

            Log.d(TAG, "Registered authentication listener.");

            if(matchesDummy(mEmail, mPassword)) {
                DeviceIoTDemoApplication.get().setMonitoredDevicesInformation(
                        new Gson().fromJson(DUMMY_RESPONSE, MonitoredDevicesInformation.class)
                );

                Log.i(TAG, "Starting MonitorActivity: Response=" + DUMMY_RESPONSE);
                Intent intent = new Intent(OAuthLoginActivity.this, MonitorActivity.class);
                OAuthLoginActivity.this.startActivity(intent);
            }
            else {
                /*
                 * By calling a protected API on the server side, IBM Bluemix Mobile Client Access
                 * Server Side SDK automatically determines if an authentication challenge is
                 * necessary, and causes the client side authentication handler to be triggered.
                 * There is no need to call any login or authentication API specifically.
                 */
                Log.d(TAG, "Requesting: " + BMSClient.getInstance().getBluemixAppRoute() + "/iotf/devices");

                Request request = new Request(BMSClient.getInstance().getBluemixAppRoute() + "/iotf/devices", Request.GET);

                request.send(OAuthLoginActivity.this, new BMSResponseListener() {
                    @Override
                    public void onSuccess(final Response response) {
                        super.onSuccess(response);
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        DeviceIoTDemoApplication.get().setMonitoredDevicesInformation(
                                                new Gson().fromJson(response.getResponseText(), MonitoredDevicesInformation.class)
                                        );

                                        Log.i(TAG, "Starting MonitorActivity: Response=" + response.getResponseText());
                                        Intent intent = new Intent(OAuthLoginActivity.this, MonitorActivity.class);
                                        OAuthLoginActivity.this.startActivity(intent);
                                    }
                                }
                        );
                    }
                });
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                // Do nothing - wait for response listener to act
            } else {

                mPasswordView.setError(getString(R.string.error_login_failure));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        private boolean matchesDummy(String email, String password) {
            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(email)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(password);
                }
            }
            return false;
        }
    }
}

