package edu.uw.tacoma.esodell.sleepsafe.fragments;

import android.content.Context;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.uw.tacoma.esodell.sleepsafe.R;
import edu.uw.tacoma.esodell.sleepsafe.network.Device;


public class DeviceFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private NsdHelper mNSD;
    private List<Device> mDevices;
    private DeviceListAdapter mAdapter;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_device, container, false);

        ListView list = (ListView) result.findViewById(R.id.deviceListView);
        mAdapter = new DeviceListAdapter(getContext(), R.layout.device_list_item, mDevices);

        mNSD = new NsdHelper(this.getContext());
        mNSD.initializeNsd();

        list.setAdapter(mAdapter);
        return result;
    }

    private void refreshList() {
        mNSD.discoverServices(mAdapter);
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
        refreshList();
    }

    @Override
    public void onStop() {
        mNSD.stopDiscovery();
        super.onStop();
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

    public class DeviceListAdapter extends ArrayAdapter<Device> {

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

    public class NsdHelper {
        Context mContext;
        NsdManager mNsdManager;
        NsdManager.ResolveListener mResolveListener;
        NsdManager.DiscoveryListener mDiscoveryListener;
        NsdManager.RegistrationListener mRegistrationListener;
        public static final String SERVICE_TYPE = "_http._tcp.";
        public static final String TAG = "SleepSafe";
        public String mServiceName = "SleepSafe";
        NsdServiceInfo mService;
        List<String> mEntries;
        DeviceFragment.DeviceListAdapter mAdapter;

        public NsdHelper(Context context) {
            mContext = context;
            mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        }
        public void initializeNsd() {
            initializeResolveListener();
            //mNsdManager.init(mContext.getMainLooper(), this);
        }
        public void initializeDiscoveryListener() {
            mDiscoveryListener = new NsdManager.DiscoveryListener() {
                @Override
                public void onDiscoveryStarted(String regType) {
                    Log.d(TAG, "Service discovery started: " + regType);
                }
                @Override
                public void onServiceFound(final NsdServiceInfo service) {
                    // TODO: Determine service type and name, store globally
                    Log.d(TAG, String.format("%s %s %s %d",
                            service.getServiceName(), service.getServiceType(),
                            service.getHost(), service.getPort()));

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.add(new Device(service.getServiceName(), service.getServiceName()));
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                    if (mEntries != null) {
                        mEntries.add(service.getServiceName());
                    }

                    if (!service.getServiceType().equals(SERVICE_TYPE)) {
                        Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                    } else if (service.getServiceName().equals(mServiceName)) {
                        Log.d(TAG, "Same machine: " + mServiceName);
//                    mNsdManager.resolveService(service, mResolveListener);
                    } else if (service.getServiceName().contains(mServiceName)){
                        mNsdManager.resolveService(service, mResolveListener);
                    }
                }
                @Override
                public void onServiceLost(NsdServiceInfo service) {
                    Log.e(TAG, "service lost" + service);
                    if (mService == service) {
                        mService = null;
                    }
                }
                @Override
                public void onDiscoveryStopped(String serviceType) {
                    Log.i(TAG, "Discovery stopped: " + serviceType);
                }
                @Override
                public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                }
                @Override
                public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                }
            };
        }
        public void initializeResolveListener() {
            mResolveListener = new NsdManager.ResolveListener() {
                @Override
                public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    Log.e(TAG, "Resolve failed" + errorCode);
                }
                @Override
                public void onServiceResolved(NsdServiceInfo serviceInfo) {
                    Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                    Log.v(TAG, serviceInfo.toString());
                    if (serviceInfo.getServiceName().equals(mServiceName)) {
                        Log.d(TAG, "Same IP.");
                        return;
                    }
                    mService = serviceInfo;
                }
            };
        }
        public void initializeRegistrationListener() {
            mRegistrationListener = new NsdManager.RegistrationListener() {
                @Override
                public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                    mServiceName = NsdServiceInfo.getServiceName();
                    Log.d(TAG, "Service registered: " + mServiceName);
                }
                @Override
                public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                    Log.d(TAG, "Service registration failed: " + arg1);
                }
                @Override
                public void onServiceUnregistered(NsdServiceInfo arg0) {
                    Log.d(TAG, "Service unregistered: " + arg0.getServiceName());
                }
                @Override
                public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    Log.d(TAG, "Service unregistration failed: " + errorCode);
                }
            };
        }
        public void registerService(int port) {
            tearDown();  // Cancel any previous registration request
            initializeRegistrationListener();
            NsdServiceInfo serviceInfo  = new NsdServiceInfo();
            serviceInfo.setPort(port);
            serviceInfo.setServiceName(mServiceName);
            serviceInfo.setServiceType(SERVICE_TYPE);
            mNsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        }
        public void discoverServices(DeviceFragment.DeviceListAdapter adapter) {
            mAdapter = adapter;
            stopDiscovery();  // Cancel any existing discovery request
            initializeDiscoveryListener();
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }
        public void stopDiscovery() {
            if (mDiscoveryListener != null) {
                try {
                    mNsdManager.stopServiceDiscovery(mDiscoveryListener);
                } finally {
                }
                mDiscoveryListener = null;
            }
        }
        public NsdServiceInfo getChosenServiceInfo() {
            return mService;
        }
        public void tearDown() {
            if (mRegistrationListener != null) {
                try {
                    mNsdManager.unregisterService(mRegistrationListener);
                } finally {
                }
                mRegistrationListener = null;
            }
        }
    }
}
