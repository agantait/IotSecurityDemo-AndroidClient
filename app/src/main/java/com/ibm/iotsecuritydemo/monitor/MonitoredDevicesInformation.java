package com.ibm.iotsecuritydemo.monitor;

/**
 * Created by ibm on 29/02/2016.
 */
public class MonitoredDevicesInformation {
    public static class Device {
        public String _id;
        public String _rev;
        public String recordType;
        public String deviceType;
        public String deviceId;
        public String topic;
        public String timestamp;
        public String payload;
    }

    // All the devices that are being monitored by this client
    public Device[] docs;
}
