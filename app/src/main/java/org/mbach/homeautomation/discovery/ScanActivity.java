package org.mbach.homeautomation.discovery;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jaredrummler.android.device.DeviceName;
import com.stealthcopter.networktools.PortScan;

import org.mbach.homeautomation.Constants;
import org.mbach.homeautomation.R;
import org.mbach.homeautomation.db.HomeAutomationDB;
import org.mbach.homeautomation.db.OuiDB;

import org.mbach.homeautomation.device.DeviceDAO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ScanActivity can search for devices on your local network.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class ScanActivity extends AppCompatActivity implements OnAsyncNetworkTaskCompleted<AsyncNetworkRequest>  {

    private static final String TAG = "ScanActivity";
    private static final String standardPorts = "80,3000,8080,10000";

    private static int t = 0;
    private final Map<String, DeviceDAO> existingDevices = new HashMap<>();
    private final ArrayList<DeviceDAO> pendingDevices = new ArrayList<>();

    private final SparseArray<View> cards = new SparseArray<>();
    private final HomeAutomationDB db = new HomeAutomationDB(this);
    private final OuiDB ouiDB = new OuiDB(this);
    private WifiManager wifiManager;
    private String currentIp;
    private RequestQueue requestQueue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        setTitle(R.string.category_scan_for_devices);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        requestQueue = Volley.newRequestQueue(this);

        final Toolbar toolbar = findViewById(R.id.toolbarScan);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (wifiManager != null && wifiManager.isWifiEnabled() && wifiManager.getConnectionInfo() != null && null != wifiManager.getConnectionInfo().getSSID()) {
            List<DeviceDAO> devices = db.getDevicesBySSID(wifiManager.getConnectionInfo().getSSID());
            for (DeviceDAO deviceDAO : devices) {
                existingDevices.put(deviceDAO.getIP(), deviceDAO);
                LinearLayout detectedDevicesLayout = findViewById(R.id.detectedDevicesLayout);
                View card = getLayoutInflater().inflate(R.layout.scan_activity_card_device, detectedDevicesLayout, false);

                Button selectDevice = card.findViewById(R.id.select_device);
                selectDevice.setTag(R.id.cardDevice, deviceDAO);

                cards.append(deviceDAO.getId(), card);
                TextView name = card.findViewById(R.id.name);
                if (deviceDAO.getName() != null && !deviceDAO.getName().isEmpty()) {
                    name.setText(deviceDAO.getName());
                }
                TextView ip = card.findViewById(R.id.ip);
                ip.setText(String.format("%s%s", getResources().getString(R.string.ip_label), deviceDAO.getIP()));
                if (deviceDAO.getVendor() != null) {
                    TextView vendor = card.findViewById(R.id.vendor);
                    vendor.setText(deviceDAO.getVendor());
                }
                detectedDevicesLayout.addView(card);
            }
        }

        FloatingActionButton fab = findViewById(R.id.addDeviceFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(Constants.EXTRA_DEVICES, pendingDevices);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        startDiscovery();
    }

    @Override
    protected void onStop () {
        super.onStop();
        requestQueue.cancelAll(TAG);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
            default:
                return true;
        }
    }

    /**
     * Finds the local IP address then extract its subnet, if Wifi has been enabled.
     *
     * @return the local subnet where one is connected
     */
    @NonNull
    private String getLocalSubnet(){
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        ip = Integer.reverseBytes(ip);
        byte[] addr = BigInteger.valueOf(ip).toByteArray();
        try {
            InetAddress inetAddress = InetAddress.getByAddress(addr);
            currentIp = inetAddress.toString().substring(1);
            int lastDot = currentIp.lastIndexOf(".");
            return currentIp.substring(0, lastDot + 1);
        } catch (UnknownHostException e) {
            return "0.0.0.";
        }
    }

    /**
     *
     */
    private void startDiscovery() {
        if (wifiManager == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.wifi_unavailable_title)
                    .setMessage(R.string.wifi_unavailable)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ProgressBar scanProgressBar = findViewById(R.id.scanProgressBar);
                            scanProgressBar.setVisibility(View.GONE);
                        }
                    });
            builder.create().show();
        } else if (wifiManager.isWifiEnabled()) {
            String subnet = getLocalSubnet();
            List<Integer> list = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                list.add(i);
            }
            list.add(254);
            for (int i = 11; i < 254; i++) {
                list.add(i);
            }

            for (int i = 0; i < 254; i++) {
                AsyncNetworkRequest asyncNetworkRequest = new AsyncNetworkRequest(this, currentIp);
                asyncNetworkRequest.execute(subnet + String.valueOf(list.get(i)));
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.wifi_disabled_title)
                    .setMessage(R.string.wifi_disabled)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (wifiManager.setWifiEnabled(true)) {
                                Toast.makeText(ScanActivity.this, R.string.toast_enabling_wifi, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ScanActivity.this, R.string.toast_enabling_wifi_failed, Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ProgressBar scanProgressBar = findViewById(R.id.scanProgressBar);
                            scanProgressBar.setVisibility(View.GONE);
                        }
                    });
            builder.create().show();
        }
    }

    /**
     *
     * @param asyncNetworkRequest the original request
     */
    @Override
    public void onNetworkScanCompleted(final AsyncNetworkRequest asyncNetworkRequest) {
        ++t;
        if (asyncNetworkRequest.isDeviceFound()) {

            // Check if device is already displayed (which means it hasn't been detected yet)
            final LinearLayout detectedDevicesLayout = findViewById(R.id.detectedDevicesLayout);
            final View device;
            final DeviceDAO deviceDAO;
            final boolean deviceWasSavedBefore = existingDevices.containsKey(asyncNetworkRequest.getIp());
            if (deviceWasSavedBefore) {
                deviceDAO = existingDevices.get(asyncNetworkRequest.getIp());
                device = cards.get(deviceDAO.getId());
            } else {
                deviceDAO = new DeviceDAO();
                device = getLayoutInflater().inflate(R.layout.scan_activity_card_device, detectedDevicesLayout, false);
            }

            // Toggle default state of the Card that has been previously instantiated
            Button deviceOffline = device.findViewById(R.id.device_offline);
            deviceOffline.setVisibility(View.GONE);
            final Button selectDevice = device.findViewById(R.id.select_device);
            selectDevice.setVisibility(View.VISIBLE);
            final TextView name = device.findViewById(R.id.name);
            final TextView vendor = device.findViewById(R.id.vendor);

            final TextView ip = device.findViewById(R.id.ip);
            deviceDAO.setIP(asyncNetworkRequest.getIp());
            deviceDAO.setSSID(wifiManager.getConnectionInfo().getSSID());
            if (asyncNetworkRequest.getDeviceName() == null) {
                name.setText(getResources().getString(R.string.scan_no_name_detected));
            } else {
                deviceDAO.setName(asyncNetworkRequest.getDeviceName());
                name.setText(asyncNetworkRequest.getDeviceName());
            }

            if (asyncNetworkRequest.isSelfFound()) {
                final ImageView deviceIcon = device.findViewById(R.id.deviceIcon);
                deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_phone_android_white_48dp));
                DeviceName.with(this).request(new DeviceName.Callback() {
                    @Override public void onFinished(DeviceName.DeviceInfo info, Exception error) {
                        deviceDAO.setName(info.getName());
                        name.setText(String.format("%s (%s)", info.getName(), getResources().getString(R.string.device_is_self)));
                        String label = String.format("%s %s", getResources().getString(R.string.ip_label), asyncNetworkRequest.getIp());
                        ip.setText(label);
                        vendor.setText(info.manufacturer);
                        deviceDAO.setVendor(info.manufacturer);
                        selectDevice.setEnabled(false);
                    }
                });
            } else {
                ip.setText(String.format("%s %s", getResources().getString(R.string.ip_label), asyncNetworkRequest.getIp()));
                String vendorName = getVendor(asyncNetworkRequest.getIp());
                if (vendorName != null) {
                    vendor.setText(vendorName);
                    deviceDAO.setVendor(vendorName);
                }
            }

            // Save the device to local database
            if (deviceWasSavedBefore) {
                db.updateDevice(deviceDAO);
                device.setId(deviceDAO.getId());
            } else {
                long id =  db.createDevice(deviceDAO);
                deviceDAO.setId((int) id);
                device.setId((int) id);
                detectedDevicesLayout.addView(device);
                existingDevices.put(asyncNetworkRequest.getIp(), deviceDAO);
            }

            // If a device isn't user's phone, try to guess open ports and reachable actions
            // A limited set of TCP/UDP port are scanned, based on my own devices (no need to scan all 65535 ports)
            // Open ports are usually 80, 3000, 8080, 10000 (web, NodeJS server on Raspberry PI, some Home Automation "standard" ports like Zigbee, etc)
            if (!asyncNetworkRequest.isSelfFound()) {
                scanPort(asyncNetworkRequest.getIp());
            }
        }
        if (t == 254) {
            ProgressBar scanProgressBar = findViewById(R.id.scanProgressBar);
            scanProgressBar.setVisibility(View.GONE);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Toolbar toolbarScan = findViewById(R.id.toolbarScan);
                toolbarScan.setElevation(getResources().getDimension(R.dimen.app_bar));
            }
            t = 0;
            Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.scan_completed, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Scan a device to find open ports.
     *
     * @param ip device to scan
     */
    private void scanPort(final String ip) {
        try {
            PortScan.onAddress(ip).setTimeOutMillis(1000).setPorts(standardPorts).doScan(new PortScan.PortListener() {

                private boolean isProtected;

                @Override
                public void onResult(int port, boolean open) {
                    if (!open) {
                        return;
                    }
                    Log.d(TAG, "port " + port + " is opened for " + ip);
                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) new URL(String.format("http://%s:%s/", ip, port)).openConnection();
                        urlConnection.connect();
                        int statusCode = urlConnection.getResponseCode();
                        /// TODO guess actions
                        // DeviceActionDAO deviceActionDAO = new DeviceActionDAO();
                        if (statusCode == HttpURLConnection.HTTP_OK) {
                            Log.d(TAG, "we are connected to " + ip);
                        } else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                            // Device is protected
                            Log.d(TAG, "Device is protected " + statusCode);
                            isProtected = true;
                        } else {
                            Log.d(TAG, "Error with statusCode: " + statusCode);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IOException: " + e.getMessage());
                    }
                }

                @Override
                public void onFinished(ArrayList<Integer> openPorts) {
                    Log.d(TAG, "ports opened = " + openPorts + " for " + ip);
                    if (openPorts.size() > 0) {
                        onPortScanCompleted(ip, openPorts.get(0), isProtected);
                    } else {
                        onPortScanCompleted(ip, 0, isProtected);
                    }
                }
            });
        } catch (UnknownHostException e) {
            Log.e(TAG, "UnknownHostException: " + e.getMessage());
        }
    }

    /**
     *
     * @param ip the ip
     * @param isProtected if current scanned device is protected
     */
    public void onPortScanCompleted(String ip, int port, final boolean isProtected) {
        // It is certain that device has been saved before
        final DeviceDAO deviceDAO = existingDevices.get(ip);
        final View device = cards.get(deviceDAO.getId());

        /// XXX: wow, seriously?
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isProtected) {
                    if (device != null) {
                        ImageView lockIcon = device.findViewById(R.id.lockIcon);
                        lockIcon.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(TAG, "device is not protected, checking for protocol");
                }
            }
        });

        deviceDAO.setPort(port);
        deviceDAO.setProtected(isProtected);
        db.updateDevice(deviceDAO);
    }

    /**
     *
     * @param ip device to scan
     * @return the name of the vendor, if exists
     */
    @Nullable
    private String getVendor(String ip) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("/proc/net/arp")));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(ip)) {
                    String mac = line.substring(41, 49);
                    Log.d(TAG, "mac: " + mac);
                    return ouiDB.findVendor(mac);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param view the button which was clicked
     */
    public void selectDevice(View view) {
        Button button = (Button) view;
        DeviceDAO device = (DeviceDAO) button.getTag(R.id.cardDevice);

        boolean isActivated = button.isActivated();
        if (isActivated) {
            button.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
            button.setText(R.string.select_device_unselected);
            pendingDevices.remove(device);
            updateFab();
            button.setActivated(false);
        } else if (device.isProtected() && device.isLocked()) {
            showCredentialsDialog(button, device);
        } else {
            addDeviceToPending(button, device);
        }
    }

    /**
     *
     * @param button button which triggered the action
     * @param device associated device to check (if protected)
     */
    private void addDeviceToPending(Button button, DeviceDAO device) {
        button.setTextColor(getResources().getColor(R.color.accent));
        button.setText(R.string.select_device_selected);
        pendingDevices.add(device);
        updateFab();
        button.setActivated(true);
    }

    /**
     *
     */
    private void updateFab() {
        FloatingActionButton fab = findViewById(R.id.addDeviceFab);
        if (pendingDevices.isEmpty()) {
            fab.hide();
        } else {
            fab.show();
        }
    }

    private void showCredentialsDialog(final Button b, final DeviceDAO deviceDAO) {
        final View content = View.inflate(this, R.layout.dialog_device_protected, null);
        final AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_device_protected_title)
                .setPositiveButton(android.R.string.yes, null)
                .setNegativeButton(R.string.cancel, null)
                .setView(content).create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText username = content.findViewById(R.id.deviceUsername);
                        EditText password = content.findViewById(R.id.devicePassword);
                        if (TextUtils.isEmpty(username.getText())) {
                            username.setError(getString(R.string.dialog_error_field_required));
                            username.requestFocus();
                        } else if (TextUtils.isEmpty(password.getText())) {
                            password.setError(getString(R.string.dialog_error_field_required));
                            password.requestFocus();
                        } else {
                            dialog.dismiss();
                            checkCredentials(b, deviceDAO, username.getText().toString(), password.getText().toString());
                        }
                    }
                });
            }
        });
        d.show();
    }

    private void checkCredentials(final Button button, final DeviceDAO deviceDAO, String username, String password) {

        String uri = Uri.parse(String.format("http://%s:%s/", deviceDAO.getIP(), deviceDAO.getPort()))
                .buildUpon().build().toString();
        String credentials = username + ":" + password;
        byte[] t = credentials.getBytes();
        byte[] auth = Base64.encode(t, Base64.DEFAULT);
        final String basicAuthValue = new String(auth);
        requestQueue.add(new StringRequest(Request.Method.POST, uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse = " + response);
                Toast.makeText(ScanActivity.this, R.string.dialog_auth_success, Toast.LENGTH_SHORT).show();
                addDeviceToPending(button, deviceDAO);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ScanActivity.this, R.string.dialog_auth_failed, Toast.LENGTH_SHORT).show();
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
