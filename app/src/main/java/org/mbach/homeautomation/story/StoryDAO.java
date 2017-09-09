package org.mbach.homeautomation.story;

/**
 * Story.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class StoryDAO {

    private long id;
    private String title;
    private boolean enabled;
    private String coverPath;

    public StoryDAO() {
        this.id = -1;
    }

    public StoryDAO(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }
}
