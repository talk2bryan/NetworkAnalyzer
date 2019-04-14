package com.maiwodi.networkanalyzer.services;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utilities {
    private static String BASE_ADDRESS;
    private static String DEFAULT_SERVER_IP;
    private static String POST_RESOURCE_PATH;
    private static String GET_RESOURCE_PATH;
    private static String DATA_RESOURCE_PATH;

    public Utilities(Context context, String propertyFileName) {
        loadProperties(context, propertyFileName);
    }

    private void loadProperties(Context context, String propertyFileName) {
        try {
            InputStream is = context.getAssets().open(propertyFileName);
            Properties props = new Properties();
            props.load(is);

            BASE_ADDRESS = props.getProperty("base_address", "");
            DATA_RESOURCE_PATH = props.getProperty("analyzed_data_path", "");
            DEFAULT_SERVER_IP = props.getProperty("default_server_ip", "");
            POST_RESOURCE_PATH = props.getProperty("post_resource_path", "");
            GET_RESOURCE_PATH = props.getProperty("get_resource_path", "");

            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String loadGetResourcePath(String userMasterIP) {
        String getUrl = String.format(
                "%s%s%s", BASE_ADDRESS,
                (userMasterIP.length() == 0 ? DEFAULT_SERVER_IP : userMasterIP),
                GET_RESOURCE_PATH
        );
        return getUrl;
    }

    public String loadPostResourcePath(String userMasterIP) {
        String postUrl = String.format(
                "%s%s%s", BASE_ADDRESS,
                (userMasterIP.length() == 0 ? DEFAULT_SERVER_IP : userMasterIP),
                POST_RESOURCE_PATH
        );
        return postUrl;
    }

    public String loadAnalyzedDataLocationURL(String userMasterIP) {
        String postUrl = String.format(
                "%s%s%s", BASE_ADDRESS,
                (userMasterIP.length() == 0 ? DEFAULT_SERVER_IP : userMasterIP),
                POST_RESOURCE_PATH
        );
        return postUrl;
    }
}
