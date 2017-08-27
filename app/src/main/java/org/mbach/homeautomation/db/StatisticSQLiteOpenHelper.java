package org.mbach.homeautomation.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * StatisticSQLiteOpenHelper.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class StatisticSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String TABLE_STORY = "table_story";
    public static final String TABLE_DEVICE = "table_device";
    public static final String COL_STORY_ID = "STORY_ID";
    public static final String COL_DEVICE_ID = "DEVICE_ID";
    public static final String COL_LAST_MODIFIED = "LAST_MODIFIED";
    public static final String COL_LAST_SEEN = "LAST_SEEN";

    private static final String CREATE_TABLE_STORY = "CREATE TABLE " + TABLE_STORY + " ("
            + COL_STORY_ID + " INTEGER PRIMARY KEY, "
            + COL_LAST_MODIFIED + " INTEGER NOT NULL);";

    private static final String CREATE_TABLE_DEVICE = "CREATE TABLE " + TABLE_DEVICE + " ("
            + COL_DEVICE_ID + " INTEGER PRIMARY KEY, "
            + COL_LAST_SEEN + " INTEGER NOT NULL);";

    public StatisticSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_STORY);
        sqLiteDatabase.execSQL(CREATE_TABLE_DEVICE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(String.format("DROP TABLE %s;", TABLE_STORY));
        sqLiteDatabase.execSQL(String.format("DROP TABLE %s;", TABLE_DEVICE));
        onCreate(sqLiteDatabase);
    }
}
