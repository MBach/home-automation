package org.mbach.homeautomation.discovery;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

import jcifs.netbios.NbtAddress;

/**
 * AsyncNetworkRequest.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
class AsyncNetworkRequest extends AsyncTask<String, Void, String> {

    private static final String TAG = "AsyncNetworkRequest";

    private final OnAsyncNetworkTaskCompleted<AsyncNetworkRequest> listener;

    private boolean deviceFound;
    private boolean selfFound;
    private String deviceName;

    private final String selfIp;
    private String ip;

    AsyncNetworkRequest(OnAsyncNetworkTaskCompleted<AsyncNetworkRequest> listener, String selfIp) {
        this.listener = listener;
        this.selfIp = selfIp;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String address = strings[0];
            InetAddress inetAddress = InetAddress.getByName(address);
            if (inetAddress != null && inetAddress.isReachable(100)){
                ip = inetAddress.getHostAddress();
                deviceFound = true;
                selfFound = selfIp.equals(ip);

                if (!selfFound) {
                    NbtAddress[] addressList = NbtAddress.getAllByAddress(address);
                    NbtAddress nbtAddress = addressList[0];
                    if (address != null) {
                        deviceName = nbtAddress.getHostName();
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        listener.onNetworkScanCompleted(this);
    }

    boolean isDeviceFound() {
        return deviceFound;
    }

    String getIp() {
        return ip;
    }

    boolean isSelfFound() {
        return selfFound;
    }

    String getDeviceName() {
        return deviceName;
    }
}
