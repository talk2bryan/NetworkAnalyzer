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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.maiwodi.networkanalyzer.objects.DataEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ConnectivitySensor implements Serializable {
    private static Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());

    private ArrayList<DataEntry> dataEntries;

    public ConnectivitySensor() {
        LOGGER.info("ConnectivitySensor instantiated.");
        dataEntries = new ArrayList<>();
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
        System.out.println(timeStamp + ": " + rssiValue + ", " + speedInMbpsSpeed);
        LOGGER.info(timeStamp + ": " + rssiValue + ", " + speedInMbpsSpeed);
        dataEntries.add(new DataEntry(timeStamp, rssiValue, speedInMbpsSpeed));
    }

    public void sendDataToCloud() {
        if (this.dataEntries != null) {
            new HTTPNetworkAsyncTask().execute("http://ec2-3-94-173-58.compute-1.amazonaws.com:8080/networkanalyzer-1.0-SNAPSHOT/rest/myresource/post/data", buidJsonObject());
        }
    }


    private String buidJsonObject() {
        final ObjectMapper objectMapper = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        ObjectWriter ow = objectMapper.writerWithType(new TypeReference<ConnectivitySensor>() {
        });
        String json = null;
        try {
            json = ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        LOGGER.info(json);
        return json;
    }
}
