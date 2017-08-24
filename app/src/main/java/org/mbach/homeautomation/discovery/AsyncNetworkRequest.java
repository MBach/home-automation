package org.mbach.homeautomation.discovery;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

/**
 * AsyncNetworkRequest.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class AsyncNetworkRequest extends AsyncTask<String, Void, String> {

    private static final String TAG = "AsyncNetworkRequest";

    private OnAsyncNetworkTaskCompleted<AsyncNetworkRequest> listener;

    public boolean deviceFound;

    public AsyncNetworkRequest(OnAsyncNetworkTaskCompleted<AsyncNetworkRequest> listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String address = strings[0];
            InetAddress inetAddress = InetAddress.getByName(address);
            if (inetAddress != null && inetAddress.isReachable(50)){
                Log.d(TAG, inetAddress.getHostName());
                this.deviceFound = true;
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
}
