package org.mbach.homeautomation.device;

/**
 * DeviceActionDAO class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-10
 */
public class StoryDeviceActionDAO {

    private long storyId;
    private boolean enabled;
    private int deviceId;
    private long actionId;

    public long getStoryId() {
        return storyId;
    }

    public void setStoryId(long storyId) {
        this.storyId = storyId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public long getActionId() {
        return actionId;
    }

    public void setActionId(long actionId) {
        this.actionId = actionId;
    }
}
