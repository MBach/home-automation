package org.mbach.homeautomation.story;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.mbach.homeautomation.Constants;
import org.mbach.homeautomation.ImageUtils;
import org.mbach.homeautomation.R;
import org.mbach.homeautomation.db.SQLiteDB;
import org.mbach.homeautomation.discovery.ScanActivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

    private boolean hasNewCover;

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

        if (getIntent() != null) {
            long id = getIntent().getLongExtra(Constants.EXTRA_STORY_ID, -1);
            if (id != -1) {
                story = db.getStory(id);
                EditText storyEditText = findViewById(R.id.storyEditText);
                storyEditText.setText(story.getTitle());
                ImageView coverStory = findViewById(R.id.coverStory);
                Bitmap bitmap = ImageUtils.loadImage(getBaseContext(), story);
                if (bitmap != null) {
                    coverStory.setImageBitmap(bitmap);
                }
            }
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
                if (story == null) {
                    story = new StoryDAO();
                }
                story.setTitle(storyEditText.getText().toString());
                /// TODO
                //story.setDevices();

                if (hasNewCover) {
                    String cover = saveCover();
                    if (cover != null) {
                        story.setCoverPath(cover);
                    }
                }

                long storyId = -1;
                if (story.getId() == -1) {
                    storyId = db.createStory(story);
                } else if (db.updateStory(story)) {
                    storyId = story.getId();
                }
                if (storyId > 0) {
                    Toast.makeText(StoryActivity.this, R.string.toast_story_saved, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
            case R.id.action_delete_story:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_story_title)
                        .setMessage(R.string.delete_story)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (db.deleteStory(story)) {
                                    Toast.makeText(StoryActivity.this, R.string.toast_story_deleted, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult 1");
        if (requestCode == Constants.RQ_STORY_TO_IMAGE && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "onActivityResult 2 : " + data.getStringExtra("file"));
            ImageView coverStory = findViewById(R.id.coverStory);
            Picasso.with(getBaseContext()).load(data.getStringExtra("file")).into(coverStory);
            hasNewCover = true;
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        Log.d(TAG, "onActivityReenter 1");

    }

    public void addDevice(View view) {
        startActivity(new Intent(getApplicationContext(), ScanActivity.class));
    }

    public void searchImage(View view) {
        Intent intent = new Intent(getApplicationContext(), ImageSearchActivity.class);
        if (story != null) {
            intent.putExtra(Constants.EXTRA_STORY_ID, story.getId());
        }
        startActivityForResult(intent, Constants.RQ_STORY_TO_IMAGE);
    }

    @Nullable
    private String saveCover() {
        ImageView coverStory = findViewById(R.id.coverStory);
        coverStory.buildDrawingCache();
        Bitmap bitmap = coverStory.getDrawingCache();
        try {
            long timestamp = System.currentTimeMillis();
            String file = String.format("story_%s", timestamp);
            FileOutputStream fos = openFileOutput(file, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
            fos.close();
            return file;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
