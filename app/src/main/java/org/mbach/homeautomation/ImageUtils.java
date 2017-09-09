package org.mbach.homeautomation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.mbach.homeautomation.story.StoryDAO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Constants class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-09
 */
public final class ImageUtils {

    public static Bitmap loadImage(Context context, StoryDAO story) {
        if (story.getCoverPath() != null) {
            try {
                FileInputStream fis = context.openFileInput(story.getCoverPath());
                return BitmapFactory.decodeStream(fis);
            } catch (FileNotFoundException e) {
                //Log.d(TAG, "Cover not found!");
                e.printStackTrace();
            }
        }
        return null;
    }
}
