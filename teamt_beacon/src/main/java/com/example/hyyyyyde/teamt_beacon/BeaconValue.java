package com.example.hyyyyyde.teamt_beacon;


import java.io.Serializable;

public class BeaconValue implements Serializable {
    private String uuid;

    private String major;

    private String minor;

    private double distance;

    private int rssi;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setRssi(int rssi) {
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

    public double getDistance() {
        return this.distance;
    }

    public int getRssi() {
        return this.rssi;
    }
}
