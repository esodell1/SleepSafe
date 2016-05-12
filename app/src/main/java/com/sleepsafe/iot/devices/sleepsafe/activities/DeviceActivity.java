package com.sleepsafe.iot.devices.sleepsafe.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.fragments.DeviceFragment;

/**
 * This class implements the main view selecting the SleepSafe device
 * from a list generated by Network Service Discovery (mDNS)
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class DeviceActivity extends AppCompatActivity implements DeviceFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}