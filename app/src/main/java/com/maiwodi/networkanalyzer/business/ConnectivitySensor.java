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
import com.maiwodi.networkanalyzer.persistence.AsyncResponse;
import com.maiwodi.networkanalyzer.services.Utilities;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class ConnectivitySensor {
    private static final Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());

    private final Utilities RESOURCE_UTILS;

    private ArrayList<DataEntry> dataEntries;


    public ConnectivitySensor(Context context) {
        LOGGER.info("ConnectivitySensor instantiated.");

        RESOURCE_UTILS = new Utilities(context, "config.properties");
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
        final int RSSIVALUE = wifiInfo.getRssi(); // Unit is dBm.
        final int SPEEDINMBPS = wifiInfo.getLinkSpeed();

        Long timeStampLong = System.currentTimeMillis() / 1000;
        final String TIMESTAMP = timeStampLong.toString();

        try {
            new SpeedTestTask(new AsyncResponse() {
                @Override
                public void processDownloadSpeed(double downloadSpeed) {
                    if (dataEntries == null)
                        dataEntries = new ArrayList<>();
                    dataEntries.add(new DataEntry(TIMESTAMP, RSSIVALUE, SPEEDINMBPS, downloadSpeed));
                }
            }).execute().get(); // .get() forces the application to wait for this call to complete.
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void sendDataToCloud(String userMasterIP) {
        if (this.dataEntries != null) {
            String dataEntriesAsJsonString = buildJsonString();
//            LOGGER.info(dataEntriesAsJsonString);

            String postUrl = RESOURCE_UTILS.loadPostResourcePath(userMasterIP);
            try {
                new PostNetworkDataToMasterTask().execute(postUrl, dataEntriesAsJsonString).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Generated " + dataEntries.size() + " entries.");
            dataEntries = null;
        }
    }

    public String getNetworkDataAnalysis(String userMasterIP) {
        String getUrl = RESOURCE_UTILS.loadGetResourcePath(userMasterIP);
        String result = null;
        try {
            result = new GetAnalyzedDataFromMasterTask().execute(getUrl).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
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
