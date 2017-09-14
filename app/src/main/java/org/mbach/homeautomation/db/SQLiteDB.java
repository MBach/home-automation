package org.mbach.homeautomation.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

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

    /// STORY

    public long createStory(StoryDAO story) {
        open();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.StoryEntry.STORY_TITLE, story.getTitle());
        values.put(SQLiteHelper.StoryEntry.LAST_MODIFIED, System.currentTimeMillis());
        values.put(SQLiteHelper.StoryEntry.IMAGE, story.getCoverPath());
        long id = sqLiteDatabase.insert(SQLiteHelper.StoryEntry.TABLE_STORY, null, values);
        story.setId(id);
        close();
        if (story.getDevices() != null) {
            setDevicesForStory(story, false);
        }
        return id;
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
        Log.d(TAG, "number of rows affected: " + r);
        close();
        if (r == 1 && story.getDevices() != null) {
            setDevicesForStory(story, false);
        }
        // If row was modified, 1 is returned
        return r == 1;
    }

    public boolean deleteStory(StoryDAO story) {
        open();
        String selection = SQLiteHelper.StoryEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(story.getId()) };
        setDevicesForStory(story, true);
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
            getDevicesForStory(story);
        }
        entry.close();
        close();
        return story;
    }

    /// DEVICE

    public int createDevice(DeviceDAO device) {
        open();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.DeviceEntry.IP, device.getIP());
        values.put(SQLiteHelper.DeviceEntry.SSID, device.getSSID());
        values.put(SQLiteHelper.DeviceEntry.NAME, device.getName());
        values.put(SQLiteHelper.DeviceEntry.VENDOR, device.getVendor());
        values.put(SQLiteHelper.DeviceEntry.LAST_SEEN, System.currentTimeMillis());
        int id = (int) sqLiteDatabase.insert(SQLiteHelper.DeviceEntry.TABLE_DEVICE, null, values);
        close();
        return id;
    }

    public void updateDevice(DeviceDAO device) {
        open();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.DeviceEntry.IP, device.getIP());
        values.put(SQLiteHelper.DeviceEntry.SSID, device.getSSID());
        values.put(SQLiteHelper.DeviceEntry.NAME, device.getName());
        values.put(SQLiteHelper.DeviceEntry.VENDOR, device.getVendor());
        values.put(SQLiteHelper.DeviceEntry.LAST_SEEN, System.currentTimeMillis());
        String selection = SQLiteHelper.DeviceEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(device.getId())};
        sqLiteDatabase.update(SQLiteHelper.DeviceEntry.TABLE_DEVICE, values, selection, selectionArgs);
        close();
    }

    public List<DeviceDAO> getDevicesBySSID(String SSID) {
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
                DeviceDAO device = new DeviceDAO(entries.getInt(++i));
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

    /// STORY AND DEVICES

    private void setDevicesForStory(StoryDAO story, boolean removeOnlyDontUpdate) {
        sqLiteDatabase.beginTransaction();
        List<Integer> ids = new ArrayList<>();
        for (DeviceDAO device : story.getDevices()) {
            ids.add(device.getId());
        }
        String deviceIds = TextUtils.join(",", ids);

        // Clean all for this story
        String selection = SQLiteHelper.StoryDeviceEntry.STORY_ID + " = ? AND " + SQLiteHelper.StoryDeviceEntry.DEVICE_ID + " IN (?)";
        String[] selectionArgs = { String.valueOf(story.getId()), deviceIds };
        int deleted = sqLiteDatabase.delete(SQLiteHelper.StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE, selection, selectionArgs);
        Log.d(TAG, "setDevicesForStory: " + story.getId() + ", deleted count = " + deleted);
        Log.d(TAG, "setDevicesForStory: " + story.getId() + ", ids = " + deviceIds);

        if (!removeOnlyDontUpdate) {
            // Then insert values, even if it's the same as previous
            for (Integer deviceId : ids) {
                ContentValues values = new ContentValues();
                values.put(SQLiteHelper.StoryDeviceEntry.STORY_ID, story.getId());
                values.put(SQLiteHelper.StoryDeviceEntry.DEVICE_ID, deviceId);
                long id = sqLiteDatabase.insert(SQLiteHelper.StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE, null, values);
                Log.d(TAG, "inserted in junction table: " + id + " for story " + story.getId());
            }
        }

        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
    }

    private void getDevicesForStory(StoryDAO story) {
        Cursor entries = sqLiteDatabase.query(SQLiteHelper.StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE
                        + " INNER JOIN " + SQLiteHelper.DeviceEntry.TABLE_DEVICE + " ON " + SQLiteHelper.StoryDeviceEntry.DEVICE_ID
                        + " = " + SQLiteHelper.DeviceEntry.TABLE_DEVICE + "." + SQLiteHelper.DeviceEntry._ID,
            new String[] {
                SQLiteHelper.StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE + "." + SQLiteHelper.StoryDeviceEntry.DEVICE_ID,
                SQLiteHelper.DeviceEntry.IP,
                SQLiteHelper.DeviceEntry.SSID,
                SQLiteHelper.DeviceEntry.NAME,
                SQLiteHelper.DeviceEntry.VENDOR,
                SQLiteHelper.DeviceEntry.LAST_SEEN
            },
            SQLiteHelper.StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE + "." + SQLiteHelper.StoryDeviceEntry.STORY_ID + " = ?",
            new String[] { String.valueOf(story.getId()) }, null,null, null);

        List<DeviceDAO> devices = new ArrayList<>();
        Log.d(TAG, "This story has " + entries.getCount() + " devices for story " + story.getId());
        while (entries.moveToNext()) {
            int i = -1;
            DeviceDAO device = new DeviceDAO((int) entries.getLong(++i));
            device.setIP(entries.getString(++i));
            device.setSSID(entries.getString(++i));
            device.setName(entries.getString(++i));
            device.setVendor(entries.getString(++i));
            device.setLastSeen(entries.getString(++i));
            devices.add(device);
        }
        entries.close();
        story.setDevices(devices);
    }
}
