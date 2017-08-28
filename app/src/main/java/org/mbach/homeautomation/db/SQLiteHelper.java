package org.mbach.homeautomation.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * SQLiteHelper.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
class SQLiteHelper extends SQLiteOpenHelper {

    /**
     *
     */
    static class StoryEntry implements BaseColumns {
        static final String TABLE_STORY = "table_story";
        static final String STORY_TITLE = "STORY_TITLE";
        static final String LAST_MODIFIED = "LAST_MODIFIED";
        static final String CREATE_TABLE_STORY = "CREATE TABLE " + TABLE_STORY + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + STORY_TITLE + " TEXT NOT NULL, "
                + LAST_MODIFIED + " INTEGER NOT NULL);";
    }

    /**
     *
     */
    static class DeviceEntry implements BaseColumns {
        static final String TABLE_DEVICE = "table_device";
        static final String LAST_SEEN = "LAST_SEEN";
        static final String CREATE_TABLE_DEVICE = "CREATE TABLE " + TABLE_DEVICE + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + LAST_SEEN + " INTEGER NOT NULL);";
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

    SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(StoryEntry.CREATE_TABLE_STORY);
        sqLiteDatabase.execSQL(DeviceEntry.CREATE_TABLE_DEVICE);
        sqLiteDatabase.execSQL(StoryDeviceEntry.CREATE_TABLE_STORY_DEVICE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(String.format("DROP TABLE %s;", StoryEntry.TABLE_STORY));
        sqLiteDatabase.execSQL(String.format("DROP TABLE %s;", DeviceEntry.TABLE_DEVICE));
        sqLiteDatabase.execSQL(String.format("DROP TABLE %s;", StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE));
        onCreate(sqLiteDatabase);
    }
}
