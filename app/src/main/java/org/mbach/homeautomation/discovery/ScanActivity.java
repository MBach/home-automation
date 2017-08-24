package org.mbach.homeautomation.discovery;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        setTitle(R.string.category_scan_for_devices);

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
    @Nullable
    private String getLocalSubnet(){
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wm == null) {
            return null;
        } else if (wm.isWifiEnabled()) {
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            int lastDot = ip.lastIndexOf(".");
            return ip.substring(0, lastDot + 1);
        } else {
            return null;
        }
    }

    /**
     *
     */
    private void startDiscovery() {
        String subnet = getLocalSubnet();
        if (subnet == null) {
            Log.d(TAG, "Wifi isn't enabled :(");
            //return;
            subnet = "192.168.1.";
        }

        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            list.add(i);
        }
        list.add(254);
        for (int i = 11; i < 254; i++) {
            list.add(i);
        }

        for (int i = 0; i < 254; i++) {
            AsyncNetworkRequest asyncNetworkRequest = new AsyncNetworkRequest(this);
            asyncNetworkRequest.execute(subnet + String.valueOf(list.get(i)));
        }
    }

    private static int t = 0;

    @Override
    public void onCallCompleted(AsyncNetworkRequest asyncNetworkRequest) {
        ++t;
        if (asyncNetworkRequest.deviceFound) {
            LinearLayout detectedDevicesLayout = findViewById(R.id.detectedDevicesLayout);
            View device = getLayoutInflater().inflate(R.layout.card_device, detectedDevicesLayout, false);
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
