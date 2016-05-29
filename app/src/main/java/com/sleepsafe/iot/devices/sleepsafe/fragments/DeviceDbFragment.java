package com.sleepsafe.iot.devices.sleepsafe.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.activities.DashboardActivity;
import com.sleepsafe.iot.devices.sleepsafe.helper.FirmwareOTA;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class DeviceDbFragment extends Fragment {

    private FirmwareOTA mFW;

    public DeviceDbFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_db, container, false);
        mFW = new FirmwareOTA(getContext());

        ImageButton refresh = (ImageButton) view.findViewById(R.id.db_device_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFW.executeUpdate();
            }
        });
        return view;

    }

}
