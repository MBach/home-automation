package org.mbach.homeautomation.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.mbach.homeautomation.story.StoryDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * StoryDB.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class StoryDB {
    private SQLiteDatabase sqLiteDatabase;
    private static final int DB_VERSION = 1;
    private final StatisticSQLiteOpenHelper statisticSQLiteOpenHelper;

    private static final String DB_NAME = "home-automation.db";

    public StoryDB(Context context){
        statisticSQLiteOpenHelper = new StatisticSQLiteOpenHelper(context, DB_NAME, null, DB_VERSION);
    }

    private void open(){
        sqLiteDatabase = statisticSQLiteOpenHelper.getWritableDatabase();
    }

    private void close(){
        sqLiteDatabase.close();
    }

    public int create(StoryDAO story) {
        open();
        close();
        return -1;
    }

    public int update(StoryDAO story) {
        open();
        close();
        return -1;
    }

    public List<StoryDAO> getStories() {
        open();
        Cursor entries = sqLiteDatabase.query(StatisticSQLiteOpenHelper.TABLE_STORY,
                new String[] { StatisticSQLiteOpenHelper.COL_STORY_ID },
                StatisticSQLiteOpenHelper.COL_STORY_ID,
                null, null,null,
                StatisticSQLiteOpenHelper.COL_LAST_MODIFIED + " DESC");
        List<StoryDAO> results = new ArrayList<>();
        if (entries.getCount() != 0) {
            while (entries.moveToNext()) {
                StoryDAO story = new StoryDAO(entries.getInt(0));
                results.add(story);
            }
        }
        entries.close();
        close();
        return results;
    }

    public StoryDAO getStory(int idToFind) {
        open();
        Cursor entries = sqLiteDatabase.query(StatisticSQLiteOpenHelper.TABLE_STORY,
                new String[] { StatisticSQLiteOpenHelper.COL_STORY_ID },
                StatisticSQLiteOpenHelper.COL_STORY_ID,
                new String[] { String.valueOf(idToFind) }, null,null, null);
        StoryDAO result = null;
        while (entries.moveToNext()) {
            int id = entries.getInt(0);
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
