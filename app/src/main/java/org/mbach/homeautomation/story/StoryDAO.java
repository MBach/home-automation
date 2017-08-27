package org.mbach.homeautomation.story;

/**
 * Story.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class StoryDAO {

    private int id;
    private String title;

    public StoryDAO() {
        this.id = -1;

    }

    public StoryDAO(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
