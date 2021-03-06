package com.sleepsafe.iot.devices.sleepsafe.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.sleepsafe.iot.devices.sleepsafe.R;

import java.util.Locale;

/**
 * This class implements the main view for the blood oxygen alarms page. This includes linking
 * the shared preferences for high and low set points to the view, as well as enable and disable
 * for each.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class Spo2AlarmFragment extends Fragment {
    private static final int BASE_SPO2_VALUE = 40;
    private static final int MAX_SPO2_VALUE = 180;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Spo2AlarmFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView;
        rootView = inflater.inflate(R.layout.fragment_alarms, container, false);
        final SharedPreferences mPref = getActivity().getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
        int maxSpo2 = mPref.getInt(getString(R.string.pref_alarm_max_spo2), 120);
        int minSpo2 = mPref.getInt(getString(R.string.pref_alarm_min_spo2), 50);

        final Switch enableMax = (Switch) rootView.findViewById(R.id.alarm_enable_max);
        final Switch enableMin = (Switch) rootView.findViewById(R.id.alarm_enable_min);

        final TextView maxTitle = (TextView) rootView.findViewById(R.id.alarm_max_title);
        final TextView minTitle = (TextView) rootView.findViewById(R.id.alarm_min_title);

        final Button minButton = (Button)rootView.findViewById(R.id.alarm_min_button);
        minButton.setText(String.format(Locale.US, "%d", minSpo2));
        minButton.setOnClickListener(new AlarmNumberPicker(getString(R.string.pref_alarm_min_spo2)));
        final Button maxButton = (Button)rootView.findViewById(R.id.alarm_max_button);
        maxButton.setText(String.format(Locale.US, "%d", maxSpo2));
        maxButton.setOnClickListener(new AlarmNumberPicker(getString(R.string.pref_alarm_max_spo2)));

        enableMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("Switch", "Switch changed to " + enableMax.isChecked());
                maxTitle.setEnabled(enableMax.isChecked());
                maxButton.setEnabled(enableMax.isChecked());
                mPref.edit().putBoolean(getString(R.string.pref_alarm_enable_max_spo2), enableMax.isChecked()).apply();
            }
        });

        // initial setup
        final boolean maxEnable = mPref.getBoolean(getString(R.string.pref_alarm_enable_max_spo2), false);
        enableMax.setChecked(maxEnable);
        maxTitle.setEnabled(maxEnable);
        maxButton.setEnabled(maxEnable);
        final boolean minEnable = mPref.getBoolean(getString(R.string.pref_alarm_enable_min_spo2), false);
        enableMin.setChecked(minEnable);
        minTitle.setEnabled(minEnable);
        minButton.setEnabled(minEnable);

        enableMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("Switch", "Switch changed to " + enableMax.isChecked());
                minTitle.setEnabled(enableMin.isChecked());
                minButton.setEnabled(enableMin.isChecked());
                mPref.edit().putBoolean(getString(R.string.pref_alarm_enable_min_spo2), enableMin.isChecked()).apply();
            }
        });



        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public class AlarmNumberPicker implements View.OnClickListener {

        final String mPrefString;

        public AlarmNumberPicker(String prefs) {
            mPrefString = prefs;
        }

        @Override
        public void onClick(final View v) {
            RelativeLayout linearLayout = new RelativeLayout(getActivity());
            final NumberPicker aNumberPicker = new NumberPicker(getActivity());
            final SharedPreferences mPref = getActivity().getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
            int currentValue = mPref.getInt(mPrefString, ((MAX_SPO2_VALUE - BASE_SPO2_VALUE) / 2) + BASE_SPO2_VALUE);

            aNumberPicker.setMaxValue(MAX_SPO2_VALUE);
            aNumberPicker.setMinValue(BASE_SPO2_VALUE);
            aNumberPicker.setValue(currentValue);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
            RelativeLayout.LayoutParams numPickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            numPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            linearLayout.setLayoutParams(params);
            linearLayout.addView(aNumberPicker,numPickerParams);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle("Select a set point");
            alertDialogBuilder.setView(linearLayout);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    Log.v("AlarmFragment", "New Setpoint Value: "+ aNumberPicker.getValue());
                                    mPref.edit().putInt(mPrefString, aNumberPicker.getValue()).apply();
                                    ((Button) v).setText(String.format(Locale.US, "%d", aNumberPicker.getValue()));
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
}
