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
    private static final  String BASE_ADDRESS = "http://";
    private static final String DEFAULT_SERVER_IP = "140.193.200.126";
    private static final String POST_RESOURCE_PATH = ":8080/networkanalyzer/rest/myresource/post/data";


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
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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


    public void sendDataToCloud(String userMasterIP) {
        if (this.dataEntries != null) {
            LOGGER.info(buildJsonString());

            String postUrl = String.format(
              "%s%s%s", BASE_ADDRESS,
                    (userMasterIP.length() == 0 ? DEFAULT_SERVER_IP : userMasterIP),
                    POST_RESOURCE_PATH
            );

            new CallAPI().execute(postUrl, buildJsonString());
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
