package org.mbach.homeautomation.device;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.mbach.homeautomation.Constants;
import org.mbach.homeautomation.R;
import org.mbach.homeautomation.edimaxsmartplug.entities.PlugCredentials;
import org.mbach.homeautomation.edimaxsmartplug.smartplug.LocalConnection;
import org.mbach.homeautomation.edimaxsmartplug.smartplug.PlugConnection;
import org.mbach.homeautomation.edimaxsmartplug.smartplug.SmartPlug;

import java.net.MalformedURLException;

/**
 * Constants class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-09
 */
public class DeviceActivity extends AppCompatActivity {

    private static final String TAG = "DeviceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = findViewById(R.id.toolbarStory);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long id = getIntent().getLongExtra(Constants.EXTRA_STORY_ID, -1);
        String deviceName = getIntent().getStringExtra(Constants.EXTRA_DEVICE_NAME);
        setTitle(deviceName);

        /// XXX
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy );
        try {
            PlugCredentials plugCredentials = new PlugCredentials("admin", "0000");
            PlugConnection plugConnection = new LocalConnection(plugCredentials, deviceName);
            SmartPlug smartPlug = new SmartPlug(plugConnection);
            smartPlug.toggle();
        } catch (MalformedURLException e) {
            Log.d(TAG, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }
}
