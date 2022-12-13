package com.github.zesty_sms_forwarder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class CreateAlarm {
    public CreateAlarm(Context context) {
        // Grabbing the appropriate alarm time
        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        long currTime = Calendar.getInstance().getTimeInMillis();
        long timeToRunAt = sharedPreferences.getLong(MainActivity.STORED_NEXT_ALARM_IN_MILI, currTime);

        if (currTime > timeToRunAt) {
            long timeFromNowToRunAt = TimeUnit.MINUTES.toMillis(1);
            timeToRunAt = currTime + timeFromNowToRunAt;
        }

        // Setting up the alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        // We call broadcast using pendingIntent, request code should be unique within this project
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // Scheduling the next alarm
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeToRunAt, pendingIntent);

        // Alternative scheduling format for debug
        // PendingIntent nullIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
        // AlarmManager.AlarmClockInfo alarm = new AlarmManager.AlarmClockInfo(timeToRunAt, nullIntent);
        // alarmManager.setAlarmClock(alarm, pendingIntent);
    }
}
