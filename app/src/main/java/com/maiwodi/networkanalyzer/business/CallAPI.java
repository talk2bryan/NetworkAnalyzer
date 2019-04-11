package com.maiwodi.networkanalyzer.business;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class CallAPI extends AsyncTask<String, Void, String> {

    private static Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());


    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String urlString = strings[0];
        String data = strings[1];

        OutputStream out;
        String responseMessage = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection =
                    (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            urlConnection.setFixedLengthStreamingMode(data.length());

            out = new BufferedOutputStream(urlConnection.getOutputStream());

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            writer.write(data);
            writer.flush();
            writer.close();
            out.close();


            urlConnection.connect();
            final int RC = urlConnection.getResponseCode();

            if (RC == HttpURLConnection.HTTP_OK) {
                LOGGER.info("SendData - HTTP_OK");
            } else {
                LOGGER.warning("SendData Error: HTTP Code = " + RC);
            }

            responseMessage = urlConnection.getResponseMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseMessage;
    }
}
