package com.ibm.iotsecuritydemo.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.ibm.iotsecuritydemo.R;
import com.ibm.iotsecuritydemo.core.DeviceIoTDemoApplication;
import com.ibm.iotsecuritydemo.monitor.MonitoredDevicesInformation;
import com.ibm.iotsecuritydemo.ui.fragments.FragmentMonitor;

public class MonitorActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MonitoredDevicesInformation devicesInformation =
                DeviceIoTDemoApplication.get().getMonitoredDevicesInformation();

        Log.i(TAG, new Gson().toJson(devicesInformation));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager(), devicesInformation);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_monitor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private MonitoredDevicesInformation devicesInformation;

        public SectionsPagerAdapter(FragmentManager fm, MonitoredDevicesInformation devicesInformation) {
            super(fm);
            this.devicesInformation = devicesInformation;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a FragmentMonitor instance.
            return FragmentMonitor.newInstance(devicesInformation.docs[position].deviceId);
        }

        // No of tabs is equal to no of devices - one tab for each device
        @Override
        public int getCount() {
            return devicesInformation.docs.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if ((position >= 0) && (position < devicesInformation.docs.length))
                return devicesInformation.docs[position].deviceId;

            return null;
        }
    }
}
