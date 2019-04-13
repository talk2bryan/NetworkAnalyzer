package com.maiwodi.networkanalyzer.objects;

public class DataEntry {
    private String timeStamp;
    private int rssiValue;
    private int speedInMbps;
    private double downloadSpeed;

    public DataEntry(String timeStamp, int rssiValue, int speedInMbps, double downloadSpeed) {
        this.timeStamp = timeStamp;
        this.rssiValue = rssiValue;
        this.speedInMbps = speedInMbps;
        this.downloadSpeed = downloadSpeed;
    }
}
