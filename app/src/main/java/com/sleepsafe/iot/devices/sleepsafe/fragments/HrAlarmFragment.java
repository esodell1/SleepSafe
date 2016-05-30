package com.sleepsafe.iot.devices.sleepsafe.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.sleepsafe.iot.devices.sleepsafe.R;


public class HrAlarmFragment extends Fragment {
    private static final int BASE_HR_VALUE = 40;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public HrAlarmFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView;
        rootView = inflater.inflate(R.layout.fragment_alarms, container, false);
        final SharedPreferences mPref = getActivity().getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
        int maxHR = mPref.getInt(getString(R.string.pref_alarm_max_hr), 120);
        int minHR = mPref.getInt(getString(R.string.pref_alarm_min_hr), 50);
        final Switch enableMax = (Switch) rootView.findViewById(R.id.alarm_enable_max);
        final Switch enableMin = (Switch) rootView.findViewById(R.id.alarm_enable_min);
        final TextView maxTitle = (TextView) rootView.findViewById(R.id.alarm_max_title);
        final TextView minTitle = (TextView) rootView.findViewById(R.id.alarm_min_title);
        final NumberPicker maxText = (NumberPicker) rootView.findViewById(R.id.alarm_max_text);
        final NumberPicker minText = (NumberPicker) rootView.findViewById(R.id.alarm_min_text);

        maxText.setMinValue(40);
        maxText.setValue(maxHR);
        maxText.setMaxValue(180);
        Log.v("MAX", "" + maxHR);
        //maxText.setEnabled(false);

        minText.setMinValue(40);
        minText.setValue(minHR);
        minText.setMaxValue(180);
        Log.v("MIN", "" + minHR);
        //minText.setEnabled(false);

        enableMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("Switch", "Switch changed to " + enableMax.isChecked());
                maxTitle.setEnabled(enableMax.isChecked());
                maxText.setEnabled(enableMax.isChecked());
            }
        });

        enableMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("Switch", "Switch changed to " + enableMax.isChecked());
                minTitle.setEnabled(enableMin.isChecked());
                minText.setEnabled(enableMin.isChecked());
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
