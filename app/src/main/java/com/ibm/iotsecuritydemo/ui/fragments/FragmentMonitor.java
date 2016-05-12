package com.ibm.iotsecuritydemo.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ibm.iotsecuritydemo.R;
import com.ibm.iotsecuritydemo.core.DeviceIoTDemoApplication;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * This fragment periodically polls the server for the latest events
 * for the given deviceId, and displays each received event as a
 * simple set of strings.
 */
public class FragmentMonitor extends Fragment {
    private final static String TAG = FragmentMonitor.class.getSimpleName();

    /**
     * The fragment argument representing the deviceId for this
     * fragment.
     */
    private static final String ARG_DEVICE_ID = "deviceId";

    private TextView mTextView;

    /**
     * If a dummy login was used, then the fragment displays this single event as a
     * dummy device data.
     *
     * This is also useful to understand how a single event data looks like when
     * retrieved from the demo server.
     */
    private static final String DUMMY_DEVICE_DATA = "{\n" +
            "   \"docs\":[\n" +
            "      {\n" +
            "         \"_id\":\"4cf3d05b9c33cd05159d1f688727b4a4\",\n" +
            "         \"_rev\":\"1-34431ee058f9ae0f749dcd481af372b8\",\n" +
            "         \"recordType\":\"event\",\n" +
            "         \"deviceType\":\"Android\",\n" +
            "         \"deviceId\":\"m.patra\",\n" +
            "         \"eventType\":\"iotevt\",\n" +
            "         \"format\":\"json\",\n" +
            "         \"timestamp\":\"1455909252026\",\n" +
            "         \"payload\":{\n" +
            "            \"d\":{\n" +
            "               \"timestampMillis\":1455909252026,\n" +
            "               \"ACCELEROMETER_0\":-7.814,\n" +
            "               \"ACCELEROMETER_1\":1.992,\n" +
            "               \"ACCELEROMETER_2\":9.194,\n" +
            "               \"PROXIMITY_0\":1,\n" +
            "               \"PROXIMITY_1\":0,\n" +
            "               \"PROXIMITY_2\":0,\n" +
            "               \"LIGHT_0\":6,\n" +
            "               \"LIGHT_1\":0,\n" +
            "               \"LIGHT_2\":0,\n" +
            "               \"MAGNETOMETER_0\":-1.812,\n" +
            "               \"MAGNETOMETER_1\":31.938,\n" +
            "               \"MAGNETOMETER_2\":-22.812,\n" +
            "               \"ORIENTATION_0\":20.156,\n" +
            "               \"ORIENTATION_1\":-9.359,\n" +
            "               \"ORIENTATION_2\":-39.734,\n" +
            "               \"AKM Software Virtual Gyroscope sensor _0\":0.722,\n" +
            "               \"AKM Software Virtual Gyroscope sensor _1\":1.297,\n" +
            "               \"AKM Software Virtual Gyroscope sensor _2\":-0.051,\n" +
            "               \"AKM Rotation vector sensor_0\":0.009,\n" +
            "               \"AKM Rotation vector sensor_1\":0.256,\n" +
            "               \"AKM Rotation vector sensor_2\":-0.187,\n" +
            "               \"AKM Rotation vector sensor_3\":0,\n" +
            "               \"AKM Rotation vector sensor_4\":-1,\n" +
            "               \"AKM Gravity sensor_0\":-4.794,\n" +
            "               \"AKM Gravity sensor_1\":-0.763,\n" +
            "               \"AKM Gravity sensor_2\":8.526,\n" +
            "               \"AKM Linear acceleration sensor_0\":-3.024,\n" +
            "               \"AKM Linear acceleration sensor_1\":2.751,\n" +
            "               \"AKM Linear acceleration sensor_2\":0.667,\n" +
            "               \"LOCATION_0\":22.672,\n" +
            "               \"LOCATION_1\":88.442\n" +
            "            }\n" +
            "         }\n" +
            "      }\n" +
            "   ]\n" +
            "}";

    private String deviceId;

    //We are going to use a monitoringTimerHandler to be able to run in our TimerTask
    final Handler monitoringTimerHandler = new Handler();
    private Timer monitoringTimer;
    private TimerTask monitoringTimerTask;
    
    /*
     * The Payload_d class assumes a certain set of sensors for the device to keep it
     * simple to convert from the JSON response from the server.
     * In reality, this should contain a dynamic array of sensors and sensor data, that
     * will be dynamically determined by parsing the JSON response.
     */
    private class Payload_d {
        public long timestampMillis = 1455909252026L;
        public float ACCELEROMETER_0 = -7.814f;
        public float ACCELEROMETER_1 = 1.992f;
        public float ACCELEROMETER_2 = 9.194f;
        public float PROXIMITY_0 = 1f;
        public float PROXIMITY_1 = 0f;
        public float PROXIMITY_2 = 0f;
        public float LIGHT_0 = 6f;
        public float LIGHT_1 = 0f;
        public float LIGHT_2 = 0f;
        public float MAGNETOMETER_0 = -1.812f;
        public float MAGNETOMETER_1 = 31.938f;
        public float MAGNETOMETER_2 = -22.812f;
        public float ORIENTATION_0 = 20.156f;
        public float ORIENTATION_1 = -9.359f;
        public float ORIENTATION_2 = -39.734f;
        public float AKM_Software_Virtual_Gyroscope_sensor__0 = 0.722f;
        public float AKM_Software_Virtual_Gyroscope_sensor__1 = 1.297f;
        public float AKM_Software_Virtual_Gyroscope_sensor__2 = -0.051f;
        public float AKM_Rotation_vector_sensor_0 = 0.009f;
        public float AKM_Rotation_vector_sensor_1 = 0.256f;
        public float AKM_Rotation_vector_sensor_2 = -0.187f;
        public float AKM_Rotation_vector_sensor_3 = 0f;
        public float AKM_Rotation_vector_sensor_4 = -1f;
        public float AKM_Gravity_sensor_0 = -4.794f;
        public float AKM_Gravity_sensor_1 = -0.763f;
        public float AKM_Gravity_sensor_2 = 8.526f;
        public float AKM_Linear_acceleration_sensor_0 = -3.024f;
        public float AKM_Linear_acceleration_sensor_1 = 2.751f;
        public float AKM_Linear_acceleration_sensor_2 = 0f;
        public float LOCATION_0 = 22.5656f;
        public float LOCATION_1 = 88.5757f;

        @Override
        public String toString() {
            Map<String, Map<Integer, String>>    sensorDataMap = new LinkedHashMap<>();

            // Use reflection to detect the sensors coded as fields of this class.
            Field[] fields = this.getClass().getDeclaredFields();

            for(Field field: fields) {
                addFieldToMap(sensorDataMap, field);
            }

            String output = "";

            output += "Event Timestamp: " +
                    DeviceIoTDemoApplication.DATE_FORMAT.format(new Date(timestampMillis));

            for(Map.Entry<String, Map<Integer, String>> sensorData: sensorDataMap.entrySet()) {
                output += sensorDataToString(sensorData);
            }

            return output;
        }

        private void addFieldToMap(Map<String, Map<Integer, String>> sensorDataMap, Field field) {
            String fieldName = field.getName();

            if(("timestampMillis".equals(fieldName)) || (fieldName.startsWith("this")) || (fieldName.startsWith("$")))
                return;

            String sensorName = fieldName.substring(0, fieldName.length()-2);
            String sensorValueIndexString = fieldName.substring(fieldName.length()-1, fieldName.length());

            Log.d(TAG, "Sensor "+sensorName+", "+sensorValueIndexString);

            int sensorValueIndex;

            try {
                sensorValueIndex = Integer.parseInt(sensorValueIndexString);
            }
            catch(NumberFormatException exc) {
                sensorValueIndex = 0;
            }

            Map<Integer, String> sensorValueList = sensorDataMap.get(sensorName);

            if(null == sensorValueList)
                sensorDataMap.put(sensorName, (sensorValueList = new TreeMap<Integer, String>() ));

            try {
                sensorValueList.put(sensorValueIndex, String.format("%.3f", (Float) field.get(this)));
            }
            catch(IllegalAccessException e) {
                Log.d(TAG, "Error accessing "+sensorName+", value at position "+sensorValueIndex+"\n"+e);
            }
        }

        private String sensorDataToString(Map.Entry<String, Map<Integer, String>> sensorData) {
            String output = "\n\""+sensorData.getKey()+"\": [";

            Iterator<Map.Entry<Integer, String>> iter = sensorData.getValue().entrySet().iterator();

            while(iter.hasNext()) {
                output += iter.next().getValue();

                if(iter.hasNext()) output += ", ";
            }

            output += "]";

            return output;
        }
    }
    
    private class Payload {
        public Payload_d d = new Payload_d();
    }
    
    private class DeviceDataSnapshot {
        public String _id = "4cf3d05b9c33cd05159d1f688727b4a4";
        public String _rev = "1-34431ee058f9ae0f749dcd481af372b8";
        public String recordType = "event";
        public String deviceType = "Android";
        public String deviceId = "m.patra";
        public String eventType = "iotevt";
        public String format = "json";
        public String timestamp = "1455909252026";
        public Payload payload = new Payload();
    }

    private class DeviceData {
        public DeviceDataSnapshot[] docs = new DeviceDataSnapshot[] { new DeviceDataSnapshot() };
    }


    public FragmentMonitor() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragmentMonitor newInstance(String deviceId) {
        FragmentMonitor fragment = new FragmentMonitor();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_monitor, container, false);

        deviceId = getArguments().getString(ARG_DEVICE_ID);

        mTextView = (TextView) rootView.findViewById(R.id.section_label);
        mTextView.setText(getString(R.string.device_id_format, deviceId));

        //Log.d(TAG, "Sample data: "+(new GsonBuilder().setPrettyPrinting().create().toJson(new DeviceData())));
        Log.d(TAG, "Sample data: "+(new Gson().toJson(new DeviceData())));

        startMonitoringTimerTask();

        return rootView;
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        stopMonitoringTimerTask();
    }

    public void startMonitoringTimerTask() {
        // stop any existing timer
        stopMonitoringTimerTask();

        //set a new Timer
        monitoringTimer = new Timer();

        //initialize the TimerTask's job
        monitoringTimerTask = new MonitoringTimerTask(deviceId);

        //Schedule the monitoringTimer
        monitoringTimer.schedule(
                monitoringTimerTask,
                DeviceIoTDemoApplication.DEVICE_MONITOR_TIMER_INITIAL,
                DeviceIoTDemoApplication.DEVICE_MONITOR_TIMER_INTERVAL);

        Log.d(TAG, "Monitoring timer started for device: " + deviceId);
    }

    public void stopMonitoringTimerTask() {
        //stop the monitoringTimer, if it's not already null
        if (null != monitoringTimer) {
            monitoringTimer.cancel();
            monitoringTimer = null;
        }
    }


    public class MonitoringTimerTask extends TimerTask {
        private String deviceId;

        public MonitoringTimerTask(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            //use a monitoringTimerHandler to run an async request to the server
            monitoringTimerHandler.post(new Runnable() {
                public void run() {
                Log.d(TAG, "Monitoring timer fired for device: " + deviceId);

                // We are requesting only the last event, hence count=1
                Request request = new Request(
                                    BMSClient.getInstance().getBluemixAppRoute()+
                                            "/iotf/devices/"+
                                            deviceId+
                                            "?count=1",
                                    Request.GET);

                request.send(FragmentMonitor.this.getContext(), new ResponseListener(){

                    /**
                     * This method will be called only when a response from the server has been received with a status
                     * in the 200 range.
                     *
                     * @param response the server response
                     */
                    @Override
                    public void onSuccess(Response response) {
                        Log.d(TAG,
                                DeviceIoTDemoApplication.DATE_FORMAT.format(new Date()) +
                                ": " + response.getResponseText());

                        final DeviceData deviceData =
                                new Gson().fromJson(response.getResponseText(), DeviceData.class);

                        if((null != deviceData) && (null != deviceData.docs) && (deviceData.docs.length > 0)) {
                            Activity activity = getActivity();

                            if(null != activity) {
                                activity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                mTextView.setText(
                                                        "Received at: " +
                                                                DeviceIoTDemoApplication.DATE_FORMAT.format(new Date()) + ":\n" +
                                                                deviceData.docs[0].payload.d.toString());
                                            }
                                        }
                                );
                            }
                        }
                    }

                    /**
                     * This method will be called in the following cases:
                     * <ul>
                     * <li>There is no response from the server.</li>
                     * <li>The status from the server response is in the 400 or 500 ranges.</li>
                     * <li>There is an operational failure such as: authentication failure, data validation failure, or custom failure.</li>
                     * </ul>
                     *
                     * @param response     Contains detail regarding why the Http request failed. May be null if the request did not reach the server
                     * @param t            Exception that could have caused the request to fail. null if no Exception thrown.
                     * @param extendedInfo Contains details regarding operational failure. null if no operational failure occurred.
                     */
                    @Override
                    public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                        final String errorMessage =
                                "onFailure(): \n"
                                +(null != response ? response.getResponseText(): "")+"\n"
                                +(null != t ? t.toString(): "")+"\n"
                                +(null != extendedInfo ? extendedInfo.toString(): "")+"\n";

                        Log.e(TAG, errorMessage);
                    }
                });
                }
            });
        }
    }
    
}

