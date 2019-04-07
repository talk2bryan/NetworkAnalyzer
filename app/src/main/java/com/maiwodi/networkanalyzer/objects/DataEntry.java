package com.maiwodi.networkanalyzer.objects;

public class DataEntry {
    private String timeStamp;
    private int rssiValue;
    private int speedInMbps;

    public DataEntry(String timeStamp, int rssiValue, int speedInMbps) {
        this.timeStamp = timeStamp;
        this.rssiValue = rssiValue;
        this.speedInMbps = speedInMbps;
    }
}
