package org.mbach.homeautomation.discovery;

import android.os.AsyncTask;
import android.util.Log;

import com.stealthcopter.networktools.PortScan;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

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
    //private static ArrayList<Integer> standardPorts = new ArrayList<>();
    private static final String standardPorts = "80,3000,8080,10000";

    AsyncNetworkRequest(OnAsyncNetworkTaskCompleted<AsyncNetworkRequest> listener, String selfIp) {
        this.listener = listener;
        this.selfIp = selfIp;
        /*if (standardPorts.isEmpty()) {
            standardPorts.add(80);
            standardPorts.add(3000);
            standardPorts.add(8080);
            standardPorts.add(10000);
        }*/
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String address = strings[0];
            InetAddress inetAddress = InetAddress.getByName(address);
            if (inetAddress != null && inetAddress.isReachable(100)){
                Log.d(TAG, inetAddress.getHostName());
                Log.d(TAG, inetAddress.getCanonicalHostName());
                Log.d(TAG, inetAddress.getHostAddress());
                ip = inetAddress.getHostAddress();
                deviceFound = true;
                selfFound = selfIp.equals(ip);

                if (!selfFound) {
                    PortScan.onAddress(ip).setTimeOutMillis(1000).setPorts(standardPorts).doScan(new PortScan.PortListener() {
                        @Override
                        public void onResult(int portNo, boolean open) {
                            if (open) {
                                Log.d(TAG, "port " + portNo + " is opened for " + ip);
                            }
                        }

                        @Override
                        public void onFinished(ArrayList<Integer> openPorts) {
                            Log.d(TAG, "ports opened = " + openPorts + " for " + ip);
                        }
                    });

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
        listener.onCallCompleted(this);
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
