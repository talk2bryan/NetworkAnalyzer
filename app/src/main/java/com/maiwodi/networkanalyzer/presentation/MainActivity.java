package com.maiwodi.networkanalyzer.presentation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maiwodi.networkanalyzer.R;
import com.maiwodi.networkanalyzer.business.ConnectivitySensor;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ConnectivitySensor connectivitySensor;
    private Context context;
    private int currentBattteryPercentage = 0;
    private ProgressBar progressBar;
    private TextView batteryPercentageTextView;
    private TextView wifiConnectionStatusTextView;
    private Chronometer chronometer;
    private long stopChronometerOffset;
    private boolean chronometerRunning;
    private ScheduledFuture<?> scheduledFuture;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            float batteryPercentage = batteryLevel / (float) batteryScale;
            currentBattteryPercentage = (int) (batteryPercentage * 100);
            batteryPercentageTextView.setText(String.format(Locale.getDefault(), "%d%%",
                    currentBattteryPercentage));
            progressBar.setProgress(currentBattteryPercentage);
        }
    };


    public void startTimer(View v) {
        if (!chronometerRunning) {
            chronometer.setBase(SystemClock.elapsedRealtime() - stopChronometerOffset);
            chronometer.start();
            chronometerRunning = true;
            
            setWifiConnectionStatusText();

            Runnable periodicRecording = new Runnable() {
                @Override
                public void run() {
                    if (connectivitySensor.hasInternetConnection(context)) {
                        connectivitySensor.recordWifiSignalStrength(context);
                    } else {
                        Log.d("MainActivity", "startTimer() - Not connected to WiFi");
                    }
                }
            };

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            // To avoid memory leak...
            ScheduledThreadPoolExecutor ex = (ScheduledThreadPoolExecutor) executor;
            ex.setRemoveOnCancelPolicy(true);
            scheduledFuture = executor.scheduleAtFixedRate(
                    periodicRecording, 0, 2, TimeUnit.SECONDS);
        }
    }

    private void setWifiConnectionStatusText() {
        if (connectivitySensor.hasInternetConnection(context)) {
            wifiConnectionStatusTextView.setText("Connected to Internet");
            wifiConnectionStatusTextView.setBackgroundColor(0xFF7CCC26); // Green.
        } else {
            wifiConnectionStatusTextView.setText("Not Connected to Internet");
            wifiConnectionStatusTextView.setBackgroundColor(0xFFFF0000); // Red.
            Log.d("MainActivity", "startTimer() - Not connected to WiFi");
        }
    }

    public void stopTimer(View v) {
        if (chronometerRunning) {
            chronometer.stop();
            stopChronometerOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            chronometerRunning = false;
            scheduledFuture.cancel(false);

            connectivitySensor.sendDataToCloud();
            Log.d("MainActivity", "stopTimer()");
        }

    }

    public void resetTimerAndSendDataToCloud(View v) {
        chronometer.setBase(SystemClock.elapsedRealtime());
        stopChronometerOffset = 0;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(broadcastReceiver, intentFilter);

        batteryPercentageTextView = findViewById(R.id.tv_percentage);
        wifiConnectionStatusTextView = findViewById(R.id.wifiIsConnected);
        progressBar = findViewById(R.id.pb);


        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("Time: %s");
        chronometer.setBase(SystemClock.elapsedRealtime());


        connectivitySensor = new ConnectivitySensor();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}