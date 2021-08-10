package com.example.myapplication;

import android.app.Notification;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.nio.charset.Charset;

public class MQTTHelper {
    final String serverUri = "tcp://io.adafruit.com:1883";
    final String clientId = "sender2";
    final String subscriptionTopic = "truongkhoa1799/feeds/iot";
    final String username = "truongkhoa1799";
    final String password = "aio_rxRZ343j5sDwEyW2isAg2tCnlLv2";
    private MqttAndroidClient mqttAndroidClient;
    private GoogleMap googleMap;
    NotificationManagerCompat notificationManager;
    Context mainContext;

    TextView doorTv;

    public MQTTHelper(Context context, GoogleMap map, TextView door){
        doorTv = door;
        googleMap = map;
        mainContext = context;
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        this.setCallback();
        connect();
    }

    public void setCallback() {
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                try {
                    Log.i("test", mqttMessage.toString());
                    if (mqttMessage.toString().equals("1") || mqttMessage.toString().equals("0")) {
                        Integer status = Integer.parseInt(mqttMessage.toString());
                        notifyMessage(status);
                    } else {
                        JSONObject object = new JSONObject(mqttMessage.toString());
                        Log.i("test", mqttMessage.toString());
                        Log.w("Mqtt-arrive", mqttMessage.toString());

                        if (object.has("lat") && object.has("lon")) {
                            Double latitude = object.getDouble("lat");
                            Double longitude = object.getDouble("lon");
                            LatLng position = new LatLng(latitude, longitude);

                            googleMap.clear();
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
                            googleMap.animateCamera(CameraUpdateFactory.zoomBy(100));
                            googleMap.addMarker(new MarkerOptions().position(position).title("Khoa's Position"));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                        }
                    }

                } catch (Exception e) {
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    public void notifyMessage(int state) {
        String title = "Tình Trạng Cửa";
        String message = "";
        if (state == 0) {
            doorTv.setBackgroundColor(Color.parseColor("#FF9800"));
            message = "Khoa đã đóng cửa";
        } else {
            doorTv.setBackgroundColor(Color.parseColor("#8BC34A"));
            message = "Khoa đã mở cửa";
        }

        Notification notification = new NotificationCompat
                .Builder(mainContext, NotificationMessage.CHANNEl_1_ID)
                .setLargeIcon(BitmapFactory.decodeResource(mainContext.getResources(),
                        R.drawable.ic_baseline_alarm_24))
                .setSmallIcon(R.drawable.ic_baseline_add_location_24)
                .setContentText(message)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build();

        notificationManager = NotificationManagerCompat.from(mainContext);
        notificationManager.notify(1, notification);
    }

    private void connect(){
        Log.i("MQTT", "Start to connect MQTT Server");
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exceptions subscribing");
            ex.printStackTrace();
        }
    }
}
