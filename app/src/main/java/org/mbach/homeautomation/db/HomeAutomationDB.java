package org.mbach.homeautomation.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import org.mbach.homeautomation.device.DeviceDAO;
import org.mbach.homeautomation.story.StoryDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeAutomationDB.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class HomeAutomationDB {
    private SQLiteDatabase sqLiteDatabase;
    private static final int DB_VERSION = 1;
    private final SQLiteHelper helper;

    private static final String DB_NAME = "home-automation.db";

    private static final String TAG = "HomeAutomationDB";

    /**
     * StoryEntry is an inner class which maps to a Story.
     *
     * @link org.mbach.homeautomation.story.StoryDAO
     */
    static class StoryEntry implements BaseColumns {
        static final String TABLE_STORY = "table_story";
        static final String STORY_TITLE = "STORY_TITLE";
        static final String LAST_MODIFIED = "LAST_MODIFIED";
        static final String ENABLED = "ENABLED";
        static final String IMAGE = "IMAGE";
        static final String CREATE_TABLE_STORY = "CREATE TABLE " + TABLE_STORY + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + STORY_TITLE + " TEXT NOT NULL, "
                + LAST_MODIFIED + " INTEGER NOT NULL, "
                + ENABLED + " INTEGER DEFAULT 1, "
                + IMAGE + " TEXT);";
    }

    /**
     *
     */
    static class DeviceEntry implements BaseColumns {
        static final String TABLE_DEVICE = "table_device";
        static final String IP = "IP";
        static final String SSID = "SSID";
        static final String NAME = "NAME";
        static final String VENDOR = "VENDOR";
        static final String LAST_SEEN = "LAST_SEEN";
        static final String CREATE_TABLE_DEVICE = "CREATE TABLE " + TABLE_DEVICE + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + IP + " TEXT NOT NULL, "
                + SSID + " TEXT NOT NULL, "
                + NAME + " TEXT, "
                + VENDOR + " TEXT, "
                + LAST_SEEN + " INTEGER NOT NULL);";
    }

    /**
     * Every device has at least one action, like "power on", "power off", "toggle", etc. Unlike StoryDeviceEntry,
     * this table is a simple 1 to N relationship.
     */
    static class DeviceActionEntry implements BaseColumns {
        static final String TABLE_DEVICE_ACTION = "table_device_action";
        static final String DEVICE_ID = "DEVICE_ID";
        static final String NAME = "NAME";
        static final String CREATE_TABLE_DEVICE_ACTION = "CREATE TABLE " + TABLE_DEVICE_ACTION + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + DEVICE_ID + " INTEGER NOT NULL, "
                + NAME + " TEXT NOT NULL);";
    }

    /**
     *
     */
    static class StoryDeviceEntry implements BaseColumns {
        static final String TABLE_JUNCTION_STORY_DEVICE = "table_story_device";
        static final String STORY_ID = "STORY_ID";
        static final String DEVICE_ID = "DEVICE_ID";
        static final String CREATE_TABLE_STORY_DEVICE = "CREATE TABLE " + TABLE_JUNCTION_STORY_DEVICE + " ("
                + STORY_ID + " INTEGER NOT NULL, "
                + DEVICE_ID + " INTEGER NOT NULL);";
    }

    public HomeAutomationDB(Context context){
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
        sqLiteDatabase.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(StoryEntry.STORY_TITLE, story.getTitle());
        values.put(StoryEntry.LAST_MODIFIED, System.currentTimeMillis());
        values.put(StoryEntry.IMAGE, story.getCoverPath());
        long id = sqLiteDatabase.insert(StoryEntry.TABLE_STORY, null, values);
        story.setId(id);
        if (story.getDevices() != null) {
            setDevicesForStory(story, false);
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
        close();
        return id;
    }

    public boolean updateStory(StoryDAO story) {
        open();
        sqLiteDatabase.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(StoryEntry.STORY_TITLE, story.getTitle());
        values.put(StoryEntry.LAST_MODIFIED, System.currentTimeMillis());
        values.put(StoryEntry.ENABLED, story.isEnabled());
        values.put(StoryEntry.IMAGE, story.getCoverPath());

        String selection = StoryEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(story.getId()) };
        int r = sqLiteDatabase.update(StoryEntry.TABLE_STORY, values, selection, selectionArgs);
        Log.d(TAG, "number of rows affected: " + r);
        if (r == 1) {
            if (story.getDevices() != null) {
                setDevicesForStory(story, false);
            }
            sqLiteDatabase.setTransactionSuccessful();
        }
        sqLiteDatabase.endTransaction();
        close();
        // If row was modified, 1 is returned
        return r == 1;
    }

    public boolean deleteStory(StoryDAO story) {
        open();
        sqLiteDatabase.beginTransaction();
        String selection = StoryEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(story.getId()) };
        setDevicesForStory(story, true);
        int count = sqLiteDatabase.delete(StoryEntry.TABLE_STORY, selection, selectionArgs);
        if (count > 0) {
            sqLiteDatabase.setTransactionSuccessful();
        }
        sqLiteDatabase.endTransaction();
        close();
        return count > 0;
    }

    public List<StoryDAO> getStories() {
        open();
        Cursor entries = sqLiteDatabase.query(StoryEntry.TABLE_STORY,
                new String[] { StoryEntry._ID, StoryEntry.STORY_TITLE, StoryEntry.ENABLED, StoryEntry.IMAGE },
                StoryEntry._ID,
                null, null,null,
                StoryEntry.LAST_MODIFIED + " DESC");
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
        sqLiteDatabase.beginTransaction();

        Cursor entry = sqLiteDatabase.query(StoryEntry.TABLE_STORY,
                new String[] { StoryEntry._ID, StoryEntry.STORY_TITLE, StoryEntry.ENABLED, StoryEntry.IMAGE },
                StoryEntry._ID + " = ?",
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
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
        close();
        return story;
    }

    /// DEVICE

    public int createDevice(DeviceDAO device) {
        open();
        ContentValues values = new ContentValues();
        values.put(DeviceEntry.IP, device.getIP());
        values.put(DeviceEntry.SSID, device.getSSID());
        values.put(DeviceEntry.NAME, device.getName());
        values.put(DeviceEntry.VENDOR, device.getVendor());
        values.put(DeviceEntry.LAST_SEEN, System.currentTimeMillis());
        int id = (int) sqLiteDatabase.insert(DeviceEntry.TABLE_DEVICE, null, values);
        close();
        return id;
    }

    public void updateDevice(DeviceDAO device) {
        open();
        ContentValues values = new ContentValues();
        values.put(DeviceEntry.IP, device.getIP());
        values.put(DeviceEntry.SSID, device.getSSID());
        values.put(DeviceEntry.NAME, device.getName());
        values.put(DeviceEntry.VENDOR, device.getVendor());
        values.put(DeviceEntry.LAST_SEEN, System.currentTimeMillis());
        String selection = DeviceEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(device.getId())};
        sqLiteDatabase.update(DeviceEntry.TABLE_DEVICE, values, selection, selectionArgs);
        close();
    }

    public List<DeviceDAO> getDevicesBySSID(String SSID) {
        open();
        Cursor entries = sqLiteDatabase.query(DeviceEntry.TABLE_DEVICE,
                new String[] {
                        DeviceEntry._ID,
                        DeviceEntry.IP,
                        DeviceEntry.SSID,
                        DeviceEntry.NAME,
                        DeviceEntry.VENDOR,
                        DeviceEntry.LAST_SEEN
                },
                DeviceEntry.SSID + " = ?",
                new String[] { SSID }, null,null,
                DeviceEntry.IP + " ASC");
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
        List<Integer> ids = new ArrayList<>();
        for (DeviceDAO device : story.getDevices()) {
            ids.add(device.getId());
        }

        // Clean all for this story
        String selection = StoryDeviceEntry.STORY_ID + " = ?";
        String[] selectionArgs = { String.valueOf(story.getId()) };
        sqLiteDatabase.delete(StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE, selection, selectionArgs);
        if (!removeOnlyDontUpdate) {
            // Then insert values, even if it's the same as previous
            for (Integer deviceId : ids) {
                ContentValues values = new ContentValues();
                values.put(StoryDeviceEntry.STORY_ID, story.getId());
                values.put(StoryDeviceEntry.DEVICE_ID, deviceId);
                long id = sqLiteDatabase.insert(StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE, null, values);
                Log.d(TAG, "inserted in junction table: " + id + " for story " + story.getId());
            }
        }
    }

    private void getDevicesForStory(StoryDAO story) {
        Cursor entries = sqLiteDatabase.query(StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE
                        + " INNER JOIN " + DeviceEntry.TABLE_DEVICE + " ON " + StoryDeviceEntry.DEVICE_ID
                        + " = " + DeviceEntry.TABLE_DEVICE + "." + DeviceEntry._ID,
            new String[] {
                StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE + "." + StoryDeviceEntry.DEVICE_ID,
                DeviceEntry.IP,
                DeviceEntry.SSID,
                DeviceEntry.NAME,
                DeviceEntry.VENDOR,
                DeviceEntry.LAST_SEEN
            },
            StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE + "." + StoryDeviceEntry.STORY_ID + " = ?",
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
