package com.sleepsafe.iot.devices.sleepsafe.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.mikephil.charting.charts.LineChart;
import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.helper.HistoryDBProvider;


public class HrAlarmFragment extends Fragment {
    private LineChart mHRActivity;
    private HistoryDBProvider mDB;

    public HrAlarmFragment() {
        super();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView;

        rootView = inflater.inflate(R.layout.fragment_alarms, container, false);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.hr_add_alarm);
        final ListView alarmList = (ListView) rootView.findViewById(R.id.alarms_hr_list);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Create New Alarm");
                    builder.setIcon(android.R.drawable.ic_lock_idle_alarm);
                    builder.setSingleChoiceItems(R.array.alarm_add, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.setView(getActivity().getLayoutInflater().inflate(R.layout.alarm_dialog, null));
                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            Log.v("Alerts", "dialog.tostring: " + dialog.toString());

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });


                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
