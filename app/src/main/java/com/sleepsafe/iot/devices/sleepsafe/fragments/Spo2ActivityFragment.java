package com.sleepsafe.iot.devices.sleepsafe.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.sleepsafe.iot.devices.sleepsafe.R;

public class Spo2ActivityFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private LineChart mSpo2Activity;

    public Spo2ActivityFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spo2, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);

        rootView = inflater.inflate(R.layout.fragment_activity, container, false);
        mSpo2Activity = (LineChart) rootView.findViewById(R.id.chart_activity);
//                    mDB = new HistoryDBProvider(this.getContext());

        return rootView;
    }

}
