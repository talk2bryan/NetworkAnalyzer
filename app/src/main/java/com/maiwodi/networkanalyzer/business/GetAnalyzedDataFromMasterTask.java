package com.maiwodi.networkanalyzer.business;

import android.os.AsyncTask;

import com.maiwodi.networkanalyzer.persistence.AsyncResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetAnalyzedDataFromMasterTask extends AsyncTask<String, Void, String> {

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;

    private AsyncResponse delegate;
    private String responseMessage;

    GetAnalyzedDataFromMasterTask(/*AsyncResponse delegate*/) {
//        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... strings) {
        String urlString = strings[0];

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection =
                    (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod(REQUEST_METHOD);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);

            urlConnection.connect();

            // Get response
            InputStreamReader inputStreamReader = new InputStreamReader(
                    urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }

            bufferedReader.close();
            responseMessage = stringBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
            responseMessage = null;
        }

        return responseMessage;
    }
}
