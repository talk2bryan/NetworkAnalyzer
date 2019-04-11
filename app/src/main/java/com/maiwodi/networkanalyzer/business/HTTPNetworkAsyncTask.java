package com.maiwodi.networkanalyzer.business;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HTTPNetworkAsyncTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {
        try {
            return httpPost(urls[0], urls[1]);
        } catch (IOException ioe) {
            return "Unable to retrieve web page. Invalid url.";
        }
    }

    protected void onPostExecute(String result) {
        /* TODO: A desired action not yet determined. */
    }

    private String httpPost(String myUrl, String jsonDataAsString) throws IOException {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(JSON, jsonDataAsString);

        Request request = new Request.Builder()
                .url(myUrl)
                .post(requestBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);

            System.out.println("\n\n\nTHE RESPONSE WAS " + responseBodyCopy.string());
            return response.body().string();
        }
    }


}
