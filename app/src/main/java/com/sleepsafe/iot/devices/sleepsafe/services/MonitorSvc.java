package com.sleepsafe.iot.devices.sleepsafe.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.activities.DashboardActivity;
import com.sleepsafe.iot.devices.sleepsafe.helper.HistoryDBProvider;
import com.sleepsafe.iot.devices.sleepsafe.helper.Sample;

/**
 * This class implements the communication service that handles requests to and from the SleepSafe
 * wearable device. This service will run in the foreground, continually poll the device, and
 * monitor set points of alarms that will actuate on user specified conditions.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class MonitorSvc extends IntentService {
    public static boolean SERVICE_RUNNING = false;
    public static final String ACTION_START_SERVICE = "start_svc";
    public static final String ACTION_STOP_SERVICE = "stop_svc";
    private static final String TAG = "SleepSafeMonitorSvc";
    private static final String REQUEST_SAMPLE = "sample";
    private HistoryDBProvider mDBProvider;

    // URL of emulator server (to be replaced by device):
    private String BASE_URL = "http://192.168.1.12:80/";
    private InetAddress mDeviceIP;

    private String user = null;
    private BroadcastReceiver mReceiver;
    private static List<Sample> samples;

    public MonitorSvc() {
        super("SleepSafeMonitorSvc");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Attach a broadcast receiver to this service to listen for
        // a stop request:
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ACTION_STOP_SERVICE)) {
                    stopSelf();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP_SERVICE);
        registerReceiver(mReceiver,filter);

        if (intent != null) {
            final String action = intent.getAction();

            if (action.equals(ACTION_START_SERVICE)) {
                // Retrieve the network address and port from the calling Intent:
                final String device_ip = intent.getStringExtra("device_ip");
                final int device_port = intent.getIntExtra("device_port", 80);
                if (device_ip != null) {
                    BASE_URL = "http:/" + device_ip + ":" + device_port + "/";
                }
                Log.v(TAG, "Resolved device info: " + device_ip);
                startSvc(intent.getStringExtra("user"));
            }
        }
    }

    @Override
    public void onDestroy() {
        stopSvc();
        super.onDestroy();
    }

    /**
     * Begins the service.
     * @param user the user for which the service is running
     */
    private void startSvc(String user) {
        this.user = user;
        SERVICE_RUNNING = true;
        Log.v(TAG, "Service started for user: " + user);

        Intent broadcast = new Intent();
        broadcast.setAction("service_running");
        sendBroadcast(broadcast);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_sync_black_24dp);
        builder.setContentTitle("Monitor Service");
        builder.setContentText("Service is running.");

        Intent notificationIntent = new Intent(getApplicationContext(), DashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        builder.setFullScreenIntent(pendingIntent, true);

        startForeground(1, builder.build());


        samples = new ArrayList<>();
        mDBProvider = new HistoryDBProvider(this);

        if (user == null || user.equals("Guest")) {
            while (SERVICE_RUNNING) {
//                Sample sample = new Sample((int)(70 + (Math.random() * 40)), (int)(90 + (Math.random() * 10)), 90);
//                newSample(sample);

                DeviceRequest request = new DeviceRequest();
                request.execute(REQUEST_SAMPLE);

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            while (SERVICE_RUNNING) {
//                Sample sample = new Sample((int)(70 + (Math.random() * 40)), (int)(90 + (Math.random() * 10)), 90);
//                newSample(sample);

//                DeviceRequest request = new DeviceRequest();
//                request.execute(REQUEST_SAMPLE);


                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * This method broadcasts a new sample being retrieved to the database and the Dashboard.
     * @param sample The Sample object to broadcast
     */
    private void newSample(Sample sample) {
        // Local store of sample
        samples.add(sample);

        mDBProvider.insertSample(sample);

        // Broadcast new sample
        Intent broadcast = new Intent();
        broadcast.setAction("new_sample");
        broadcast.putExtra("hr", sample.hr_val);
        broadcast.putExtra("spo2", sample.spo2_val);
        broadcast.putExtra("temp", sample.temp_val);
        sendBroadcast(broadcast);
    }

    /**
     * Stops the service. Should only be called by onDestroy().
     */
    private void stopSvc() {
        SERVICE_RUNNING = false;
        unregisterReceiver(mReceiver);
        mDBProvider.closeDB();
        Log.v(TAG, "Service terminated for user: " + user);
    }

    /**
     * This class provides an asynchronous HTTP request helper so the service may query the device.
     */
    private class DeviceRequest extends AsyncTask<String, Void, Sample> {

        @Override
        protected Sample doInBackground(String... params) {

            if (params.length == 0) return null;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonStr = null;

            try {
                if (params[0] == null) return null;

                Uri builtUri = Uri.parse(BASE_URL + params[0]);

                Log.v(TAG, "BASE_URL: " + BASE_URL);

                URL url = new URL(builtUri.toString());
                Log.v(TAG, "URI: " + url.toString());

                // Create the request to the device, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            Sample result;
            try {
                result = getSampleFromJSON(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            Log.v(TAG, jsonStr);
            return result;
        }

        private Sample getSampleFromJSON(String jsonString) throws JSONException {
            final String HR_DATA = "HR";
            final String SPO2_DATA = "SpO2";
            final String TEMP_DATA = "Temp";
            final String val = "value";

            JSONObject result = new JSONObject(jsonString);
            int hr = result.getJSONObject(HR_DATA).getInt(val);
            int spo2 = result.getJSONObject(SPO2_DATA).getInt(val);
            int temp = result.getJSONObject(TEMP_DATA).getInt(val);
            return new Sample (hr, spo2, temp);
        }

        @Override
        protected void onPostExecute(Sample result) {
            if (result != null) {
                Log.v(TAG, result.toString());
                newSample(result);
            }
        }
    }


}
