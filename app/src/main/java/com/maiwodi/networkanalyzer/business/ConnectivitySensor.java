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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class ConnectivitySensor {
    private static Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());

    private ArrayList<DataEntry> dataEntries;
    private static String BASE_ADDRESS;
    private static String DEFAULT_SERVER_IP;
    private static String POST_RESOURCE_PATH;


    public ConnectivitySensor(Context context) {
        LOGGER.info("ConnectivitySensor instantiated.");

        try {
            InputStream is = context.getAssets().open("config.properties");
            Properties props = new Properties();
            props.load(is);

            BASE_ADDRESS = props.getProperty("base_address", "");
            DEFAULT_SERVER_IP = props.getProperty("default_server_ip", "");
            POST_RESOURCE_PATH = props.getProperty("post_resource_path", "");

            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        final String timeStamp = timeStampLong.toString();

        new SpeedTestTask(new SpeedTestTask.AsyncResponse() {
            @Override
            public void processFinish(double downloadSpeed) {
                if (dataEntries == null)
                    dataEntries = new ArrayList<>();
                dataEntries.add(new DataEntry(timeStamp, RSSIVALUE, SPEEDINMBPS, downloadSpeed));
            }
        }).execute();
    }


    public void sendDataToCloud(String userMasterIP) {
        if (this.dataEntries != null) {
            final String dataEntriesAsJsonString = buildJsonString();
//            LOGGER.info(dataEntriesAsJsonString);

            String postUrl = String.format(
              "%s%s%s", BASE_ADDRESS,
                    (userMasterIP.length() == 0 ? DEFAULT_SERVER_IP : userMasterIP),
                    POST_RESOURCE_PATH
            );

            new CallAPI().execute(postUrl, dataEntriesAsJsonString);
            System.out.println("Generated " + dataEntries.size() + " entries.");
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
