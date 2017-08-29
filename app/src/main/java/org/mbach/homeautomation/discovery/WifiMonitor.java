package org.mbach.homeautomation.discovery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * WifiMonitor.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class WifiMonitor extends BroadcastReceiver {

    private static final String TAG = "WifiMonitor";

    @Override
    public void onReceive(Context context, Intent intent) {
        /// FIXME Android N
        Log.d(TAG, "onReceive: " + intent.getAction());

        if (!WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            return;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    Log.d(TAG, "Have Wifi Connection");
                } else {
                    Log.d(TAG, "Don't have Wifi Connection");
                }
            } else {
                Log.d(TAG, "netInfo is null :(");
            }
        }
    }
}
