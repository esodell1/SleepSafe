package com.sleepsafe.iot.devices.sleepsafe.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.sleepsafe.iot.devices.sleepsafe.R;
import com.sleepsafe.iot.devices.sleepsafe.activities.DashboardActivity;
import com.sleepsafe.iot.devices.sleepsafe.helper.FirmwareOTA;

/**
 * This class implements the main fragment for selecting the
 * SleepSafe device from a list generated by Network Service
 * Discovery (mDNS)
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class DeviceFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private NsdHelper mNSD;
    private List<NsdServiceInfo> mDevices;
    private DeviceListAdapter mAdapter;
    private ListView mDeviceList;

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

        // Add adapter to the list view
        mDeviceList = (ListView) result.findViewById(R.id.deviceListView);
        mAdapter = new DeviceListAdapter(getContext(), R.layout.device_list_item, mDevices);

        // Initialize Network Service Discovery (NSD) through the helper class
        mNSD = new NsdHelper(this.getContext());
        mNSD.initializeNsd();

        mDeviceList.setAdapter(mAdapter);

        return result;
    }

    /**
     * This method begins a service discovery of network devices.
     */
    private void refreshList() {
        //mAdapter.clear();
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
        refreshList(); // initialize discovery
    }

    @Override
    public void onStop() {
        mNSD.stopDiscovery(); // suspend discovery
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    /**
     * This class implements the ArrayAdapter that displays the network devices
     * within the list view object.
     */
    public class DeviceListAdapter extends ArrayAdapter<NsdServiceInfo> {

        private ArrayList<NsdServiceInfo> devices;

        public DeviceListAdapter(Context context, int resource, List<NsdServiceInfo> objects) {
            super(context, resource, objects);
            this.devices = (ArrayList<NsdServiceInfo>) objects;
            // Generate pseudo device for testing app/emulator:
            NsdServiceInfo nsdInfo = new NsdServiceInfo();
            nsdInfo.setServiceName("SleepSafe <Test>");
            nsdInfo.setServiceType("._http._tcp");
            nsdInfo.setPort(80);
            try {
                nsdInfo.setHost(InetAddress.getByName("192.168.1.4"));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            this.devices.add(nsdInfo);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.device_list_item, null);
            }
            final NsdServiceInfo devItem = devices.get(position);
            if (devItem != null) {
                TextView serviceName = (TextView) v.findViewById(R.id.devices_service_name);
                TextView serviceIP = (TextView) v.findViewById(R.id.devices_service_ip);
                if (serviceName != null) {
                    serviceName.setText(devItem.getServiceName());
                }
                if(serviceIP != null && devItem.getHost() != null) {
                    serviceIP.setText(devItem.getHost().toString());
                }
            }
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNSD.resolveService(devItem);
                    startActivity(new Intent(getContext(), DashboardActivity.class));
                }
            });
            return v;
        }


    }

    /**
     * Network Service Discovery helper class. This class handles finding devices on
     * the local WiFi network. It is implemented as an inner class due to the
     * static nature of the Android NsdManager callbacks.
     */
    public class NsdHelper {
        Context mContext;
        NsdManager mNsdManager;
        NsdManager.ResolveListener mResolveListener;
        NsdManager.DiscoveryListener mDiscoveryListener;
        NsdManager.RegistrationListener mRegistrationListener;
        public static final String SERVICE_TYPE = "_http._tcp.";
        public static final String TAG = "SleepSafe";
        public String mServiceName = "SleepSafe";
        android.net.nsd.NsdServiceInfo mService;
        List<String> mEntries;
        DeviceFragment.DeviceListAdapter mAdapter;
        private SharedPreferences mDevicePref;

        public NsdHelper(Context context) {
            mContext = context;
            mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
            //mDevicePref = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
            mDevicePref = getActivity().getPreferences(Context.MODE_PRIVATE);
        }

        /**
         * Initializes the the NSD protocol.
         */
        public void initializeNsd() {
            initializeResolveListener();
            //mNsdManager.init(mContext.getMainLooper(), this);
        }

        /**
         * Initializes the discovery listener for NSD discovery.
         */
        public void initializeDiscoveryListener() {
            mDiscoveryListener = new NsdManager.DiscoveryListener() {
                @Override
                public void onDiscoveryStarted(String regType) {
                    Log.d(TAG, "Service discovery started: " + regType);
                }
                @Override
                public void onServiceFound(final android.net.nsd.NsdServiceInfo service) {

                    Log.d(TAG, service.toString());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.add(service);
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
                public void onServiceLost(android.net.nsd.NsdServiceInfo service) {
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

        /**
         * Resolves a service given an NsdServiceInfo object describing it's address, name, and port.
         * @param nsdInfo the service to resolve
         */
        public void resolveService(android.net.nsd.NsdServiceInfo nsdInfo) {
            if ((nsdInfo.getServiceName() != null && !nsdInfo.getServiceName().isEmpty())
            && (nsdInfo.getServiceType() != null && !nsdInfo.getServiceType().isEmpty())) {
                mNsdManager.resolveService(nsdInfo, mResolveListener);
            }
        }

        /**
         * Registers the NSD resolve listener. This retrieves the network information from the service
         * and saves it in the shared preferences as the current device.
         */
        public void initializeResolveListener() {
            mResolveListener = new NsdManager.ResolveListener() {
                @Override
                public void onResolveFailed(android.net.nsd.NsdServiceInfo serviceInfo, int errorCode) {
                    Log.e(TAG, "Resolve failed: " + errorCode);
                    mDevicePref = getContext().getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
                    mDevicePref.edit().putString(getString(R.string.pref_device_ip), serviceInfo.getHost().toString()).apply();
                    mDevicePref.edit().putInt(getString(R.string.pref_device_port), serviceInfo.getPort()).apply();
                    mDevicePref.edit().putString(getString(R.string.pref_device_name), serviceInfo.getServiceName()).apply();
                    Log.v(TAG, "Resolve failed SHARED PREF: " + mDevicePref.getAll().keySet());

                }
                @Override
                public void onServiceResolved(android.net.nsd.NsdServiceInfo serviceInfo) {
                    mDevicePref = getContext().getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
                    mDevicePref.edit().putString(getString(R.string.pref_device_ip), serviceInfo.getHost().toString()).apply();
                    mDevicePref.edit().putInt(getString(R.string.pref_device_port), serviceInfo.getPort()).apply();
                    mDevicePref.edit().putString(getString(R.string.pref_device_name), serviceInfo.getServiceName()).apply();
                    Log.v(TAG, "Resolve success SHARED PREF: " + mDevicePref.getAll().keySet());
                    Log.v(TAG, "Resolve succeeded: " + serviceInfo.toString());
                    mService = serviceInfo;
                }
            };
        }

        /**
         * Begins the active discovery of devices on the current WiFi network.
         * @param adapter The adapter of the corresponding list to populate
         */
        public void discoverServices(DeviceFragment.DeviceListAdapter adapter) {
            mAdapter = adapter;
            stopDiscovery();  // Cancel any existing discovery request
            initializeDiscoveryListener();
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }

        /**
         * Stops the active discovery of services on the network.
         */
        public void stopDiscovery() {
            if (mDiscoveryListener != null) {
                try {
                    mNsdManager.stopServiceDiscovery(mDiscoveryListener);
                } finally {
                }
                mDiscoveryListener = null;
            }
        }

        /**
         * Destroys applicable objects and registered services.
         */
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
