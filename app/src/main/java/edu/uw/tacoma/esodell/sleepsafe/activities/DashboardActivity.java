package edu.uw.tacoma.esodell.sleepsafe.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.uw.tacoma.esodell.sleepsafe.R;
import edu.uw.tacoma.esodell.sleepsafe.services.MonitorSvc;

public class DashboardActivity extends AppCompatActivity {

    private Button start_button;
    private Button stop_button;
    public String user;
    private BroadcastReceiver mReceiver;
    private Intent mService;
    private SharedPreferences mSharedPref;
    private TextView mDeviceName;
    private TextView mDeviceIP;
    private TextView mDevicePort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSharedPref = getSharedPreferences(getString(R.string.pref_device_key), Context.MODE_PRIVATE);

        user = getIntent().getAction();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Loading device discovery...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    startActivity(new Intent(getApplicationContext(), DeviceActivity.class));
                }
            });
        }

        View hrdb_fragment = findViewById(R.id.hrdb);
        if (hrdb_fragment != null) {
            hrdb_fragment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), HrActivity.class));
                }
            });
        }

        View spo2db_fragment = findViewById(R.id.spo2db);
        if (spo2db_fragment != null) {
            spo2db_fragment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), Spo2Activity.class));
                }
            });
        }

        start_button = (Button) findViewById(R.id.button_start);
        stop_button = (Button) findViewById(R.id.button_stop);
        mDeviceName = (TextView) findViewById(R.id.db_device_name);
        mDeviceIP = (TextView) findViewById(R.id.db_device_ip);
        mDevicePort = (TextView) findViewById(R.id.db_device_port);

        View deviceDisplay = findViewById(R.id.devicedb);
        deviceDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DeviceActivity.class));
            }
        });

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService = new Intent(getApplicationContext(), MonitorSvc.class);
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

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("new_sample")) {
                    int hr, spo2, temp;
                    hr = intent.getIntExtra("hr", 0);
                    spo2 = intent.getIntExtra("spo2", 0);
                    temp = intent.getIntExtra("temp", 0);
                    TextView hr_val = (TextView) findViewById(R.id.hr_value);
                    TextView spo2_val = (TextView) findViewById(R.id.spo2_value);
                    if (hr_val != null) hr_val.setText(Integer.toString(hr));
                    if (spo2_val != null) spo2_val.setText(Integer.toString(spo2));
                    if (start_button.isEnabled()) start_button.setEnabled(false);
                    if (!stop_button.isEnabled()) stop_button.setEnabled(true);
                } else if (intent.getAction().equals("service_running")) {
                    start_button.setEnabled(false);
                    stop_button.setEnabled(true);
                }

            }
        };


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
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
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("new_sample");
        filter.addAction("service_running");
        registerReceiver(mReceiver,filter);
        String ip = mSharedPref.getString(getString(R.string.pref_device_ip), "0.0.0.0");
        mDeviceIP.setText(ip);
        int port = mSharedPref.getInt(getString(R.string.pref_device_port), 80);
        mDevicePort.setText(String.valueOf(port));
        String name = mSharedPref.getString(getString(R.string.pref_device_name), "No Device Selected");
        mDeviceName.setText(name);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }


}
