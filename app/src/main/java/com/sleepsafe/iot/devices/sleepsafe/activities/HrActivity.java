package com.sleepsafe.iot.devices.sleepsafe.activities;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;

import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.helper.HistoryDBProvider;

/**
 * This class implements the main view for the heart rate display activity.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class HrActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hr, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A fragment containing a a paged view.
     */
    public static class HrPageFragment extends Fragment {

        private LineChart mHRActivity;
        private HistoryDBProvider mDB;
        private static final String ARG_SECTION_NUMBER = "section_number";

        public HrPageFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static HrPageFragment newInstance(int sectionNumber) {
            HrPageFragment fragment = new HrPageFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;
            // Inflate different views depending on the section
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_hr_activity, container, false);
                    mHRActivity = (LineChart) rootView.findViewById(R.id.chart_hr_activity);
                    mDB = new HistoryDBProvider(this.getContext());
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_hr_history, container, false);
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_hr_alarms, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_hr, container, false);
                    break;
            }
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    // Populate graph data and display
                    LineDataSet set1;
                    ArrayList<Entry> yVals = (ArrayList<Entry>) mDB.getHRSamples();

                    ArrayList<String> xVals = new ArrayList<String>();
                    for (int i = 0; i < yVals.size(); i++) {
                        xVals.add((i) + "");
                        yVals.get(i).setXIndex(i);
                    }

                    mHRActivity.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mHRActivity.setDescription("");
                    mHRActivity.setDrawGridBackground(false);
                    mHRActivity.getLegend().setEnabled(false);
                    mHRActivity.animateXY(1000, 1000);


                    if(mHRActivity.getLineData() == null) {
                        set1 = new LineDataSet(yVals, "Heart Rate");
                        set1.enableDashedHighlightLine(10f, 5f, 0f);
                        set1.setColor(getResources().getColor(R.color.colorPrimary));
                        set1.setCircleColor(Color.WHITE);
                        set1.setLineWidth(3f);
                        set1.setCircleRadius(3f);
                        set1.setDrawCircleHole(true);
                        set1.setDrawValues(false);
                        set1.setDrawCubic(true);


                        if (Utils.getSDKInt() >= 18) {
                            // fill drawable only supported on api level 18 and above
                            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.border);
                            set1.setFillDrawable(drawable);
                        }
                        else {
                            set1.setFillColor(Color.BLACK);
                        }

                        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                        dataSets.add(set1); // add the datasets

                        // create a data object with the datasets
                        LineData data = new LineData(xVals, dataSets);

                        // set data
                        mHRActivity.setData(data);
                    }

                    break;
                case 2:

                    break;
                case 3:

                    break;
                default:

                    break;
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a HrPageFragment (defined as a static inner class below).
            return HrPageFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Activity";
                case 1:
                    return "History";
                case 2:
                    return "Alarms";
            }
            return null;
        }
    }
}
