package com.sleepsafe.iot.devices.sleepsafe.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.activities.HrActivity;
import com.sleepsafe.iot.devices.sleepsafe.helper.Alarm;
import com.sleepsafe.iot.devices.sleepsafe.helper.AlarmListAdapter;
import com.sleepsafe.iot.devices.sleepsafe.helper.HistoryDBProvider;


public class HrAlarmFragment extends Fragment {
    private HistoryDBProvider mDB;
    private AlarmListAdapter mAlarmListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlarmListAdapter = new AlarmListAdapter(getActivity().getApplicationContext(), R.layout.alarm_list_item);
    }

    public HrAlarmFragment() {
        super();
    }

    private void addAlarm(int value, boolean lessThan) {
        mAlarmListAdapter.add(new Alarm(value, lessThan));
        mAlarmListAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView;
        rootView = inflater.inflate(R.layout.fragment_alarms, container, false);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.hr_add_alarm);
        final ListView alarmList = (ListView) rootView.findViewById(R.id.alarms_hr_list);
        alarmList.setAdapter(mAlarmListAdapter);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Create New Alarm");
                    builder.setIcon(android.R.drawable.ic_lock_idle_alarm);
                    final View layout = getActivity().getLayoutInflater().inflate(R.layout.alarm_dialog, null);
                    builder.setView(layout);
                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            EditText val = (EditText)layout.findViewById(R.id.alarm_value);
                            Log.v("ALARM VALUE ", "VALUE: " + val.getText().toString());
                            RadioButton lessThan = (RadioButton)layout.findViewById(R.id.alarm_value_less);
                            addAlarm(Integer.parseInt(val.getText().toString()), lessThan.isChecked());
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
