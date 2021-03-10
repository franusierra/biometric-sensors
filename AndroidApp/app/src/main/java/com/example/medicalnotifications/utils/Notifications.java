package com.example.medicalnotifications.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.preference.PreferenceManager;

import com.example.medicalnotifications.App;
import com.example.medicalnotifications.R;
import com.example.medicalnotifications.database.Alarm;

import java.util.Calendar;

public class Notifications {
    public static void alarmNotification(Context context, Alarm alarm, Intent intent, int iconResource) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int messageID= preferences.getInt("alarm_notification_id",0);
        //Get the notification manage which we will use to display the notification
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
        Calendar.getInstance().getTime().toString();
        long when = System.currentTimeMillis();

        //build the pending intent that will start the appropriate activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, intent, 0);

        //build the notification
        Notification.Builder notificationCompat = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationCompat = new Notification.Builder(context, App.CHANNEL_BLOOD_OXYGEN);
        }else{
            notificationCompat= new Notification.Builder(context);
        }
        notificationCompat.setAutoCancel(true)
        .setContentTitle(alarm.getAlarm_typeEnum().name()+" - "+alarm.getPatient_id())
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconResource))
                .setSmallIcon(iconResource)
        .setContentIntent(pendingIntent)
        .setContentText("El sensor ha medido un valor de "+alarm.getMeasured_value());

        //.setWhen(when)
        Notification notification = notificationCompat.build();
        //display the notification
        mNotificationManager.notify(messageID, notification);
        // Update preferences
        SharedPreferences.Editor preferenceEditor=preferences.edit();
        preferenceEditor.putInt("alarm_notification_id",messageID+1);
        preferenceEditor.apply();
    }
    public static void noWifiNotification(Context context) {
        Notification.Builder notificationCompat = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationCompat = new Notification.Builder(context, App.CHANNEL_CONNECTIVITY);
        }else{
            notificationCompat= new Notification.Builder(context);
        }
        notificationCompat.setAutoCancel(true)
                .setOngoing(true)
                .setContentTitle("Fallo en la conexión")
                //.setContentIntent(pendingIntent)
                .setContentText("Debe estar conectado a la red local wifi")
                .setSmallIcon(R.mipmap.ic_launcher);

        //.setWhen(when)
        Notification notification = notificationCompat.build();
        //display the notification
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(ns);
        notificationManager.notify(0, notification);
    }
}
