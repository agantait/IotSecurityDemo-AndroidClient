package com.ibm.iotsecuritydemo.core;

import android.app.Application;
import android.content.Context;

import com.ibm.iotsecuritydemo.monitor.MonitoredDevicesInformation;

import java.text.SimpleDateFormat;

/**
 * Created by ibm on 03/01/2016.
 */
public class DeviceIoTDemoApplication extends Application {
    private static final String TAG = DeviceIoTDemoApplication.class.getSimpleName();
    
    private static DeviceIoTDemoApplication ourInstance = new DeviceIoTDemoApplication();

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Context launcherActivity;

    public static final String APPLICATION_ROUTE = "http://iotsecuritydemo.mybluemix.net";
    public static final String APPLICATION_ID = "your_application_id";

    public static final String APPLICATION_OAUTH_REALM = "iotsecuritydemoRealm";

    public static final int DEVICE_MONITOR_TIMER_INITIAL = 2000; // millisecs
    public static final int DEVICE_MONITOR_TIMER_INTERVAL = 5000; // millisecs

    // The devices that the logged in user is allowed to monitor, as per the list
    // of devices provided by the server-side custom API
    private MonitoredDevicesInformation monitoredDevicesInformation;


    public static DeviceIoTDemoApplication get() {
        return ourInstance;
    }

    private DeviceIoTDemoApplication() {
    }

    public DeviceIoTDemoApplication reset() {
        launcherActivity = null;
        return this;
    }

    public DeviceIoTDemoApplication initialize(Context context) {
        this.launcherActivity = context;
        return this;
    }

    public MonitoredDevicesInformation getMonitoredDevicesInformation() {
        return monitoredDevicesInformation;
    }

    public void setMonitoredDevicesInformation(MonitoredDevicesInformation monitoredDevicesInformation) {
        this.monitoredDevicesInformation = monitoredDevicesInformation;
    }

}
