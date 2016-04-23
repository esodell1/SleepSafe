package edu.uw.tacoma.esodell.sleepsafe.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.uw.tacoma.esodell.sleepsafe.R;
import edu.uw.tacoma.esodell.sleepsafe.activities.DashboardActivity;


public class MonitorSvc extends IntentService {
    private static final String TAG = "SleepSafeMonitorSvc";
    public static final String ACTION_START_SERVICE = "start_svc";
    public static final String ACTION_STOP_SERVICE = "stop_svc";

    public static boolean SERVICE_RUNNING = false;

    private String user = null;
    private BroadcastReceiver mReceiver;
    private static List<Sample> samples;

    public MonitorSvc() {
        super("SleepSafeMonitorSvc");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

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
                startSvc(intent.getStringExtra("user"));
            }
        }
    }

    @Override
    public void onDestroy() {
        stopSvc();
        super.onDestroy();
    }

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

        if (user.equals("Guest")) {
            while (SERVICE_RUNNING) {
                Sample sample = new Sample((int)(70 + (Math.random() * 40)), (int)(90 + (Math.random() * 10)), 90);
                newSample(sample);

                DeviceRequest request = new DeviceRequest();
                request.execute("98103");


                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void newSample(Sample sample) {
        // Local store of sample
        samples.add(sample);

        // Broadcast new sample
        Intent broadcast = new Intent();
        broadcast.setAction("new_sample");
        broadcast.putExtra("hr", sample.hr_val);
        broadcast.putExtra("spo2", sample.spo2_val);
        broadcast.putExtra("temp", sample.temp_val);
        sendBroadcast(broadcast);
    }

    private void stopSvc() {
        SERVICE_RUNNING = false;
        unregisterReceiver(mReceiver);
        Log.v(TAG, "Service terminated for user: " + user);
    }


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
            String format = "json";
            String units = "imperial";
            String appid = "b0d7fdda0ed3a33c86985bfda8d2d5ce";
            Integer numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String APPID_PARAM = "appid";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(APPID_PARAM, appid)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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

            Log.v(TAG, jsonStr);
            return new Sample(70, 90, 90);
        }

        @Override
        protected void onPostExecute(Sample result) {
            if (result != null) {
                Log.v(TAG, result.toString());
            }
        }
    }


}
