package org.mbach.homeautomation.story;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.mbach.homeautomation.R;
import org.mbach.homeautomation.db.StoryDB;
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

        storyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                /// FIXME
                if (story == null) {
                    story = new StoryDAO();
                }
                story.setTitle(editable.toString());
                Log.d(TAG, "text changed: " + editable.toString());
            }
        });


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
                StoryDB storyDB = new StoryDB(this);
                EditText storyEditText = findViewById(R.id.storyEditText);
                if (story == null || story.getId() == -1) {
                    story = new StoryDAO();
                    story.setTitle(storyEditText.getText().toString());
                    /// TODO
                    //old.setDevices(story.getDevices());
                    storyDB.create(story);
                } else {
                    StoryDAO old = storyDB.getStory(story.getId());
                    old.setId(story.getId());
                    old.setTitle(storyEditText.getText().toString());
                    /// TODO
                    //old.setDevices(story.getDevices());
                    storyDB.update(story);
                }
                break;
            case R.id.action_delete_story:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_story_title)
                        .setMessage(R.string.delete_story)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

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
