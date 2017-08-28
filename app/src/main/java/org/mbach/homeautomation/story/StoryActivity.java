package org.mbach.homeautomation.story;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.mbach.homeautomation.R;
import org.mbach.homeautomation.db.SQLiteDB;
import org.mbach.homeautomation.discovery.ScanActivity;

/**
 * StoryActivity.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class StoryActivity extends AppCompatActivity {

    private static final String TAG = "StoryActivity";

    private StoryDAO story;

    private final SQLiteDB db = new SQLiteDB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        setTitle(R.string.add_story);

        Toolbar toolbar = findViewById(R.id.toolbarStory);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.storyactivity_right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_save_story:
                EditText storyEditText = findViewById(R.id.storyEditText);
                if (story == null || story.getId() == -1) {
                    story = new StoryDAO();
                    story.setTitle(storyEditText.getText().toString());
                    /// TODO
                    //old.setDevices(story.getDevices());
                    long storyId = db.createStory(story);
                    if (storyId > 0) {
                        Toast.makeText(StoryActivity.this, R.string.toast_story_saved, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    StoryDAO old = db.getStory(story.getId());
                    old.setId(story.getId());
                    old.setTitle(storyEditText.getText().toString());
                    /// TODO
                    //old.setDevices(story.getDevices());
                    db.updateStory(story);
                }
                break;
            case R.id.action_delete_story:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_story_title)
                        .setMessage(R.string.delete_story)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.deleteStory(story);
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                /// Nothing
                            }
                        });
                builder.create().show();
                break;
            default:
                break;
        }
        return true;

    }

    public void addDevice(View view) {
        startActivity(new Intent(getApplicationContext(), ScanActivity.class));
    }
}
