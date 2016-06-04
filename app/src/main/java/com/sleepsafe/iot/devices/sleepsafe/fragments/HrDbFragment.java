package com.sleepsafe.iot.devices.sleepsafe.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.sleepsafe.iot.devices.sleepsafe.R;


/**
 * This is the fragment class for the heart rate dashboard object. This shows the current heart
 * rate to the user, as well as a sliding scale which reflects the range.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class HrDbFragment extends Fragment {


    public HrDbFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hr_db, container, false);
        SeekBar gauge = (SeekBar) view.findViewById(R.id.hr_seekbar);
        gauge.setMax(140);
        gauge.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int originalProgress;
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Nothing here..
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                originalProgress = seekBar.getProgress();
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int arg1, boolean fromUser) {
                if(fromUser){
                    seekBar.setProgress( originalProgress);
                }
            }
        });
        return view;
    }

}
