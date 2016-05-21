package com.sleepsafe.iot.devices.sleepsafe.activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.fragments.HrActivityFragment;
import com.sleepsafe.iot.devices.sleepsafe.fragments.HrAlarmFragment;
import com.sleepsafe.iot.devices.sleepsafe.fragments.HrHistoryFragment;

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
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container_hr);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_hr);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }



    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Context mContext;

        public SectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = new HrActivityFragment();
                    break;
                case 1:
                    fragment = new HrHistoryFragment();
                    break;
                case 2:
                    fragment = new HrAlarmFragment();
                    break;
                default:
                    throw new IllegalArgumentException("position " + position + " is not valid.");
            }
            return fragment;
        }

        @Override
        public int getCount() {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }
}
