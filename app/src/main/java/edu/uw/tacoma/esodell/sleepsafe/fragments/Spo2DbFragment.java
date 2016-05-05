package edu.uw.tacoma.esodell.sleepsafe.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uw.tacoma.esodell.sleepsafe.R;


/**
 * A simple {@link Fragment} subclass.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class Spo2DbFragment extends Fragment {


    public Spo2DbFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spo2_db, container, false);
    }

}
