package com.example.hyyyyyde.teamt_beacon;


import java.io.Serializable;

public class BeaconValue implements Serializable {
    private String uuid;

    private String major;

    private String minor;

    private String distance;

    private String rssi;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getMajor() {
        return this.major;
    }

    public String getMinor() {
        return this.minor;
    }

    public String getDistance() {
        return this.distance;
    }

    public String getRssi() {
        return this.rssi;
    }
}
