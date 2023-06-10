package com.github.zesty_sms_forwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

// Phone will only ever receive SMS, so no need to make MMS->SMS translation layer
public class SMSReceiver extends BroadcastReceiver {
    static final String SMS_RECEIVED_ACTION = android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION;
    static final SmsManager smsManager = SmsManager.getDefault();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (! intent.getAction().equals(SMS_RECEIVED_ACTION)) return;

        final Bundle bundle = intent.getExtras();
        final Object[] pduObjects = (Object[]) bundle.get("pdus");
        if (pduObjects == null) return;

        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        final String targetNumber = sharedPreferences.getString(MainActivity.STORED_PHONE_NUMBER, "");
        final String smtpServer = sharedPreferences.getString(MainActivity.STORED_SMTP_SERVER, "");
        final String emailAddr = sharedPreferences.getString(MainActivity.STORED_EMAIL_ADDR, "");
        final String emailPwd = sharedPreferences.getString(MainActivity.STORED_EMAIL_PWD, "");
        final boolean smsForwardingBool = sharedPreferences.getBoolean(MainActivity.STORED_SMS_FORWARDING_BOOL, true);
        final boolean emailForwardingBool = sharedPreferences.getBoolean(MainActivity.STORED_EMAIL_FORWARDING_BOOL, true);

        for (Object messageObj : pduObjects) {
            SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) messageObj, (String) bundle.get("format"));
            // Replace removes the "+" from the front of the sender number
            String senderNumber = currentMessage.getDisplayOriginatingAddress();
            String rawMessageContent = currentMessage.getDisplayMessageBody();

            /**
             * When receive a message from ordinary numbers,
             * forward the message to target number in this format:
             *      'from `senderNumber` :`messageContent`'
             *
             * when receive a message from the target number
             *      e.g. target phone replying the message
             * the format should be:
             *      'to `toNumber` :`messageContent`'
             * then forward the message to 'toNumber'
             */
            String forwardNumber = null;
            String forwardPrefix = null;
            String forwardContent = null;
            if (senderNumber.replaceAll("[^\\d.]", "").equals(targetNumber.replaceAll("[^\\d.]", ""))) {
                // Checking if follows the correct form
                if (rawMessageContent.substring(0, 2).equals("to")) {
                    int divindex = rawMessageContent.indexOf(':');
                    // World's shortest phone number ends in at least 4 digits
                    if (divindex - 4 > 3 && rawMessageContent.substring(divindex - 4, divindex).matches("[0-9]+")) {
                        // Tt'a message that needs to be forwarded
                        forwardNumber = rawMessageContent.substring(3, divindex).replaceAll("[^\\d.]", "");
                        forwardPrefix = "";
                        forwardContent = rawMessageContent.substring(divindex + 2, rawMessageContent.length());
                    }
                }
            } else {
                // it's a normal message, need to be forwarded
                if (smsForwardingBool) {
                    forwardNumber = targetNumber;
                    forwardPrefix = "from " + senderNumber + ":\n";
                    forwardContent = rawMessageContent;
                }

                if (emailForwardingBool && smtpServer.length() > 0 && emailAddr.length() > 0
                && emailPwd.length() > 0) {
                    new CreateEmail(smtpServer, emailAddr, emailPwd, emailAddr,
                            "Forwarded SMS from " + senderNumber, rawMessageContent);
                }
            }

            if (forwardNumber != null && forwardNumber.length() > 0 && forwardContent != null) {
                try {
                    if ((forwardPrefix + forwardContent).getBytes().length > 120) {
                        // there is a length limit in SMS, if the message length exceeds it, separate the meta data and content
                        smsManager.sendTextMessage(forwardNumber, null, forwardPrefix, null, null);
                        smsManager.sendTextMessage(forwardNumber, null, forwardContent, null, null);
                    } else {
                        // if it's not too long, combine meta data and content to save money
                        smsManager.sendTextMessage(forwardNumber, null, forwardPrefix + forwardContent, null, null);
                    }
                } catch (Exception e) {
                    //
                }
            }
        }
    }
}
