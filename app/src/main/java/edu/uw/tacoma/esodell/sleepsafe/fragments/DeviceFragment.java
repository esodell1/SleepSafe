package edu.uw.tacoma.esodell.sleepsafe.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.uw.tacoma.esodell.sleepsafe.R;
import edu.uw.tacoma.esodell.sleepsafe.network.NsdHelper;


public class DeviceFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private NsdHelper mNSD;
    private List<Device> mDevices;

    public DeviceFragment() {
        // Required empty public constructor
    }

    public static DeviceFragment newInstance() {
        return new DeviceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevices = new ArrayList<>();
        mDevices.add(new Device("SleepSafe", "192.168.0.1"));
        mNSD = new NsdHelper(this.getContext());
        mNSD.initializeNsd();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_device, container, false);

        ListView list = (ListView) result.findViewById(R.id.deviceListView);
        list.setAdapter(new DeviceListAdapter(getContext(), R.layout.device_list_item, mDevices));
        return result;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mNSD.discoverServices();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class Device {

        public String deviceName;
        public String deviceIP;

        public Device(String deviceName, String deviceIP) {
            this.deviceName = deviceName;
            this.deviceIP = deviceIP;
        }
    }

    private class DeviceListAdapter extends ArrayAdapter<Device> {

        private ArrayList<Device> devices;

        public DeviceListAdapter(Context context, int resource, List<Device> objects) {
            super(context, resource, objects);
            this.devices = (ArrayList<Device>) objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.device_list_item, null);
            }
            Device devItem = devices.get(position);
            if (devItem != null) {
                TextView username = (TextView) v.findViewById(R.id.devices_service_name);
                TextView email = (TextView) v.findViewById(R.id.devices_service_ip);
                if (username != null) {
                    username.setText(devItem.deviceName);
                }
                if(email != null) {
                    email.setText(devItem.deviceIP );
                }
            }
            return v;
        }
    }
}
