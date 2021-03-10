package com.example.medicalnotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartServiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context.getApplicationContext(),MQTTService.class));
    }
}
