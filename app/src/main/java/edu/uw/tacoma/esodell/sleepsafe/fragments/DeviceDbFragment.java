package edu.uw.tacoma.esodell.sleepsafe.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uw.tacoma.esodell.sleepsafe.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceDbFragment extends Fragment {


    public DeviceDbFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_db, container, false);
    }

}