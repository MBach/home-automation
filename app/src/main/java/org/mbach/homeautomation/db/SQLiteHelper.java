package org.mbach.homeautomation.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteHelper.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
class SQLiteHelper extends SQLiteOpenHelper {

    SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(OuiDB.OuiEntry.CREATE_TABLE_OUI);
        sqLiteDatabase.execSQL(HomeAutomationDB.StoryEntry.CREATE_TABLE_STORY);
        sqLiteDatabase.execSQL(HomeAutomationDB.DeviceEntry.CREATE_TABLE_DEVICE);
        sqLiteDatabase.execSQL(HomeAutomationDB.StoryDeviceEntry.CREATE_TABLE_STORY_DEVICE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(String.format("DROP TABLE %s;", OuiDB.OuiEntry.CREATE_TABLE_OUI));
        sqLiteDatabase.execSQL(String.format("DROP TABLE %s;", HomeAutomationDB.StoryEntry.TABLE_STORY));
        sqLiteDatabase.execSQL(String.format("DROP TABLE %s;", HomeAutomationDB.DeviceEntry.TABLE_DEVICE));
        sqLiteDatabase.execSQL(String.format("DROP TABLE %s;", HomeAutomationDB.StoryDeviceEntry.TABLE_JUNCTION_STORY_DEVICE));
        onCreate(sqLiteDatabase);
    }
}
