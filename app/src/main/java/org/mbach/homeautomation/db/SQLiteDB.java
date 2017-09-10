package org.mbach.homeautomation.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.mbach.homeautomation.discovery.DeviceDAO;
import org.mbach.homeautomation.story.StoryDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteDB.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class SQLiteDB {
    private SQLiteDatabase sqLiteDatabase;
    private static final int DB_VERSION = 1;
    private final SQLiteHelper helper;

    private static final String DB_NAME = "home-automation.db";

    private static final String TAG = "SQLiteDB";

    public SQLiteDB(Context context){
        helper = new SQLiteHelper(context, DB_NAME, null, DB_VERSION);
    }

    private void open(){
        sqLiteDatabase = helper.getWritableDatabase();
    }

    private void close(){
        sqLiteDatabase.close();
    }

    public long createStory(StoryDAO story) {
        open();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.StoryEntry.STORY_TITLE, story.getTitle());
        values.put(SQLiteHelper.StoryEntry.LAST_MODIFIED, System.currentTimeMillis());
        values.put(SQLiteHelper.StoryEntry.IMAGE, story.getCoverPath());
        long id = sqLiteDatabase.insert(SQLiteHelper.StoryEntry.TABLE_STORY, null, values);
        close();
        return id;
    }

    public long createOrUpdateDevice(DeviceDAO device) {
        open();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.DeviceEntry.IP, device.getIP());
        values.put(SQLiteHelper.DeviceEntry.SSID, device.getSSID());
        values.put(SQLiteHelper.DeviceEntry.NAME, device.getName());
        values.put(SQLiteHelper.DeviceEntry.VENDOR, device.getVendor());
        values.put(SQLiteHelper.DeviceEntry.LAST_SEEN, System.currentTimeMillis());

        // Devices are uniquely identified by their IP and SSID
        Cursor savedDevice = sqLiteDatabase.query(SQLiteHelper.DeviceEntry.TABLE_DEVICE,
                new String[] { SQLiteHelper.DeviceEntry._ID },
                SQLiteHelper.DeviceEntry.IP + " = ? AND " + SQLiteHelper.DeviceEntry.SSID + " = ?",
                new String[] { device.getIP(), device.getSSID() }, null,null, null);

        // First, check if device was registered before
        long id = -1;
        if (savedDevice.getCount() > 0 && savedDevice.moveToFirst()) {
            id = savedDevice.getInt(0);
        }
        savedDevice.close();

        if (id > 0) {
            String selection = SQLiteHelper.DeviceEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(id) };
            id = sqLiteDatabase.update(SQLiteHelper.DeviceEntry.TABLE_DEVICE, values, selection, selectionArgs);
        } else {
            id = sqLiteDatabase.insert(SQLiteHelper.DeviceEntry.TABLE_DEVICE, null, values);
        }
        close();
        return id;
    }

    public List<DeviceDAO> getDevices(String SSID) {
        open();
        Cursor entries = sqLiteDatabase.query(SQLiteHelper.DeviceEntry.TABLE_DEVICE,
                new String[] {
                    SQLiteHelper.DeviceEntry._ID,
                    SQLiteHelper.DeviceEntry.IP,
                    SQLiteHelper.DeviceEntry.SSID,
                    SQLiteHelper.DeviceEntry.NAME,
                    SQLiteHelper.DeviceEntry.VENDOR,
                    SQLiteHelper.DeviceEntry.LAST_SEEN
                },
                SQLiteHelper.DeviceEntry.SSID + " = ?",
                new String[] { SSID }, null,null,
                SQLiteHelper.DeviceEntry.IP + " ASC");
        List<DeviceDAO> devices = new ArrayList<>();
        if (entries.getCount() != 0) {
            while (entries.moveToNext()) {
                int i = -1;
                DeviceDAO device = new DeviceDAO(entries.getLong(++i));
                device.setIP(entries.getString(++i));
                device.setSSID(entries.getString(++i));
                device.setName(entries.getString(++i));
                device.setVendor(entries.getString(++i));
                device.setLastSeen(entries.getString(++i));
                devices.add(device);
            }
        }
        entries.close();
        close();
        return devices;
    }

    public boolean updateStory(StoryDAO story) {
        open();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.StoryEntry.STORY_TITLE, story.getTitle());
        values.put(SQLiteHelper.StoryEntry.LAST_MODIFIED, System.currentTimeMillis());
        values.put(SQLiteHelper.StoryEntry.ENABLED, story.isEnabled());
        values.put(SQLiteHelper.StoryEntry.IMAGE, story.getCoverPath());

        String selection = SQLiteHelper.StoryEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(story.getId()) };
        int r = sqLiteDatabase.update(SQLiteHelper.StoryEntry.TABLE_STORY, values, selection, selectionArgs);
        close();
        // If row was modified, 1 is returned
        return r == 1;
    }

    public boolean deleteStory(StoryDAO story) {
        open();
        String selection = SQLiteHelper.StoryEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(story.getId()) };
        int count = sqLiteDatabase.delete(SQLiteHelper.StoryEntry.TABLE_STORY, selection, selectionArgs);
        close();
        return count > 0;
    }

    public List<StoryDAO> getStories() {
        open();
        Cursor entries = sqLiteDatabase.query(SQLiteHelper.StoryEntry.TABLE_STORY,
                new String[] { SQLiteHelper.StoryEntry._ID, SQLiteHelper.StoryEntry.STORY_TITLE, SQLiteHelper.StoryEntry.ENABLED, SQLiteHelper.StoryEntry.IMAGE },
                SQLiteHelper.StoryEntry._ID,
                null, null,null,
                SQLiteHelper.StoryEntry.LAST_MODIFIED + " DESC");
        List<StoryDAO> stories = new ArrayList<>();
        if (entries.getCount() != 0) {
            while (entries.moveToNext()) {
                StoryDAO story = new StoryDAO(entries.getLong(0));
                story.setTitle(entries.getString(1));
                story.setEnabled(entries.getInt(2) == 1);
                story.setCoverPath(entries.getString(3));
                stories.add(story);
            }
        }
        entries.close();
        close();
        return stories;
    }

    public StoryDAO getStory(long idToFind) {
        open();
        Cursor entry = sqLiteDatabase.query(SQLiteHelper.StoryEntry.TABLE_STORY,
                new String[] { SQLiteHelper.StoryEntry._ID, SQLiteHelper.StoryEntry.STORY_TITLE, SQLiteHelper.StoryEntry.ENABLED, SQLiteHelper.StoryEntry.IMAGE },
                SQLiteHelper.StoryEntry._ID + " = ?",
                new String[] { String.valueOf(idToFind) }, null,null, null);
        StoryDAO story = null;
        entry.moveToFirst();
        long id = entry.getLong(0);
        if (id == idToFind) {
            story = new StoryDAO(id);
            story.setTitle(entry.getString(1));
            story.setEnabled(entry.getInt(2) == 1);
            story.setCoverPath(entry.getString(3));
        }
        entry.close();
        close();
        return story;
    }
}
