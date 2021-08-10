package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import static com.example.myapplication.NotificationMessage.CHANNEl_1_ID;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    MQTTHelper mqttHelper;
    GoogleMap map;
    TextView door;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        door = findViewById(R.id.door);
        SupportMapFragment mapFragment;
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mqttHelper = new MQTTHelper(getApplicationContext(), map, door);
    }
}


//public class MainActivity extends AppCompatActivity {
//    MQTTHelper mqttHelper;
//    NotificationManagerCompat notificationManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//    }
//
//    public void sendMessage(View v) {
//        String title = "Vị trí Khoa thay đổi";
//        String message = "Khoa đã thay đổi vị trí";
//
//        Notification notification = new NotificationCompat
//                .Builder(this, CHANNEl_1_ID)
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
//                        R.drawable.ic_baseline_alarm_24))
//                .setSmallIcon(R.drawable.ic_baseline_add_location_24)
//                .setContentText(message)
//                .setContentTitle(title)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                .setAutoCancel(true)
//                .build();
//
//        notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.notify(1, notification);
//    }
//
//}