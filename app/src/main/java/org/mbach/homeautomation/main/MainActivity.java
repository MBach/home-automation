package org.mbach.homeautomation.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.mbach.homeautomation.Constants;
import org.mbach.homeautomation.ImageUtils;
import org.mbach.homeautomation.R;
import org.mbach.homeautomation.db.HomeAutomationDB;
import org.mbach.homeautomation.device.DeviceActionDAO;
import org.mbach.homeautomation.device.DeviceDAO;
import org.mbach.homeautomation.device.StoryDeviceActionDAO;
import org.mbach.homeautomation.discovery.ScanActivity;
import org.mbach.homeautomation.story.StoryActivity;
import org.mbach.homeautomation.story.StoryDAO;

import java.util.List;

/**
 * MainActivity class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private final HomeAutomationDB db = new HomeAutomationDB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initOuiDb();
        initToolbar();
        setupDrawerLayout();
        loadStories();

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), StoryActivity.class), Constants.RQ_MAIN_TO_STORY);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainactivity_right_menu, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.category_add_scenario:
                startActivityForResult(new Intent(getApplicationContext(), StoryActivity.class), Constants.RQ_MAIN_TO_STORY);
                break;
            case R.id.category_scan_for_devices:
                startActivity(new Intent(getApplicationContext(), ScanActivity.class));
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_account:
                // startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                return true;
            case R.id.action_settings:
                // startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), FROM_SETTINGS_ACTIVITY);
                return true;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.RQ_MAIN_TO_STORY && resultCode == RESULT_OK) {
            recreate();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Initialize the toolbar.
     */
    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    /**
     * Initialize the drawer.
     */
    private void setupDrawerLayout() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
            }
        });

        final Toolbar toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,  drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void loadStories() {
        LinearLayout storiesLayout = findViewById(R.id.storiesLayout);
        for (final StoryDAO storyDAO : db.getStories()) {

            View storyView = getLayoutInflater().inflate(R.layout.card_story, storiesLayout, false);
            storyView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), StoryActivity.class);
                    intent.putExtra(Constants.EXTRA_STORY_ID, storyDAO.getId());
                    startActivityForResult(intent, Constants.RQ_MAIN_TO_STORY);
                }
            });
            TextView titleStory = storyView.findViewById(R.id.title);
            titleStory.setText(storyDAO.getTitle());
            final Switch s = storyView.findViewById(R.id.enabled);
            s.setChecked(storyDAO.isEnabled());
            s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
                    storyDAO.setEnabled(enabled);
                    if (db.updateStory(storyDAO)) {
                        int text = enabled ? R.string.story_enabled : R.string.story_disabled;
                        Snackbar.make(findViewById(R.id.coordinatorLayout), text, Snackbar.LENGTH_SHORT).show();
                    }
                    toggleStory(enabled, storyDAO);
                }
            });
            Bitmap bitmap = ImageUtils.loadImage(getBaseContext(), storyDAO);
            if (bitmap == null) {
                Drawable drawable = getResources().getDrawable(R.drawable.default_scenario);
                storyView.setBackground(drawable);
            } else {
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                storyView.setBackground(bitmapDrawable);
            }
            storiesLayout.addView(storyView);
        }
    }

    /**
     *
     */
    private void initOuiDb() {
        AsyncPopulateDb asyncNetworkRequest = new AsyncPopulateDb(this);
        asyncNetworkRequest.execute();
    }

    private void toggleStory(boolean enabled, StoryDAO story) {
        List<DeviceDAO> devices = story.getDevices();
        for (DeviceDAO device : devices) {
            List<StoryDeviceActionDAO> actions = db.getActionsForStoryAndDevice(story.getId(), device.getId());
            for (StoryDeviceActionDAO action : actions) {
                if (enabled && action.isEnabled() || !enabled && !action.isEnabled()) {
                    /// XXX should be optimized with JOIN request instead of fetching the whole list again
                    List<DeviceActionDAO> deviceActions = db.getActionsForDevice(action.getDeviceId());
                    DeviceActionDAO deviceAction = null;
                    for (DeviceActionDAO d : deviceActions) {
                        if (d.getId() == action.getActionId()) {
                            deviceAction = d;
                            break;
                        }
                    }
                    if (deviceAction != null) {
                        Log.d(TAG, "d = " + deviceAction.getName() + ", " + deviceAction.getCommand());
                        CommandExecutor commandExecutor = new CommandExecutor(this);
                        commandExecutor.send(device, deviceAction);
                    }
                }
            }
        }
    }
}
