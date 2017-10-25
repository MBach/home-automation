package org.mbach.homeautomation.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Story.
 *
 * @author Matthieu BACHELIER
 * @since 2017-09
 */
public class DeviceDAO implements Parcelable {

    private int id;
    private String IP;
    private int port;
    private String SSID;
    private String name;
    private String vendor;
    private String lastSeen;
    private boolean isProtected;

    private List<DeviceActionDAO> actions;

    public static final Parcelable.Creator<DeviceDAO> CREATOR = new Parcelable.Creator<DeviceDAO>() {
        public DeviceDAO createFromParcel(Parcel in) {
            return new DeviceDAO(in);
        }

        public DeviceDAO[] newArray(int size) {
            return new DeviceDAO[size];
        }
    };

    private DeviceDAO(Parcel in) {
        id = in.readInt();
        IP = in.readString();
        port = in.readInt();
        SSID = in.readString();
        name = in.readString();
        vendor = in.readString();
        lastSeen = in.readString();
        isProtected = in.readInt() == 1;
        // actions = in.readArrayList(DeviceActionDAO.class.getClassLoader());
    }

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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public List<DeviceActionDAO> getActions() {
        return actions;
    }

    public void setActions(List<DeviceActionDAO> actions) {
        this.actions = actions;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public boolean isProtected() {
        return isProtected;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(IP);
        parcel.writeInt(port);
        parcel.writeString(SSID);
        parcel.writeString(name);
        parcel.writeString(vendor);
        parcel.writeString(lastSeen);
        parcel.writeInt(isProtected ? 1 : 0);
        // parcel.writeArray(actions.toArray());
    }
}
