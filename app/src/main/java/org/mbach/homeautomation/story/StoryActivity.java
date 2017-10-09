package org.mbach.homeautomation.story;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.mbach.homeautomation.Constants;
import org.mbach.homeautomation.ImageUtils;
import org.mbach.homeautomation.R;
import org.mbach.homeautomation.db.HomeAutomationDB;
import org.mbach.homeautomation.device.DeviceActivity;
import org.mbach.homeautomation.device.DeviceDAO;
import org.mbach.homeautomation.discovery.ScanActivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * StoryActivity.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class StoryActivity extends AppCompatActivity {

    private static final String TAG = "StoryActivity";
    private final HomeAutomationDB db = new HomeAutomationDB(this);
    private StoryDAO story;
    private boolean hasNewCover;
    private DeviceDAO lastRemovedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        Toolbar toolbar = findViewById(R.id.toolbarStory);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() == null) {
            setTitle(R.string.add_story);
        } else {
            long id = getIntent().getLongExtra(Constants.EXTRA_STORY_ID, -1);
            if (id == -1) {
                setTitle(R.string.add_story);
            } else {
                story = db.getStory(id);
                EditText storyEditText = findViewById(R.id.storyEditText);
                storyEditText.setText(story.getTitle());
                ImageView coverStory = findViewById(R.id.coverStory);
                Bitmap bitmap = ImageUtils.loadImage(getBaseContext(), story);
                if (bitmap != null) {
                    coverStory.setImageBitmap(bitmap);
                    Button addPicture = findViewById(R.id.add_picture);
                    addPicture.setText(R.string.edit_picture);
                }
                if (!story.getDevices().isEmpty()) {
                    populateDevices();
                }
                setTitle(R.string.edit_story);
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
                if (hasNewCover) {
                    String cover = saveCover();
                    if (cover != null) {
                        story.setCoverPath(cover);
                    }
                }

                long storyId = -1;
                Log.d(TAG, "storyId ? " + story.getId());
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
        if (requestCode == Constants.RQ_STORY_TO_IMAGE && resultCode == RESULT_OK && data != null) {
            ImageView coverStory = findViewById(R.id.coverStory);
            Picasso.with(getBaseContext()).load(data.getStringExtra(Constants.EXTRA_FILE)).into(coverStory);
            hasNewCover = true;
        } else if (requestCode == Constants.RQ_STORY_TO_SCAN_LAN && resultCode == RESULT_OK && data != null) {
            ArrayList<DeviceDAO> devices = data.getParcelableArrayListExtra(Constants.EXTRA_DEVICES);
            if (story == null) {
                story = new StoryDAO();
            }
            story.setDevices(devices);
            populateDevices();
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        Log.d(TAG, "onActivityReenter");
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

    /**
     *
     */
    private void populateDevices() {
        final LinearLayout mainLinearLayout = findViewById(R.id.mainLinearLayout);
        final CardView addPictureCard = findViewById(R.id.addPictureCard);

        for (final DeviceDAO deviceDAO : story.getDevices()) {
            final View previousDeviceView = mainLinearLayout.findViewById(deviceDAO.getId());
            if (previousDeviceView != null) {
                mainLinearLayout.removeView(previousDeviceView);
            }
            final View deviceView = getLayoutInflater().inflate(R.layout.story_activity_card_device, mainLinearLayout, false);
            deviceView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), DeviceActivity.class);
                    intent.putExtra(Constants.EXTRA_DEVICE_ID, deviceDAO.getId());
                    intent.putExtra(Constants.EXTRA_DEVICE_NAME, deviceDAO.getIP());
                    startActivityForResult(intent, Constants.RQ_STORY_TO_DEVICE);
                }
            });

            deviceView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(StoryActivity.this)
                            .setTitle(R.string.remove_device_from_story_title)
                            .setMessage(R.string.remove_device_from_story_description)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    removeDeviceFromStory(deviceDAO);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    builder.create().show();
                    return true;
                }
            });
            TextView ip = deviceView.findViewById(R.id.ip);
            ip.setText(deviceDAO.getIP());
            TextView vendor = deviceView.findViewById(R.id.vendor);
            vendor.setText(deviceDAO.getVendor());
            deviceView.setId(deviceDAO.getId());
            mainLinearLayout.addView(deviceView, mainLinearLayout.indexOfChild(addPictureCard));
        }
    }

    private void removeDeviceFromStory(DeviceDAO deviceToFind) {
        final List<DeviceDAO> devices = story.getDevices();
        if (devices.remove(deviceToFind)) {
            this.lastRemovedDevice = deviceToFind;
        }
        story.setDevices(devices);
        LinearLayout mainLinearLayout = findViewById(R.id.mainLinearLayout);
        View deviceView = mainLinearLayout.findViewById(deviceToFind.getId());
        mainLinearLayout.removeView(deviceView);

        // Add the possibility to restore a device in the list
        Snackbar.make(mainLinearLayout, getString(R.string.snackbar_device_removed_from_story), Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lastRemovedDevice != null) {
                        story.getDevices().add(lastRemovedDevice);
                        populateDevices();
                    }
                }
            })
            .show();
        populateDevices();
    }

    /**
     *  Navigate to {@link ScanActivity} for listing connected devices.
     *
     * @param view the view
     */
    public void addDevice(View view) {
        Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
        if (story != null) {
            intent.putExtra(Constants.EXTRA_STORY_ID, story.getId());
        }
        startActivityForResult(intent, Constants.RQ_STORY_TO_SCAN_LAN);
    }

    /**
     * Navigate to {@link ImageSearchActivity} for customizing this story.
     *
     * @param view the view
     */
    public void searchImage(View view) {
        Intent intent = new Intent(getApplicationContext(), ImageSearchActivity.class);
        if (story != null) {
            intent.putExtra(Constants.EXTRA_STORY_ID, story.getId());
        }
        startActivityForResult(intent, Constants.RQ_STORY_TO_IMAGE);
    }

    /**
     * Ask to one if he wants to remove the attached picture for the current story.
     *
     * @param view the view
     */
    public void askRemoveImage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(StoryActivity.this)
            .setTitle(R.string.remove_image_story_title)
            .setMessage(R.string.remove_image_story_description)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ImageView coverStory = findViewById(R.id.coverStory);
                    Drawable drawable = getResources().getDrawable(R.drawable.default_scenario);
                    coverStory.setImageDrawable(drawable);
                    story.setCoverPath(null);
                    hasNewCover = false;
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
        builder.create().show();
    }
}
