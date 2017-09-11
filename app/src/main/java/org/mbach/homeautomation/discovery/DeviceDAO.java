package org.mbach.homeautomation.discovery;

/**
 * Story.
 *
 * @author Matthieu BACHELIER
 * @since 2017-09
 */
public class DeviceDAO {

    private int id;
    private String IP;
    private String SSID;
    private String name;
    private String vendor;
    private String lastSeen;

    public DeviceDAO() {
        this.id = -1;
    }

    public DeviceDAO(int id) {
        this.id = id;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
