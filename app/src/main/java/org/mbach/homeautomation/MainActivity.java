package org.mbach.homeautomation;

import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.mbach.homeautomation.db.SQLiteDB;
import org.mbach.homeautomation.discovery.ScanActivity;
import org.mbach.homeautomation.story.StoryActivity;
import org.mbach.homeautomation.story.StoryDAO;

/**
 * MainActivity class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private static final int FROM_STORY_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        setupDrawerLayout();
        loadStories();

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), StoryActivity.class), FROM_STORY_ACTIVITY);
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
        //if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
        //}
        switch (menuItem.getItemId()) {
            case R.id.category_add_scenario:
                startActivityForResult(new Intent(getApplicationContext(), StoryActivity.class), FROM_STORY_ACTIVITY);
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
                //startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                return true;
            case R.id.action_settings:
                //startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), FROM_SETTINGS_ACTIVITY);
                return true;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FROM_STORY_ACTIVITY && resultCode == Constants.STORY_MODIFIED) {
            recreate();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Initialize the toolbar with a custom icon.
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
     * Initialize the drawer and apply a custom color for every item.
     */
    private void setupDrawerLayout() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
        final SQLiteDB db = new SQLiteDB(this);
        for (final StoryDAO storyDAO : db.getStories()) {

            View story = getLayoutInflater().inflate(R.layout.card_story, storiesLayout, false);
            story.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), StoryActivity.class);
                    intent.putExtra(Constants.EXTRA_STORY_ID, storyDAO.getId());
                    /// TODO params
                    startActivityForResult(intent, FROM_STORY_ACTIVITY);
                    //Bundle bundle = new Bundle();
                    //startActivity(intent, bundle);
                }
            });
            TextView titleStory = story.findViewById(R.id.title);
            titleStory.setText(storyDAO.getTitle());
            Switch toggleStory = story.findViewById(R.id.enabled);
            toggleStory.setChecked(storyDAO.isEnabled());
            toggleStory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
                    storyDAO.setEnabled(enabled);
                    if (db.updateStory(storyDAO)) {
                        int text = enabled ? R.string.story_enabled : R.string.story_disabled;
                        Snackbar.make(findViewById(R.id.appBarLayout), text, Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
            storiesLayout.addView(story);
        }
    }
}
