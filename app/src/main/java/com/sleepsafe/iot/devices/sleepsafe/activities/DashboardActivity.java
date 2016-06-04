package com.sleepsafe.iot.devices.sleepsafe.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.services.MonitorSvc;

import java.util.Locale;

/**
 * This class implements the main view Dashboard for the SleepSafe app.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class DashboardActivity extends AppCompatActivity {

    public String user;
    private Button start_button;
    private Button stop_button;
    private TextView mDeviceName;
    private TextView mDeviceIP;
    private TextView mDevicePort;
    private Intent mService;
    private BroadcastReceiver mReceiver;
    private SharedPreferences mSharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Define SharedPreferences object and get user name
        user = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE).getString(getString(R.string.pref_app_username), "Guest");

        // Bind actions to view objects
        start_button = (Button) findViewById(R.id.button_start);
        stop_button = (Button) findViewById(R.id.button_stop);
        mDeviceName = (TextView) findViewById(R.id.db_device_name);
        mDeviceIP = (TextView) findViewById(R.id.db_device_ip);
        mDevicePort = (TextView) findViewById(R.id.db_device_port);

        // Register click listeners
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Loading device discovery...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    startActivity(new Intent(DashboardActivity.this, DeviceActivity.class));
                }
            });
        }

        View hrdb_fragment = findViewById(R.id.hrdb);
        if (hrdb_fragment != null) {
            hrdb_fragment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(DashboardActivity.this, HrActivity.class));
                }
            });
        }

        View spo2db_fragment = findViewById(R.id.spo2db);
        if (spo2db_fragment != null) {
            spo2db_fragment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(DashboardActivity.this, Spo2Activity.class));
                }
            });
        }

        View deviceDisplay = findViewById(R.id.devicedb);

        if (deviceDisplay != null) {
            deviceDisplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (start_button.isEnabled()) {
                        startActivity(new Intent(DashboardActivity.this, DeviceActivity.class));
                    }
                }
            });
        }

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService = new Intent(DashboardActivity.this, MonitorSvc.class);
                mService.setAction(MonitorSvc.ACTION_START_SERVICE);
                mService.putExtra("user", mSharedPref.getString(getString(R.string.pref_app_username), "Guest"));
                mService.putExtra("device_ip", mSharedPref.getString(getString(R.string.pref_device_ip), "0.0.0.0"));
                mService.putExtra("device_port", mSharedPref.getInt(getString(R.string.pref_device_port), 80));
                startService(mService);
            }
        });

        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent broadcast = new Intent();
                broadcast.setAction(MonitorSvc.ACTION_STOP_SERVICE);
                sendBroadcast(broadcast);
                start_button.setEnabled(true);
                stop_button.setEnabled(false);
            }
        });

        // This defines the handling of updates triggered by the monitor service:
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "new_sample":
                        int hr, spo2, temp;
                        hr = intent.getIntExtra("hr", 0);
                        spo2 = intent.getIntExtra("spo2", 0);
                        temp = intent.getIntExtra("temp", 0);
                        TextView hr_val = (TextView) findViewById(R.id.hr_value);
                        SeekBar hr_gauge = (SeekBar) findViewById(R.id.hr_seekbar);
                        TextView spo2_val = (TextView) findViewById(R.id.spo2_value);
                        SeekBar spo2_gauge = (SeekBar) findViewById(R.id.spo2_seekbar);

                        if (hr_val != null) hr_val.setText(String.format(Locale.US, "%d", hr));
                        if (hr_gauge != null) hr_gauge.setProgress(hr);
                        if (spo2_val != null) spo2_val.setText(String.format(Locale.US, "%d", spo2));
                        if (spo2_gauge != null) spo2_gauge.setProgress(spo2);

                        if (start_button.isEnabled()) start_button.setEnabled(false);
                        if (!stop_button.isEnabled()) stop_button.setEnabled(true);
                        break;
                    case "service_running":
                        start_button.setEnabled(false);
                        stop_button.setEnabled(true);
                        break;
                    case "failed_sample":
                        String msg;
                        if (!intent.getStringExtra("msg").isEmpty())
                            msg = intent.getStringExtra("msg");
                        else
                            msg = "General Error";
                        View frame = findViewById(R.id.app_frame);
                        if (frame != null) Snackbar.make(frame,
                                "Error connecting to device: " + msg, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Intent broadcast = new Intent();
                        broadcast.setAction(MonitorSvc.ACTION_STOP_SERVICE);
                        sendBroadcast(broadcast);
                        start_button.setEnabled(true);
                        stop_button.setEnabled(false);
                        break;
                    default:
                        Log.e("Message", "Unhandled broadcast received!");
                        break;
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Define menu item listeners
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        } else if (id == R.id.action_logout) {
            mSharedPref.edit().putString(getString(R.string.pref_app_username), null).apply();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        // Register the broadcast receiver to listen for service updates:
        mSharedPref = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction("new_sample");
        filter.addAction("service_running");
        filter.addAction("failed_sample");
        registerReceiver(mReceiver,filter);

        // Update device parameters from shared pref
        String ip = mSharedPref.getString(getString(R.string.pref_device_ip), "0.0.0.0");
        Log.v("Dashboard", "IP found: " + ip);
        mDeviceIP.setText(ip);
        int port = mSharedPref.getInt(getString(R.string.pref_device_port), 80);
        mDevicePort.setText(String.valueOf(port));
        Log.v("Dashboard", "Port found: " + port);
        String name = mSharedPref.getString(getString(R.string.pref_device_name), "No Device Selected");
        mDeviceName.setText(name);
        Log.v("Dashboard", "Name found: " + name);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Unregister the receiver (prevents memory leak and other bad things)
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // Close the dashboard
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        DashboardActivity.this.finish();
                    }
                }).create().show();
    }

}
