package com.sleepsafe.iot.devices.sleepsafe.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.helper.HistoryDBProvider;

public class HrHistoryFragment extends Fragment {
    private LineChart mHRActivity;
    private HistoryDBProvider mDB;

    public HrHistoryFragment() {
        super();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
