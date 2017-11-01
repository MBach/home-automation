package org.mbach.homeautomation.device;

/**
 * DeviceActionDAO class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-10
 */
public class DeviceActionDAO {

    private long id;
    private int deviceId;
    private String name;
    private String command;

    public DeviceActionDAO(int deviceId, String name, String command) {
        this.deviceId = deviceId;
        this.name = name;
        this.command = command;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

}
