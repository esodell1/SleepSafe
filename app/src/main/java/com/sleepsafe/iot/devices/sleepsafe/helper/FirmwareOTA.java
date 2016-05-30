package com.sleepsafe.iot.devices.sleepsafe.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.sleepsafe.iot.devices.sleepsafe.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;


public class FirmwareOTA {

    private static final String TAG = "FirmwareOTA";
    private static final String DEV_INFO = "AboutDevice";
    private static final String DEV_UPDATE = "UpdateDeviceFirmware";
    private static final String FIRMWARE_FILE_NAME = "sleepsafe.001.bin";
    private static final int FIRMWARE_VERSION = 2;
    private static final String FIRMWARE_VERSION_STRING = "0.0.2";
    private ProgressDialog mProgress;


    // URL of emulator server (to be replaced by device):
    private String BASE_URL = null;

    private Context mContext;
    private FW_Uploader mFW;
    private DeviceInfo mDeviceInfo;
    private DeviceRequest mDeviceRequest;

    public FirmwareOTA(Context context) {
        mContext = context;
        mProgress = new ProgressDialog(mContext);
    }

    public void executeUpdate() {
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getString(R.string.pref_name), Context.MODE_PRIVATE);
        BASE_URL = "http:/" + prefs.getString(mContext.getString(R.string.pref_device_ip), "192.168.24.23")
                + ":" + prefs.getInt(mContext.getString(R.string.pref_device_port), 80) + "/";
        mFW = new FW_Uploader();
        mDeviceRequest = new DeviceRequest();
        mDeviceRequest.execute();
        Log.v("Updater", "Update");
    }

    public void checkVersion(DeviceInfo deviceInfo) {

    }

    public class FW_Uploader extends AsyncTask<Void, Void, Void> {


        // can use UI thread here
        protected void onPreExecute() {
            mProgress.setMessage("Uploading Firmware...");
            mProgress.setCancelable(false);
            mProgress.show();
        }

        @Override
        protected Void doInBackground(Void... nsd) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024*1024;
            String urlString = BASE_URL + DEV_UPDATE;
            try{

                //------------------ CLIENT REQUEST
                InputStream fileStream = mContext.getAssets().open(FIRMWARE_FILE_NAME);
                // open a URL connection to the Servlet
                URL url = new URL(urlString);
                // Open a HTTP connection to the URL
                conn = (HttpURLConnection) url.openConnection();
                // Allow Inputs
                conn.setDoInput(true);
                // Allow Outputs
                conn.setDoOutput(true);
                // Don't use a cached copy.
                conn.setUseCaches(false);
                // Use a post method.
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
                dos = new DataOutputStream( conn.getOutputStream() );
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"update\";filename=\"" + FIRMWARE_FILE_NAME + "\"" + lineEnd); // uploaded_file_name is the Name of the File to be uploaded
                dos.writeBytes(lineEnd);
                bytesAvailable = fileStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fileStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0){
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileStream.read(buffer, 0, bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                fileStream.close();
                dos.flush();
                dos.close();
            }
            catch (IOException ioe){
                Log.e("Debug", "error: " + ioe.getMessage(), ioe);
            }
            //------------------ read the SERVER RESPONSE
            try {
                inStream = new DataInputStream ( conn.getInputStream() );
                String str;
                while (( str = inStream.readLine()) != null){
                    Log.e("Debug","Server Response "+str);
                }
                inStream.close();
            }
            catch (IOException ioex){
                Log.e("Debug", "error: " + ioex.getMessage(), ioex);
            }
            // Delay to see progress dialog:
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
        }
    }


    /**
     * This class provides an asynchronous HTTP request helper so the service may query the device.
     */
    private class DeviceRequest extends AsyncTask<Void, Void, DeviceInfo> {

        @Override
        protected DeviceInfo doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonStr = null;

            try {
                String urlString = BASE_URL + DEV_INFO;


                URL url = new URL(urlString);
                // Create the request to the device, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (Exception e) {
                Log.e(TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            DeviceInfo result;
            try {
                result = getDataFromJSON(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            return result;
        }

        private DeviceInfo getDataFromJSON(String jsonString) throws JSONException {
            final String NAME = "Device_Name";
            final String FIRMWARE = "Firmware_Version";
            final String SERIAL_NUMBER = "SerialNumber";//FIRMWARE_INT = "firmware_int";
            //final String BATTERY = "battery";

            JSONObject result = new JSONObject(jsonString);
            String name = result.getJSONObject(NAME).getString("value");
            int fw = result.getJSONObject(FIRMWARE).getInt("value");
            int serial_int = result.getJSONObject(SERIAL_NUMBER).getInt("value");
            //int battery = result.getInt(BATTERY);
            return new DeviceInfo (name, fw, serial_int);//, battery);
        }

        @Override
        protected void onPostExecute(DeviceInfo result) {
            if (result != null) {

                if (result.mFirmware < FIRMWARE_VERSION) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Firmware update available!\nCurrent Version: "
                            + result.mFirmware + "\nNewest Version: "
                            + FIRMWARE_VERSION_STRING);
                    builder.setTitle("Update");
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mFW.execute();
                        }
                    });
                    builder.show();

                }

            }
        }
    }

    public class DeviceInfo {
        public String mName = null;
        public int mFirmware = 0;
        public int mSerialNumber = 0;
        //public int mBattery = 0;

        public DeviceInfo() {
            // default constructor
        }

        public DeviceInfo(String name, int firmware, int serNum) {//, int battery) {
            mName = name;
            mFirmware = firmware;
            mSerialNumber = serNum;
            //mBattery = battery;
        }
    }

}