package org.mbach.homeautomation.discovery;


import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.mbach.homeautomation.device.DeviceDAO;

import java.util.HashMap;
import java.util.Map;

/**
 * DeviceActionsResolver class tries to guess actions based on the name of the device and the opened port which was detected.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
class DeviceActionsResolver {
    private static final String TAG = "DeviceActionsResolver";

    private static final int PORT_80 = 80;
    private static final int PORT_3000 = 3000;
    private static final int PORT_8080 = 8080;
    private static final int PORT_10000 = 10000;

    private RequestQueue requestQueue = null;

    DeviceActionsResolver(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    void guessActions(DeviceDAO device, int port) {
        Log.d(TAG, "device = " + device);
        switch (port) {
            case PORT_80:
                Log.d(TAG, "No detector for port 80 yet");
                break;
            case PORT_3000:
                Log.d(TAG, "No detector for port 3000 yet");
                break;
            case PORT_8080:
                Log.d(TAG, "No detector for port 8080 yet");
                break;
            case PORT_10000:
                detectOnPort10000(device);
                break;
            default:
                Log.d(TAG, "No detector for port " + device.getPort() + " yet");
                break;
        }
    }

    private void detectOnPort10000(DeviceDAO device) {
        switch (device.getVendor()) {
            case "Edimax Technology Co. Ltd.":
                Log.d(TAG, "TODO for Edimax");
                getActions(device);
                break;
            case "Raspberry Pi Foundation":
                getActions(device);
                break;
            default:
                Log.d(TAG, "TODO for device " + device);
                break;
        }
    }

    private void getActions(final DeviceDAO device) {
        Log.d(TAG, "TODO getActions");
        String uri = Uri.parse(String.format("http://%s:%s/", device.getIP(), device.getPort()))
                .buildUpon().build().toString();
        String credentials = device.getUsername() + ":" + device.getPassword();
        byte[] t = credentials.getBytes();
        byte[] auth = Base64.encode(t, Base64.DEFAULT);
        final String basicAuthValue = new String(auth);
        requestQueue.add(new StringRequest(Request.Method.POST, uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Basic " + basicAuthValue);
                params.put("Connection", "close");
                return params;
            }
        });
    }
}
