package com.maiwodi.networkanalyzer.business;

import android.os.AsyncTask;

import java.math.BigDecimal;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class SpeedTestTask extends AsyncTask<Void, Void, Void> {
    private AsyncResponse delegate;

    SpeedTestTask(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Void doInBackground(Void... params) {

        SpeedTestSocket speedTestSocket = new SpeedTestSocket();

        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onCompletion(SpeedTestReport report) {
                // called when download/upload is finished
                BigDecimal bitsPerSecond = report.getTransferRateBit();
                double mbps = bpsToMbps(bitsPerSecond);
                delegate.processFinish(mbps);
//                Log.v("speedtest", "[COMPLETED] rate in MB/s   : " + mbps);
            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                // called when a download/upload error occur
            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                // called to notify download/upload progress
                /*Log.v("speedtest", "[PROGRESS] progress : " + percent + "%");
                Log.v("speedtest", "[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                Log.v("speedtest", "[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());*/
            }

            private double bpsToMbps(BigDecimal bitsPerSecond) {
                BigDecimal oneBitInMb = BigDecimal.valueOf(0.000000125);
                BigDecimal result = oneBitInMb.multiply(bitsPerSecond);
                return result.doubleValue();
            }
        });

        speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso");

        return null;
    }


    public interface AsyncResponse {
        void processFinish(double output);
    }
}
