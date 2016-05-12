package com.ibm.iotsecuritydemo.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.ibm.iotsecuritydemo.R;

public class LauncherActivity extends ActionBarActivity {

    private static final String TAG = LauncherActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    public void startLoginActivity(View view) {

        if(view.getId() == R.id.btn_monitor_device) {
            Log.i(TAG, "Starting OAuthLoginActivity.");
            Intent intent = new Intent(LauncherActivity.this, OAuthLoginActivity.class);
            LauncherActivity.this.startActivity(intent);
        }
    }
}
