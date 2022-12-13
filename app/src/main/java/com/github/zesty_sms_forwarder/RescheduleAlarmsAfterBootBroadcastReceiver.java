package com.github.zesty_sms_forwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RescheduleAlarmsAfterBootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new CreateAlarm(context);
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
