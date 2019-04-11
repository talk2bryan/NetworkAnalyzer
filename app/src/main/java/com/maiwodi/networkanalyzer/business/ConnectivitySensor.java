package com.maiwodi.networkanalyzer.business;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.maiwodi.networkanalyzer.objects.DataEntry;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ConnectivitySensor {
    private static Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());

    private ArrayList<DataEntry> dataEntries;
    private static final String AWS_SERVER = "http://ec2-3-94-173-58.compute-1.amazonaws.com:8080/networkanalyzer-1.0-SNAPSHOT/rest/myresource/post/data";

    public ConnectivitySensor() {
        LOGGER.info("ConnectivitySensor instantiated.");
        dataEntries = null;
    }


    @RequiresApi(api = VERSION_CODES.M)
    public boolean hasInternetConnection(final Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        final Network network = connectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = connectivityManager
                .getNetworkCapabilities(network);

        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    public void recordWifiSignalStrength(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int rssiValue = wifiInfo.getRssi(); // Unit is dBm.
        int speedInMbpsSpeed = wifiInfo.getLinkSpeed();

        Long timeStampLong = System.currentTimeMillis() / 1000;
        String timeStamp = timeStampLong.toString();
        LOGGER.info(timeStamp + ", " + rssiValue + ", " + speedInMbpsSpeed);

        if (dataEntries == null)
            dataEntries = new ArrayList<>();
        dataEntries.add(new DataEntry(timeStamp, rssiValue, speedInMbpsSpeed));
    }


    public void sendDataToCloud() {
        if (this.dataEntries != null) {
            LOGGER.info(buildJsonString());

            new CallAPI().execute(AWS_SERVER, buildJsonString());
            dataEntries = null;
        }
    }

    private String buildJsonString() {

        final ObjectMapper objectMapper = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(dataEntries);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }
}
