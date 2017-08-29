package org.mbach.homeautomation.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        long id = sqLiteDatabase.insert(SQLiteHelper.StoryEntry.TABLE_STORY, null, values);
        close();
        return id;
    }

    public boolean updateStory(StoryDAO story) {
        open();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.StoryEntry.STORY_TITLE, story.getTitle());
        values.put(SQLiteHelper.StoryEntry.LAST_MODIFIED, System.currentTimeMillis());
        values.put(SQLiteHelper.StoryEntry.ENABLED, story.isEnabled());

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
                new String[] { SQLiteHelper.StoryEntry._ID, SQLiteHelper.StoryEntry.STORY_TITLE, SQLiteHelper.StoryEntry.ENABLED },
                SQLiteHelper.StoryEntry._ID,
                null, null,null,
                SQLiteHelper.StoryEntry.LAST_MODIFIED + " DESC");
        List<StoryDAO> results = new ArrayList<>();
        if (entries.getCount() != 0) {
            while (entries.moveToNext()) {
                StoryDAO story = new StoryDAO(entries.getLong(0));
                story.setTitle(entries.getString(1));
                story.setEnabled(entries.getInt(2) == 1);
                results.add(story);
            }
        }
        entries.close();
        close();
        return results;
    }

    public StoryDAO getStory(long idToFind) {
        open();
        Cursor entries = sqLiteDatabase.query(SQLiteHelper.StoryEntry.TABLE_STORY,
                new String[] { SQLiteHelper.StoryEntry._ID },
                SQLiteHelper.StoryEntry._ID,
                new String[] { String.valueOf(idToFind) }, null,null, null);
        StoryDAO result = null;
        while (entries.moveToNext()) {
            long id = entries.getLong(0);
            if (id == idToFind) {
                result = new StoryDAO(id);
                result.setTitle(entries.getString(1));
                break;
            }
        }
        entries.close();
        close();
        return result;
    }
}
