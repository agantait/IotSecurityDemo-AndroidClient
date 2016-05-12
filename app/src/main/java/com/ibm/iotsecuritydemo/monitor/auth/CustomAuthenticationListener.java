package com.ibm.iotsecuritydemo.monitor.auth;

/**
 * Created by ibm on 25/02/2016.
 */

import android.content.Context;
import android.util.Log;

import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AuthenticationContext;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AuthenticationListener;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomAuthenticationListener implements AuthenticationListener {
    private final String TAG = getClass().getSimpleName();

    private Context context;
    private String username = "";
    private String password = "";

    public CustomAuthenticationListener(Context context, String username, String password) {
        this.context = context;
        this.username = username;
        this.password = password;

        log("Instance created with username: " + username);
    }

    private void log(String text){
        Log.d(TAG, text);
    }

    @Override
    public void onAuthenticationChallengeReceived (AuthenticationContext authContext,
                                                   JSONObject challenge, Context context) {

        log("onAuthenticationChallengeReceived() entered: " + challenge.toString());

        // This is where developer would
        // show a login screen, collect credentials and invoke
        // authContext.submitAuthenticationChallengeAnswer() API

        JSONObject challengeResponse = new JSONObject();
        try {
            challengeResponse.put("username", username);
            challengeResponse.put("password", password);
            authContext.submitAuthenticationChallengeAnswer(challengeResponse);
        } catch (JSONException e){

            // In case there was a failure collecting credentials you need to report
            // it back to the AuthenticationContext. Otherwise Mobile Client
            // Access Client SDK will remain in a waiting-for-credentials state
            // forever

            log("Unexpected exception in submitting challenge answer to server: "+e);
            authContext.submitAuthenticationFailure(null);
        }
    }

    @Override
    public void onAuthenticationSuccess (Context context, JSONObject info) {
        log("Authentication success: " + info.toString());
        //Toast.makeText(context, "Successfully authenticated with Bluemix.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailure (Context context, JSONObject info) {
        log("Authentication failure: " + info.toString());
        //Toast.makeText(context, "Could not authenticate with Bluemix.", Toast.LENGTH_LONG).show();
    }

}