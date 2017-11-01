package org.mbach.homeautomation.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import org.mbach.homeautomation.device.DeviceActionDAO;
import org.mbach.homeautomation.device.DeviceDAO;
import org.mbach.homeautomation.device.StoryDeviceActionDAO;
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
                + ENABLED + " INTEGER DEFAULT 0, "
                + IMAGE + " TEXT);";
    }

    /**
     *
     */
    static class DeviceEntry implements BaseColumns {
        static final String TABLE_DEVICE = "table_device";
        static final String IP = "IP";
        static final String PORT = "PORT";
        static final String SSID = "SSID";
        static final String NAME = "NAME";
        static final String VENDOR = "VENDOR";
        static final String LAST_SEEN = "LAST_SEEN";
        static final String IS_PROTECTED = "IS_PROTECTED";
        static final String USERNAME = "USERNAME";
        static final String PASSWORD = "PASSWORD";
        static final String IS_LOCKED = "IS_LOCKED";
        static final String ENDPOINT = "ENDPOINT";
        static final String CREATE_TABLE_DEVICE = "CREATE TABLE " + TABLE_DEVICE + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + IP + " TEXT NOT NULL, "
                + PORT + " INTEGER NOT NULL, "
                + SSID + " TEXT NOT NULL, "
                + NAME + " TEXT, "
                + VENDOR + " TEXT, "
                + LAST_SEEN + " INTEGER NOT NULL, "
                + IS_PROTECTED + " INTEGER DEFAULT 0, "
                + USERNAME + " TEXT, "
                + PASSWORD + " TEXT, "
                + IS_LOCKED + " INTEGER DEFAULT 0, "
                + ENDPOINT + " TEXT);";
    }

    /**
     * Every device has at least one action, like "power on", "power off", "toggle", etc. Unlike StoryDeviceEntry,
     * this table is a simple 1 to N relationship. At this point, each action describes only a device: it's not linked to any story.
     */
    static class ActionEntry implements BaseColumns {
        static final String TABLE_ACTION = "table_action";
        static final String DEVICE_ID = "DEVICE_ID";
        static final String NAME = "NAME";
        static final String CMD = "CMD";
        static final String CREATE_TABLE_ACTION = "CREATE TABLE " + TABLE_ACTION + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + DEVICE_ID + " INTEGER NOT NULL, "
                + NAME + " TEXT NOT NULL, "
                + CMD + " TEXT NOT NULL, UNIQUE (" + DEVICE_ID + ", " + NAME + ", " + CMD + ") ON CONFLICT REPLACE);";
    }

    /**
     * Junction table to link a story with some devices. It's possible to create as many stories as one wants, so
     * it's a mapped with a complex relationship N to N.
     */
    static class StoryDeviceEntry implements BaseColumns {
        static final String TABLE_JUNCTION_STORY_DEVICE = "table_story_device";
        static final String STORY_ID = "STORY_ID";
        static final String DEVICE_ID = "DEVICE_ID";
        static final String CREATE_TABLE_STORY_DEVICE = "CREATE TABLE " + TABLE_JUNCTION_STORY_DEVICE + " ("
                + STORY_ID + " INTEGER NOT NULL, "
                + DEVICE_ID + " INTEGER NOT NULL);";
    }


    static class StoryDeviceActionEntry implements BaseColumns {
        static final String TABLE_JUNCTION_STORY_DEVICE_ACTION = "table_story_device_action";
        static final String STORY_ID = "STORY_ID";
        static final String STORY_ENABLED = "STORY_ENABLED";
        static final String DEVICE_ID = "DEVICE_ID";
        static final String ACTION_ID = "ACTION_ID";
        static final String CREATE_TABLE_STORY_DEVICE_ACTION = "CREATE TABLE " + TABLE_JUNCTION_STORY_DEVICE_ACTION + " ("
                + STORY_ID + " INTEGER NOT NULL, "
                + STORY_ENABLED + " INTEGER NOT NULL, "
                + DEVICE_ID + " INTEGER NOT NULL, "
                + ACTION_ID + " INTEGER NOT NULL, UNIQUE (" + STORY_ID + ", " + STORY_ENABLED + ", " + DEVICE_ID + ", " + ACTION_ID + ") ON CONFLICT REPLACE);";
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
                getDevicesForStory(story);
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
        values.put(DeviceEntry.PORT, device.getPort());
        values.put(DeviceEntry.SSID, device.getSSID());
        values.put(DeviceEntry.NAME, device.getName());
        values.put(DeviceEntry.VENDOR, device.getVendor());
        values.put(DeviceEntry.LAST_SEEN, System.currentTimeMillis());
        values.put(DeviceEntry.IS_PROTECTED, device.isProtected() ? 1 : 0);
        values.put(DeviceEntry.USERNAME, device.getUsername());
        values.put(DeviceEntry.PASSWORD, device.getPassword());
        values.put(DeviceEntry.IS_LOCKED, device.isLocked() ? 1 : 0);
        values.put(DeviceEntry.ENDPOINT, device.getEndpoint());
        int id = (int) sqLiteDatabase.insert(DeviceEntry.TABLE_DEVICE, null, values);
        close();
        return id;
    }

    public void updateDevice(DeviceDAO device) {
        open();
        ContentValues values = new ContentValues();
        values.put(DeviceEntry.IP, device.getIP());
        values.put(DeviceEntry.PORT, device.getPort());
        values.put(DeviceEntry.SSID, device.getSSID());
        values.put(DeviceEntry.NAME, device.getName());
        values.put(DeviceEntry.VENDOR, device.getVendor());
        values.put(DeviceEntry.LAST_SEEN, System.currentTimeMillis());
        values.put(DeviceEntry.IS_PROTECTED, device.isProtected() ? 1 : 0);
        values.put(DeviceEntry.USERNAME, device.getUsername());
        values.put(DeviceEntry.PASSWORD, device.getPassword());
        values.put(DeviceEntry.IS_LOCKED, device.isLocked() ? 1 : 0);
        values.put(DeviceEntry.ENDPOINT, device.getEndpoint());
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
                        DeviceEntry.PORT,
                        DeviceEntry.SSID,
                        DeviceEntry.NAME,
                        DeviceEntry.VENDOR,
                        DeviceEntry.LAST_SEEN,
                        DeviceEntry.IS_PROTECTED,
                        DeviceEntry.USERNAME,
                        DeviceEntry.PASSWORD,
                        DeviceEntry.IS_LOCKED,
                        DeviceEntry.ENDPOINT
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
                device.setPort(entries.getInt(++i));
                device.setSSID(entries.getString(++i));
                device.setName(entries.getString(++i));
                device.setVendor(entries.getString(++i));
                device.setLastSeen(entries.getString(++i));
                device.setProtected(entries.getInt(++i) == 1);
                device.setUsername(entries.getString(++i));
                device.setPassword(entries.getString(++i));
                device.setLocked(entries.getInt(++i) == 1);
                device.setEndpoint(entries.getString(++i));
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
                sqLiteDatabase.insert(StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE, null, values);
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
                DeviceEntry.PORT,
                DeviceEntry.SSID,
                DeviceEntry.NAME,
                DeviceEntry.VENDOR,
                DeviceEntry.LAST_SEEN,
                DeviceEntry.IS_PROTECTED,
                DeviceEntry.USERNAME,
                DeviceEntry.PASSWORD,
                DeviceEntry.IS_LOCKED,
                DeviceEntry.ENDPOINT
            },
            StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE + "." + StoryDeviceEntry.STORY_ID + " = ?",
            new String[] { String.valueOf(story.getId()) }, null,null, null);

        List<DeviceDAO> devices = new ArrayList<>();
        while (entries.moveToNext()) {
            int i = -1;
            DeviceDAO device = new DeviceDAO((int) entries.getLong(++i));
            device.setIP(entries.getString(++i));
            device.setPort(entries.getInt(++i));
            device.setSSID(entries.getString(++i));
            device.setName(entries.getString(++i));
            device.setVendor(entries.getString(++i));
            device.setLastSeen(entries.getString(++i));
            device.setProtected(entries.getInt(++i) == 1);
            device.setUsername(entries.getString(++i));
            device.setPassword(entries.getString(++i));
            device.setLocked(entries.getInt(++i) == 1);
            device.setEndpoint(entries.getString(++i));
            devices.add(device);
        }
        entries.close();
        story.setDevices(devices);
    }

    /// DEVICE AND ACTIONS

    public void createActionsForDevice(DeviceDAO device, List<DeviceActionDAO> actions) {
        open();
        sqLiteDatabase.beginTransaction();
        for (DeviceActionDAO action : actions) {
            ContentValues values = new ContentValues();
            values.put(ActionEntry.DEVICE_ID, device.getId());
            values.put(ActionEntry.NAME, action.getName());
            values.put(ActionEntry.CMD, action.getCommand());
            sqLiteDatabase.insert(ActionEntry.TABLE_ACTION, null, values);
        }

        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
        close();
    }

    public List<DeviceActionDAO> getActionsForDevice(int deviceId) {
        open();
        Log.d(TAG, "device = " + deviceId);
        Cursor entries = sqLiteDatabase.query(ActionEntry.TABLE_ACTION,
                new String[] {
                    ActionEntry._ID,
                    ActionEntry.NAME,
                    ActionEntry.CMD
                },
                ActionEntry.DEVICE_ID + " = ?",
                new String[] { String.valueOf(deviceId) }, null,null,null);

        List<DeviceActionDAO> deviceActions = new ArrayList<>();
        while (entries.moveToNext()) {
            DeviceActionDAO deviceAction = new DeviceActionDAO(deviceId, entries.getString(1), entries.getString(2));
            deviceAction.setId(entries.getInt(0));
            deviceActions.add(deviceAction);
        }
        entries.close();
        close();
        return deviceActions;
    }

    /**
     *
     * @param storyId the story
     * @param deviceId the device
     * @return list
     */
    public List<StoryDeviceActionDAO> getActionsForStoryAndDevice(long storyId, int deviceId) {
        open();
        Log.d(TAG, "story = " + storyId + ", device = " + deviceId);
        Cursor entries = sqLiteDatabase.query(StoryDeviceActionEntry.TABLE_JUNCTION_STORY_DEVICE_ACTION,
                new String[] {
                    StoryDeviceActionEntry.STORY_ENABLED,
                    StoryDeviceActionEntry.ACTION_ID
                },
                StoryDeviceActionEntry.STORY_ID + " = ? AND " + StoryDeviceActionEntry.DEVICE_ID + " = ?",
                new String[] { String.valueOf(storyId), String.valueOf(deviceId) }, null,null,null);

        List<StoryDeviceActionDAO> storyDeviceActions = new ArrayList<>();
        while (entries.moveToNext()) {
            int i = -1;
            StoryDeviceActionDAO storyDeviceAction = new StoryDeviceActionDAO();
            storyDeviceAction.setStoryId(storyId);
            storyDeviceAction.setEnabled(entries.getInt(++i) == 1);
            storyDeviceAction.setDeviceId(deviceId);
            storyDeviceAction.setActionId(entries.getLong(++i));
            storyDeviceActions.add(storyDeviceAction);
        }
        entries.close();
        close();
        return storyDeviceActions;
    }

    public void createActionForStoryAndDevice(long storyId, boolean enabled, DeviceActionDAO action) {
        open();
        ContentValues values = new ContentValues();
        values.put(StoryDeviceActionEntry.STORY_ID, storyId);
        values.put(StoryDeviceActionEntry.STORY_ENABLED, enabled);
        values.put(StoryDeviceActionEntry.DEVICE_ID, action.getDeviceId());
        values.put(StoryDeviceActionEntry.ACTION_ID, action.getId());
        Log.d(TAG, "createActionForStoryAndDevice = " + storyId + ", " + enabled + ", " + action.getName());
        sqLiteDatabase.insert(StoryDeviceActionEntry.TABLE_JUNCTION_STORY_DEVICE_ACTION, null, values);
        close();
    }

    public void deleteActionForStoryAndDevice(long storyId, boolean enabled, DeviceActionDAO action) {
        open();
        String e = enabled ? "1" : "0";
        String selection = StoryDeviceActionEntry.STORY_ID + " = ? AND "
                + StoryDeviceActionEntry.STORY_ENABLED + " = ? AND "
                + StoryDeviceActionEntry.DEVICE_ID + " = ? AND "
                + StoryDeviceActionEntry.ACTION_ID + " = ?";
        String[] selectionArgs = { String.valueOf(storyId), e, String.valueOf(action.getDeviceId()), String.valueOf(action.getId()) };
        sqLiteDatabase.delete(StoryDeviceActionEntry.TABLE_JUNCTION_STORY_DEVICE_ACTION, selection, selectionArgs);
        close();
    }
}
