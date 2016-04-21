package edu.uw.tacoma.esodell.sleepsafe.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.ArraySet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.uw.tacoma.esodell.sleepsafe.R;
import edu.uw.tacoma.esodell.sleepsafe.activities.DashboardActivity;


public class MonitorSvc extends IntentService {
    private static final String TAG = "SleepSafeMonitorSvc";
    public static final String ACTION_START_SERVICE = "start_svc";
    public static final String ACTION_STOP_SERVICE = "stop_svc";

    protected static boolean SERVICE_RUNNING = false;

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
                samples.add(sample);

                Intent broadcast = new Intent();
                broadcast.setAction("new_sample");
                broadcast.putExtra("hr", sample.hr_val);
                broadcast.putExtra("spo2", sample.spo2_val);
                broadcast.putExtra("temp", sample.temp_val);
                sendBroadcast(broadcast);
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }



    private void stopSvc() {
        SERVICE_RUNNING = false;
        unregisterReceiver(mReceiver);
        Log.v(TAG, "Service terminated for user: " + user);
    }




}
