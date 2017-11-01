package org.mbach.homeautomation.device;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

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

    private long storyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = findViewById(R.id.toolbarStory);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        storyId = getIntent().getLongExtra(Constants.EXTRA_STORY_ID, -1);
        int deviceId = getIntent().getIntExtra(Constants.EXTRA_DEVICE_ID, -1);
        String deviceName = getIntent().getStringExtra(Constants.EXTRA_DEVICE_NAME);
        if (deviceName == null) {
            setTitle(getIntent().getStringExtra(Constants.EXTRA_DEVICE_IP));
        } else {
            setTitle(deviceName);
        }

        if (storyId != -1 && deviceId != -1) {
            // If the device has previously been configured, then display
            List<StoryDeviceActionDAO> existingActions = db.getActionsForStoryAndDevice(storyId, deviceId);

            // Get the default list of actions for the selected device
            List<DeviceActionDAO> defaultActions = db.getActionsForDevice(deviceId);
            populate(true, defaultActions, existingActions);
            populate(false, defaultActions, existingActions);
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

    private void populate(final boolean enabled, List<DeviceActionDAO> defaultActions, List<StoryDeviceActionDAO> existingActions) {
        LinearLayout layout;
        if (enabled) {
            layout = findViewById(R.id.storyEnabledLinearLayout);
        } else {
            layout = findViewById(R.id.storyDisabledLinearLayout);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        for (final DeviceActionDAO action : defaultActions) {
            Switch s = new Switch(this);
            s.setText(action.getName());
            s.setLayoutParams(lp);
            // Check the matching switch for every list
            for (StoryDeviceActionDAO existingAction : existingActions) {
                if (enabled && existingAction.isEnabled() && existingAction.getActionId() == action.getId()) {
                    s.setChecked(true);
                    break;
                } else if (!enabled && !existingAction.isEnabled() && existingAction.getActionId() == action.getId()) {
                    s.setChecked(true);
                    break;
                }
            }
            layout.addView(s);
            s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if (checked) {
                        db.createActionForStoryAndDevice(storyId, enabled, action);
                    } else {
                        db.deleteActionForStoryAndDevice(storyId, enabled, action);
                    }
                }
            });
        }
    }
}
