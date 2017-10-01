package org.mbach.homeautomation.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import org.mbach.homeautomation.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * HomeAutomationDB.
 *
 * @author Matthieu BACHELIER
 * @since 2017-10
 */
public class OuiDB {
    private SQLiteDatabase sqLiteDatabase;
    private static final int DB_VERSION = 1;
    private final SQLiteHelper helper;
    private Context context;

    private static final String DB_NAME = "oui.db";

    private static final String TAG = "OuiDB";

    /**
     * StoryEntry is an inner class which maps to a Story.
     *
     * @link org.mbach.homeautomation.story.StoryDAO
     */
    static class OuiEntry implements BaseColumns {
        static final String TABLE_OUI = "table_oui";
        static final String MAC = "MAC";
        static final String SHORT_NAME = "SHORT_NAME";
        static final String FULL_NAME = "FULL_NAME";
        static final String CREATE_TABLE_OUI = "CREATE TABLE " + TABLE_OUI + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + MAC + " TEXT NOT NULL, "
                + SHORT_NAME + " TEXT NOT NULL, "
                + FULL_NAME + " TEXT);";
    }

    public OuiDB(Context context){
        helper = new SQLiteHelper(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    private void open(){
        sqLiteDatabase = helper.getWritableDatabase();
    }

    private void close(){
        sqLiteDatabase.close();
    }

    private boolean hasData() {
        Cursor entry = sqLiteDatabase.query(OuiEntry.TABLE_OUI, new String[] { OuiEntry._ID },
                null,null, null,null, null);
        boolean notEmpty = entry.moveToFirst();
        entry.close();
        return notEmpty;
    }

    public void populateFromLocalResource() {
        open();
        Log.d(TAG, "populateFromLocalResource");
        if (hasData()) {
            Log.d(TAG, "we have some data, skipping...");
            return;
        }
        sqLiteDatabase.beginTransaction();

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(this.context.getResources().openRawResource(R.raw.manuf)));
            String line;
            ContentValues values = new ContentValues();
            while ((line = r.readLine()) != null) {
                String[] tokens = line.split("\t");
                values.put(OuiEntry.MAC, tokens[0]);
                values.put(OuiEntry.SHORT_NAME, tokens[1]);
                if (tokens.length == 3) {
                    values.put(OuiEntry.FULL_NAME, tokens[2]);
                }
                sqLiteDatabase.insert(OuiEntry.TABLE_OUI, null, values);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        sqLiteDatabase.endTransaction();
        close();
    }

}
