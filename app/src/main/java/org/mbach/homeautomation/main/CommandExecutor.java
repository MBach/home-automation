package org.mbach.homeautomation.main;

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

import org.mbach.homeautomation.device.DeviceActionDAO;
import org.mbach.homeautomation.device.DeviceDAO;

import java.util.HashMap;
import java.util.Map;

/**
 * CommandExecutor class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-11
 */
class CommandExecutor {
    private static final String TAG = "CommandExecutor";
    private RequestQueue requestQueue = null;

    CommandExecutor(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    void send(final DeviceDAO device, final DeviceActionDAO deviceAction) {
        String uri = Uri.parse(String.format("http://%s:%s/%s", device.getIP(), device.getPort(), device.getEndpoint()))
                .buildUpon().build().toString();
        Log.d(TAG, "about to send to " + uri);
        requestQueue.add(new StringRequest(Request.Method.POST, uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse = " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse = " + error.toString());
            }
        }) {
            @Override
            public byte[] getBody() {
                return deviceAction.getCommand().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if (device.isProtected()) {
                    String credentials = device.getUsername() + ":" + device.getPassword();
                    byte[] t = credentials.getBytes();
                    byte[] auth = Base64.encode(t, Base64.DEFAULT);
                    final String basicAuthValue = new String(auth);
                    params.put("Authorization", "Basic " + basicAuthValue);
                    params.put("Connection", "close");
                    Log.d(TAG, "headers");
                }
                return params;
            }
        });
    }
}
