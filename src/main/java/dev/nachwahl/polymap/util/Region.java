package dev.nachwahl.polymap.util;

import java.util.Date;
import java.util.UUID;

public class Region {
    private String objectID;
    private UUID uid;
    private Date createdDate;
    private String username;
    private UUID useruuid;
    private String data;
    private String city;
    private int area;

    public Region(String username, UUID useruuid, String data, String city, int area) {
        this.uid = UUID.randomUUID();
        this.objectID = this.uid.toString();
        this.createdDate = new Date();
        this.username = username;
        this.useruuid = useruuid;
        this.data = data;
        this.city = city;
        this.area = area;
    }

    public UUID getUid() {
        return uid;
    }
    public String getObjectID() {
        return objectID;
    }

    public String getData() {
        return data;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUseruuid() {
        return useruuid;
    }

    public void setUseruuid(UUID useruuid) {
        this.useruuid = useruuid;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }
}
