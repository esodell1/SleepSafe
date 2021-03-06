package com.sleepsafe.iot.devices.sleepsafe.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.fragments.Spo2ActivityFragment;
import com.sleepsafe.iot.devices.sleepsafe.fragments.Spo2AlarmFragment;
import com.sleepsafe.iot.devices.sleepsafe.fragments.Spo2HistoryFragment;

/**
 * This class implements the main view for the blood oxygen display activity.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class Spo2Activity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spo2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_spo2);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = new Spo2ActivityFragment();
                    break;
                case 1:
                    fragment = new Spo2HistoryFragment();
                    break;
                case 2:
                    fragment = new Spo2AlarmFragment();
                    break;
                default:
                    throw new IllegalArgumentException("Position " + position + " is not a valid argument.");
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
}
