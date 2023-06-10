package com.github.zesty_sms_forwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.telephony.SmsManager;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AlarmReceiver extends BroadcastReceiver {
    static final SmsManager smsManager = SmsManager.getDefault();

    @Override
    // Implement onReceive() method
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        boolean monthlyPingBool = sharedPreferences.getBoolean(MainActivity.STORED_MONTHLY_PING_BOOL, true);

        if (monthlyPingBool) {
            final SharedPreferences.Editor editor = sharedPreferences.edit();

            long timeFromNowToRunAt = TimeUnit.DAYS.toMillis(30);
            long timeToRunAt = Calendar.getInstance().getTimeInMillis() + timeFromNowToRunAt;

            editor.putLong(MainActivity.STORED_NEXT_ALARM_IN_MILI, timeToRunAt).apply();
            new CreateAlarm(context);

            final String targetNumber = sharedPreferences.getString(MainActivity.STORED_PHONE_NUMBER, "");

            String pingPayload = "Hi, just wanted to let you know I'm still good: ";

            Random random = new Random();

            int sleepSeconds = random.nextInt(60);
            try {
                Thread.sleep(sleepSeconds * 1000);
            } catch (InterruptedException e) {
                //
            }

            // The number after the "+" is the minimum bound for numChars
            int numChars = random.nextInt(6) + 2;
            String alphabet = "abcdefghijklmnopqrstuvwxyz1234567890";
            for (int i = 0; i < numChars; i++) {
                pingPayload += alphabet.charAt(random.nextInt(alphabet.length()));
            }

            if (targetNumber.length() > 0) {
                try {
                    smsManager.sendTextMessage(targetNumber, null, pingPayload, null, null);
                } catch (Exception e) {
                    //
                }
            }
        }
    }
}
