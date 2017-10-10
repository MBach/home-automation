package org.mbach.homeautomation.discovery;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.android.device.DeviceName;

import org.mbach.homeautomation.Constants;
import org.mbach.homeautomation.R;
import org.mbach.homeautomation.db.HomeAutomationDB;
import org.mbach.homeautomation.db.OuiDB;
import org.mbach.homeautomation.device.DeviceActionDAO;
import org.mbach.homeautomation.device.DeviceActionResolver;
import org.mbach.homeautomation.device.DeviceDAO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcifs.netbios.NbtAddress;

/**
 * ScanActivity can search for devices on your local network.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class ScanActivity extends AppCompatActivity implements OnAsyncNetworkTaskCompleted<AsyncNetworkRequest> {

    private static final String TAG = "ScanActivity";
    private static int t = 0;

    private final Map<String, DeviceDAO> existingDevices = new HashMap<>();
    private final ArrayList<DeviceDAO> pendingDevices = new ArrayList<>();
    private final SparseArray<View> cards = new SparseArray<>();
    private final DeviceActionResolver deviceActionResolver = new DeviceActionResolver();

    private final HomeAutomationDB db = new HomeAutomationDB(this);
    private final OuiDB ouiDB = new OuiDB(this);
    private WifiManager wifiManager;
    private String currentIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        setTitle(R.string.category_scan_for_devices);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

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
        currentIp = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        int lastDot = currentIp.lastIndexOf(".");
        Log.d(TAG, "IP:" + currentIp);
        return currentIp.substring(0, lastDot + 1);
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

    @Override
    public void onCallCompleted(final AsyncNetworkRequest asyncNetworkRequest) {
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

            if (asyncNetworkRequest.isSelfFound()) {
                final ImageView deviceIcon = device.findViewById(R.id.deviceIcon);
                deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_phone_android_white_48dp));
                DeviceName.with(this).request(new DeviceName.Callback() {
                    @Override public void onFinished(DeviceName.DeviceInfo info, Exception error) {
                        deviceDAO.setName(info.getName());
                        name.setText(info.getName());
                        String label = String.format("%s%s (%s)", getResources().getString(R.string.ip_label), asyncNetworkRequest.getIp(), getResources().getString(R.string.device_is_self));
                        ip.setText(label);
                        vendor.setText(info.manufacturer);
                        deviceDAO.setVendor(info.manufacturer);
                        selectDevice.setEnabled(false);

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
                    }
                });
            } else {
                ip.setText(String.format("%s%s", getResources().getString(R.string.ip_label), asyncNetworkRequest.getIp()));
                String vendorName = getVendor(asyncNetworkRequest.getIp());
                // If a device has a vendor name, try to guess its actions
                if (vendorName != null) {
                    vendor.setText(vendorName);
                    deviceDAO.setVendor(vendorName);
                    List<DeviceActionDAO> actions = deviceActionResolver.guessByVendor(asyncNetworkRequest.getIp(), vendorName);
                    if (!actions.isEmpty()) {
                        deviceDAO.setActions(actions);
                    }
                }

                try {
                    StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(threadPolicy );
                    NbtAddress[] addressList = NbtAddress.getAllByAddress(asyncNetworkRequest.getIp());
                    NbtAddress address = addressList[0];
                    if (address != null) {
                        Log.d(TAG, "HOSTNAME = " + address.getHostName());
                        deviceDAO.setName(address.getHostName());
                    }

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
                } catch (UnknownHostException e) {
                    Log.e(TAG, e.getMessage());
                }


            }

            // Save the device to local database
            /*if (deviceWasSavedBefore) {
                db.updateDevice(deviceDAO);
                device.setId(deviceDAO.getId());
            } else {
                long id =  db.createDevice(deviceDAO);
                deviceDAO.setId((int) id);
                device.setId((int) id);
                detectedDevicesLayout.addView(device);
                existingDevices.put(asyncNetworkRequest.getIp(), deviceDAO);
            }*/
        }
        if (t == 254) {
            ProgressBar scanProgressBar = findViewById(R.id.scanProgressBar);
            scanProgressBar.setVisibility(View.GONE);
            t = 0;
            Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.scan_completed, Snackbar.LENGTH_LONG).show();
        }
    }

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

    public void selectDevice(View view) {
        Button button = (Button) view;
        CardView cardView = (CardView) view.getParent().getParent().getParent();

        boolean isActivated = button.isActivated();
        if (isActivated) {
            button.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
            button.setText(R.string.select_device_unselected);
            for (DeviceDAO device : existingDevices.values()) {
                if (device.getId() == cardView.getId()) {
                    pendingDevices.remove(device);
                    updateFab();
                    break;
                }
            }
        } else {
            button.setTextColor(getResources().getColor(R.color.accent));
            button.setText(R.string.select_device_selected);
            for (DeviceDAO device : existingDevices.values()) {
                if (device.getId() == cardView.getId()) {
                    pendingDevices.add(device);
                    updateFab();
                    break;
                }
            }
        }
        button.setActivated(!isActivated);
    }

    private void updateFab() {
        FloatingActionButton fab = findViewById(R.id.addDeviceFab);
        if (pendingDevices.isEmpty()) {
            fab.hide();
        } else {
            fab.show();
        }
    }
}
