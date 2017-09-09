package org.mbach.homeautomation.discovery;

import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.mbach.homeautomation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ScanActivity can search for devices on your local network.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class ScanActivity extends AppCompatActivity implements OnAsyncNetworkTaskCompleted<AsyncNetworkRequest> {

    private static final String TAG = "ScanActivity";

    private static int t = 0;

    private WifiManager wifiManager;

    private String currentIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        setTitle(R.string.category_scan_for_devices);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        Toolbar toolbar = findViewById(R.id.toolbarScan);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
                            //WifiMonitor wifiMonitor = new WifiMonitor();
                            //IntentFilter intentFilter = new IntentFilter();
                            //intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                            //registerReceiver(wifiMonitor, intentFilter);
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
    public void onCallCompleted(AsyncNetworkRequest asyncNetworkRequest) {
        ++t;
        if (asyncNetworkRequest.isDeviceFound()) {
            LinearLayout detectedDevicesLayout = findViewById(R.id.detectedDevicesLayout);
            View device = getLayoutInflater().inflate(R.layout.card_device, detectedDevicesLayout, false);
            TextView ip = device.findViewById(R.id.ip);
            if (asyncNetworkRequest.isSelfFound()) {
                ip.setText(String.format("%s (this is you)", asyncNetworkRequest.getIp()));
            } else {
                ip.setText(asyncNetworkRequest.getIp());
            }
            detectedDevicesLayout.addView(device);
        }
        if (t == 254) {
            ProgressBar scanProgressBar = findViewById(R.id.scanProgressBar);
            scanProgressBar.setVisibility(View.GONE);
            t = 0;
            Snackbar.make(findViewById(R.id.scanConstraintLayout), R.string.scan_completed, Snackbar.LENGTH_LONG).show();
        }
    }
}
