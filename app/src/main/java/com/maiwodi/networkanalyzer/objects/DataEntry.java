package com.maiwodi.networkanalyzer.objects;

public class DataEntry {
    private String timeStamp;
    private int rssiValue;

    public DataEntry(String timeStamp, int rssiValue) {
        this.timeStamp = timeStamp;
        this.rssiValue = rssiValue;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public int getRssiValue() {
        return rssiValue;
    }

    @Override
    public String toString() {
        return "DataEntry{" +
                "timeStamp='" + timeStamp + '\'' +
                ", rssiValue=" + rssiValue +
                '}';
    }
}
