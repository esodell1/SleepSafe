package com.sleepsafe.iot.devices.sleepsafe.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.helper.HistoryDBProvider;
import com.sleepsafe.iot.devices.sleepsafe.helper.Sample;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This class implements the main view for the heart rate activity page. This includes
 * the graph, point selection, and data view.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
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

        // Listen for new sample broadcasts
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
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
                        mHRActivity.moveViewToX(data.getXValCount());
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
        storeSelectedData();
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
        ArrayList<Sample> samples = (ArrayList<Sample>) mDB.getCurrentSessionSamples();
        ArrayList<Entry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < samples.size(); i++) {
            xVals.add((new SimpleDateFormat("KK:mm:ss a MM/dd/yyyy", Locale.US))
                    .format(new Date(samples.get(i).timestamp.getTime())));
            yVals.add(new Entry(samples.get(i).hr_val, i));
            yVals.get(i).setXIndex(i);
        }

        mHRActivity.setBackground(getResources().getDrawable(R.drawable.graph_bg));
        mHRActivity.setDescription("");
        mHRActivity.setDrawGridBackground(false);
        mHRActivity.setOnChartValueSelectedListener(this);
        mHRActivity.getLegend().setEnabled(false);
        mHRActivity.animateXY(1000, 1000);
        mHRActivity.setHorizontalScrollBarEnabled(true);
        if (!settings.getBoolean("pref_graph_draw_grid", true)) {
            XAxis xaxis = mHRActivity.getXAxis();
            xaxis.setDrawGridLines(false);
            xaxis.setEnabled(false);
            YAxis leftAxis = mHRActivity.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setEnabled(false);
            YAxis rightAxis = mHRActivity.getAxisRight();
            rightAxis.setDrawGridLines(false);
            rightAxis.setEnabled(false);
        }

        if (mHRActivity.getLineData() == null) {
            set1 = new LineDataSet(yVals, "Heart Rate");
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            if (settings.getString("pref_graph_scheme", "").equals("default")) {
                set1.setColor(getResources().getColor(R.color.graphLine));
                set1.setCircleColor(Color.WHITE);
            } else if (settings.getString("pref_graph_scheme", "").equals("high_contrast")) {
                mHRActivity.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                set1.setColor(Color.BLACK);
                set1.setCircleColor(Color.RED);
            }
            set1.setLineWidth(3f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(true);
            set1.setDrawValues(false);
            set1.setDrawCubic(settings.getBoolean("pref_graph_draw_cubic", true));



            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
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
            mPointTime.setText(mHRActivity.getXValue(e.getXIndex()));

            storeSelectedData();
        }
    }

    private void storeSelectedData() {
        //Store selected point on graph to shared preferences to
        //later send this data to a friend via text message
        SharedPreferences myData;
        SharedPreferences.Editor editor;
        myData = mPointValue.getContext().getSharedPreferences("SelectedPoint", Context.MODE_PRIVATE);
        editor = myData.edit();

        editor.putString("HR", mPointValue.getText().toString());
        editor.putString("Time", mPointTime.getText().toString());
        editor.apply();
    }
    @Override
    public void onNothingSelected() {
        mPointValue.setText(getString(R.string.default_db_value));
        mPointTime.setText(getString(R.string.default_db_value));

        storeSelectedData();
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

