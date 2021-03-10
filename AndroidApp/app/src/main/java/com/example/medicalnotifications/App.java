package com.example.medicalnotifications;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_BLOOD_OXYGEN = "Blood Oxygen Channel";
    public static final String CHANNEL_CONNECTIVITY = "Connectivity Channel";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channelBloodOxygen = new NotificationChannel(
                    CHANNEL_BLOOD_OXYGEN,
                    "Blood Oxygen Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelBloodOxygen.enableVibration(true);
            channelBloodOxygen.setDescription("Canal para notificaciones de oxigeno en sangre");
            AudioAttributes.Builder audioAttr=new AudioAttributes.Builder();
            audioAttr.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
            audioAttr.setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT);
            audioAttr.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED);
            AudioAttributes attr=audioAttr.build();

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            channelBloodOxygen.setSound(alarmSound,attr);
            NotificationChannel channelConnectionFailure = new NotificationChannel(
                    CHANNEL_CONNECTIVITY,
                    "Connection Failure",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelConnectionFailure.setDescription("Canal para notificaciones de conexión");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channelBloodOxygen);
            manager.createNotificationChannel(channelConnectionFailure);

        }
    }
}
