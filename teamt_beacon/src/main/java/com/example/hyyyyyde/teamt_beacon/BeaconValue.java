package com.example.hyyyyyde.teamt_beacon;


import java.io.Serializable;

public class BeaconValue implements Serializable {
    private String uuid;

    private String major;

    private String minor;

    private double distance;

    private int rssi;

    private int txPower;

    private boolean connected;

    public BeaconValue setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public BeaconValue setMajor(String major) {
        this.major = major;
        return this;
    }

    public BeaconValue setMinor(String minor) {
        this.minor = minor;
        return this;
    }

    public BeaconValue setDistance(double distance) {
        this.distance = distance;
        return this;
    }

    public BeaconValue setRssi(int rssi) {
        this.rssi = rssi;
        return this;
    }

    public BeaconValue setConnected(boolean connected) {
        this.connected = connected;
        return this;
    }

    public BeaconValue setTxPower(int txPower) {
        this.txPower = txPower;
        return this;
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

    public int getTxPower() {
        return this.txPower;
    }

    public boolean getConnected() {
        return this.connected;
    }

    @Override
    public String toString() {
        return "Uuid:" + uuid + "\nmajor:" + major + " minor:" + minor + "\ndistance:" + distance + " rssi:" + rssi;
    }
}
