package com.example.mqtt_sender;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    Button closeBtn;
    Button openBtn;
    Integer btn_state = 0;
    String currentTime;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
    Integer countSendLocation = 30;
    private GpsTracker gpsTracker;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView currentTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mqttHelper = new MQTTHelper(getApplicationContext());
        latitudeTextView = findViewById(R.id.latitudeTextView);
        longitudeTextView = findViewById(R.id.longitudeTextView);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);

        initiateButton();
        createTimerUpdateTime();

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initiateButton() {
        closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHelper.sendDataMQTT(Integer.toString(0));
            }
        });

        openBtn = findViewById(R.id.openBtn);
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHelper.sendDataMQTT(Integer.toString(1));
            }
        });
    }

    public void createTimerUpdateTime() {
        final Runnable myRunnable = new Runnable() {
            public void run() {
                LocalDateTime time = LocalDateTime.now();
                currentTime = String.valueOf(dtf.format(time));
                currentTimeTextView.setText(currentTime);
                countSendLocation--;
                if (countSendLocation == 0) {
                    countSendLocation = 30;
                    setLocation();
                }
            }
        };
        final Handler myHandler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                myHandler.post(myRunnable);
            }
        };
        timer.schedule(task,1000, 1000);
    }

    public void setLocation(){
        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            double latitude = (double)((long)(gpsTracker.getLatitude() * 1000000000))/1000000000;
            double longitude = (double)((long)(gpsTracker.getLongitude() * 1000000000))/1000000000;
            latitudeTextView.setText(String.valueOf(latitude));
            longitudeTextView.setText(String.valueOf(longitude));

            JSONObject jsonString = new JSONObject();
            try {
                jsonString.put("lon", String.valueOf(longitude));
                jsonString.put("lat", String.valueOf(latitude));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String data = jsonString.toString();
            mqttHelper.sendDataMQTT(data);
        }else{
            gpsTracker.showSettingsAlert();
        }
    }
}