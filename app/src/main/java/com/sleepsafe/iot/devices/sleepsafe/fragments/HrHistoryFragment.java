package com.sleepsafe.iot.devices.sleepsafe.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.helper.HistoryDBProvider;
import com.sleepsafe.iot.devices.sleepsafe.helper.Sample;
import com.sleepsafe.iot.devices.sleepsafe.helper.Session;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HrHistoryFragment extends Fragment {
    private LineChart mHRActivity;
    private HistoryDBProvider mDB;
    private HistoryListAdapter mAdapter;
    private ListView mList;

    public HrHistoryFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        mList = (ListView) rootView.findViewById(R.id.history_list);
        TextView tv = (TextView) rootView.findViewById(R.id.empty_history_list);
        mList.setEmptyView(tv);
        mDB = new HistoryDBProvider(this.getContext());
        return rootView;
    }

    @Override
    public void onResume() {
        List<Session> sessions = mDB.getSessions();
        mAdapter = new HistoryListAdapter(getActivity(), R.layout.fragment_history_list_item, sessions);
        mList.setAdapter(mAdapter);
        super.onResume();
    }

    public class HistoryListAdapter extends ArrayAdapter<Session> {

        private Context mContext;

        public HistoryListAdapter(Context context, int resource, List<Session> objects) {
            super(context, resource, objects);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.fragment_history_list_item, parent, false);
            TextView nameTextView = (TextView) rowView.findViewById(R.id.history_item_name);
            TextView avgTextView = (TextView) rowView.findViewById(R.id.history_item_average);
            LineChart chart = (LineChart) rowView.findViewById(R.id.history_item_chart);
            Session session = getItem(position);
            String start = (new SimpleDateFormat("KK:mm:ss a M/d/yyyy", Locale.US))
                    .format(session.getmStart());
            String end = (new SimpleDateFormat("KK:mm:ss a M/d/yyyy", Locale.US))
                    .format(session.getmEnd());
            nameTextView.setText("From " + start + " to " + end);
            avgTextView.setText("Session " + (position + 1));

            populateGraph(chart, session);
            return rowView;
        }

        private void populateGraph(LineChart chart, Session session) {
            // Populate graph data and display
            LineDataSet set1;
            List<Sample> samples = session.getmSessionData();
            ArrayList<Entry> yVals = new ArrayList<>();
            ArrayList<String> xVals = new ArrayList<>();
            for (int i = 0; i < samples.size(); i++) {
                xVals.add((new SimpleDateFormat("KK:mm:ss a MM/dd/yyyy", Locale.US))
                        .format(new Date(samples.get(i).timestamp.getTime())));
                yVals.add(new Entry(samples.get(i).hr_val, i));
                yVals.get(i).setXIndex(i);
            }

            //mHRActivity.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            chart.setDescription("");
            chart.setTouchEnabled(false);
            chart.setDragEnabled(false);
            chart.setScaleEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setPinchZoom(false);
            chart.getLegend().setEnabled(false);
            XAxis xaxis = chart.getXAxis();
            xaxis.setDrawGridLines(false);
            xaxis.setEnabled(false);
            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setEnabled(false);
            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setDrawGridLines(false);
            rightAxis.setEnabled(false);

            if (chart.getLineData() == null) {
                set1 = new LineDataSet(yVals, "Heart Rate");
                //set1.enableDashedHighlightLine(10f, 5f, 0f);
                set1.setColor(getResources().getColor(R.color.graphLine));
                set1.setDrawValues(false);

                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSets.add(set1); // add the datasets

                // create a data object with the datasets
                LineData data = new LineData(xVals, dataSets);

                // set data
                chart.setData(data);
            }
        }
    }
}
