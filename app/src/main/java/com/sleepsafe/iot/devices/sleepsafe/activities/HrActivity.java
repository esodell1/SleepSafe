package com.sleepsafe.iot.devices.sleepsafe.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
    private static final int PERMISSION_SEND_SMS = 3523;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        MenuItem item = menu.findItem(R.id.menu_share);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (ContextCompat.checkSelfPermission(HrActivity.this,
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(HrActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            PERMISSION_SEND_SMS);
                } else {
                    showSMSDialog();
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSMSDialog();

                } else {
                    Toast.makeText(this, "Cannot send SMS without permissions.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showSMSDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HrActivity.this);
        builder.setTitle("Share Heart Rate Session");
        builder.setIcon(android.R.drawable.ic_menu_share);
        builder.setMessage("Please enter recipient Phone Number: ");//email: ");
        final EditText text = new EditText(HrActivity.this);
        builder.setView(text);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v("SHARE", "Share with " + text.getText());

                SharedPreferences myData;

                myData = getSharedPreferences("SelectedPoint", Context.MODE_PRIVATE);
                String mHr = myData.getString("HR", null);
                String mTime = myData.getString("Time", null);

                SmsManager smsManager = SmsManager.getDefault();

                if (!mHr.equals("--")) {
                    String message = "My HR was " + mHr + " at " + mTime + ".";
                    smsManager.sendTextMessage(text.getText().toString(), null, message, null, null);
                    Log.v("Point was selected", mHr);
                } else {
                    Log.v("Point wasn't selected", mHr);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

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
