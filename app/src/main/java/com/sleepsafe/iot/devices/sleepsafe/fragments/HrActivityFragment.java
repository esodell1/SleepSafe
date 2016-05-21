package com.sleepsafe.iot.devices.sleepsafe.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.TimeUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.activities.SettingsActivity;
import com.sleepsafe.iot.devices.sleepsafe.helper.HistoryDBProvider;
import com.sleepsafe.iot.devices.sleepsafe.helper.Sample;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class HrActivityFragment extends Fragment implements OnChartValueSelectedListener {

    private LineChart mHRActivity;
    private HistoryDBProvider mDB;
    private TextView mPointValue;
    private TextView mPointTime;
    private BroadcastReceiver mReceiver;

    public HrActivityFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v("RECEIVE", "RECEIVED : " + intent.toString());
                if (intent.getAction().equals("new_sample")) {
                    LineData data = mHRActivity.getData();
                    if (data != null) {
                        ILineDataSet set = data.getDataSetByIndex(0);
                        // set.addEntry(...); // can be called as well

                        if (set == null) {
                            set = createSet();
                            data.addDataSet(set);
                        }

                        // add a new x-value first
                        data.addXValue(intent.getStringExtra("time"));
                        data.addEntry(new Entry(intent.getIntExtra("hr", 0), set.getEntryCount()), 0);

                        mHRActivity.notifyDataSetChanged();
                        mHRActivity.setVisibleXRangeMaximum(20);
                        mHRActivity.moveViewToX(data.getXValCount() - 21);
                    }

                }

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView;
        rootView = inflater.inflate(R.layout.fragment_activity, container, false);
        mHRActivity = (LineChart) rootView.findViewById(R.id.chart_activity);
        mPointValue = (TextView) rootView.findViewById(R.id.activity_point_value);
        mPointTime = (TextView) rootView.findViewById(R.id.activity_point_time);
        mDB = new HistoryDBProvider(this.getContext());
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("new_sample");
        getActivity().registerReceiver(mReceiver,filter);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // Populate graph data and display
        LineDataSet set1;
        ArrayList<Entry> yVals = (ArrayList<Entry>) mDB.getHRSamples();

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < yVals.size(); i++) {
            xVals.add((i) + "");
            yVals.get(i).setXIndex(i);
        }

        //mHRActivity.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mHRActivity.setBackground(getResources().getDrawable(R.drawable.graph_bg));
        mHRActivity.setDescription("");
        mHRActivity.setDrawGridBackground(false);
        mHRActivity.setOnChartValueSelectedListener(this);
        mHRActivity.getLegend().setEnabled(false);
        mHRActivity.animateX(1000);


        if (mHRActivity.getLineData() == null) {
            set1 = new LineDataSet(yVals, "Heart Rate");
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(getResources().getColor(R.color.graphLine));
            set1.setCircleColor(Color.WHITE);
            set1.setLineWidth(3f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(true);
            set1.setDrawValues(false);
            set1.setDrawCubic(settings.getBoolean("pref_graph_draw_cubic", true));



            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(xVals, dataSets);

            // set data
            mHRActivity.setData(data);
        }

    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (e!= null) {
            mPointValue.setText(String.valueOf(e.getVal()));
            mPointTime.setText(String.valueOf(e.getXIndex()));
        }
    }

    @Override
    public void onNothingSelected() {
        mPointValue.setText(getString(R.string.default_db_value));
        mPointTime.setText(getString(R.string.default_db_value));
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mReceiver);
        super.onPause();
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }
}

