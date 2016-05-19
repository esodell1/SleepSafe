package com.sleepsafe.iot.devices.sleepsafe.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.helper.HistoryDBProvider;
import com.sleepsafe.iot.devices.sleepsafe.helper.Sample;

import java.util.ArrayList;

public class HrActivityFragment extends Fragment implements OnChartValueSelectedListener {

    private LineChart mHRActivity;
    private HistoryDBProvider mDB;
    private TextView mPointValue;
    private TextView mPointTime;

    public HrActivityFragment() {
        super();
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
        mHRActivity.animateXY(1000, 1000);


        if (mHRActivity.getLineData() == null) {
            set1 = new LineDataSet(yVals, "Heart Rate");
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(getResources().getColor(R.color.graphLine));
            set1.setCircleColor(Color.WHITE);
            set1.setLineWidth(3f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(true);
            set1.setDrawValues(false);
            set1.setDrawCubic(true);



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
}
