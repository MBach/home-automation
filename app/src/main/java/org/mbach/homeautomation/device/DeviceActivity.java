package org.mbach.homeautomation.device;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.mbach.homeautomation.Constants;
import org.mbach.homeautomation.R;
import org.mbach.homeautomation.db.HomeAutomationDB;

import java.util.List;

/**
 * DeviceActivity class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-09
 */
public class DeviceActivity extends AppCompatActivity {

    private static final String TAG = "DeviceActivity";

    private final HomeAutomationDB db = new HomeAutomationDB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = findViewById(R.id.toolbarStory);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long storyId = getIntent().getLongExtra(Constants.EXTRA_STORY_ID, -1);
        int deviceId = getIntent().getIntExtra(Constants.EXTRA_DEVICE_ID, -1);
        String deviceName = getIntent().getStringExtra(Constants.EXTRA_DEVICE_NAME);
        if (deviceName == null) {
            setTitle(getIntent().getStringExtra(Constants.EXTRA_DEVICE_IP));
        } else {
            setTitle(deviceName);
        }

        if (storyId != -1 && deviceId != -1) {
            List<DeviceActionDAO> list = db.getActionsForStoryAndDevice(storyId, deviceId);
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
