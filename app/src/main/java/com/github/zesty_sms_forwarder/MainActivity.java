package com.github.zesty_sms_forwarder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private EditText phoneNumberInput;
    private EditText SMTPServerInput;
    private EditText emailAddrInput;
    private EditText emailPwdInput;
    private Switch smsForwardingBool;
    private Switch emailForwardingBool;
    private Switch monthlyPingBool;
    private EditText focusGrabber;
    private String tempTargetPhone;

    public static final String RAN_BOOL = "ranBool";
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String STORED_PHONE_NUMBER = "storedPhoneNumber";
    public static final String STORED_SMTP_SERVER = "storedSMTPServer";
    public static final String STORED_EMAIL_ADDR = "storedEmailAddr";
    public static final String STORED_EMAIL_PWD = "storedEmailPwd";
    public static final String STORED_SMS_FORWARDING_BOOL = "storedSMSForwardingBool";
    public static final String STORED_EMAIL_FORWARDING_BOOL = "storedEmailForwardingBool";
    public static final String STORED_MONTHLY_PING_BOOL = "storedMonthlyPingBool";
    public static final String STORED_NEXT_ALARM_IN_MILI = "storedNextAlarmInMili";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[]{
                Manifest.permission.SEND_SMS,
        }, 0);

        // Disable battery optimization
        // startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName())));

        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MainActivity.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        phoneNumberInput = (EditText) findViewById(R.id.targetPhoneNumberInput);
        SMTPServerInput = (EditText) findViewById(R.id.smtp_prompt);
        emailAddrInput = (EditText) findViewById(R.id.email_addr);
        emailPwdInput = (EditText) findViewById(R.id.email_pwd);
        smsForwardingBool = (Switch) findViewById(R.id.sms_forwarding_bool);
        emailForwardingBool = (Switch) findViewById(R.id.email_forwarding_bool);
        monthlyPingBool = (Switch) findViewById(R.id.pinging_bool);

        focusGrabber = (EditText) findViewById(R.id.focus_grabber);
        focusGrabber.setBackgroundResource(android.R.color.transparent);

        tempTargetPhone = "";

        if (sharedPreferences.getBoolean(MainActivity.STORED_EMAIL_FORWARDING_BOOL, false) != true) {
            // Initially invisible until email is enabled
            SMTPServerInput.setVisibility(View.INVISIBLE);
            emailAddrInput.setVisibility(View.INVISIBLE);
            emailPwdInput.setVisibility(View.INVISIBLE);
        }

        // Initial run only
        if (sharedPreferences.getBoolean(MainActivity.RAN_BOOL, false) != true) {
            editor.putString(STORED_PHONE_NUMBER, "").apply();
            editor.putString(STORED_SMTP_SERVER, "smtp.gmail.com").apply();
            editor.putString(STORED_EMAIL_ADDR, "").apply();
            editor.putString(STORED_EMAIL_PWD, "").apply();
            editor.putBoolean(STORED_SMS_FORWARDING_BOOL, true).apply();
            editor.putBoolean(STORED_EMAIL_FORWARDING_BOOL, false).apply();
            editor.putBoolean(STORED_MONTHLY_PING_BOOL, false).apply();

            editor.putBoolean(RAN_BOOL, true).apply();
        }

        // Makes sure alarm is running if it ought to be
        if (sharedPreferences.getBoolean(MainActivity.STORED_MONTHLY_PING_BOOL, true) == true) {
            new CreateAlarm(getApplicationContext());
        }

        phoneNumberInput.setText(sharedPreferences.getString(STORED_PHONE_NUMBER, ""));
        SMTPServerInput.setText(sharedPreferences.getString(STORED_SMTP_SERVER, ""));
        emailAddrInput.setText(sharedPreferences.getString(STORED_EMAIL_ADDR, ""));
        emailPwdInput.setText(sharedPreferences.getString(STORED_EMAIL_PWD, ""));
        smsForwardingBool.setChecked(sharedPreferences.getBoolean(STORED_SMS_FORWARDING_BOOL, true));
        emailForwardingBool.setChecked(sharedPreferences.getBoolean(STORED_EMAIL_FORWARDING_BOOL, true));
        monthlyPingBool.setChecked(sharedPreferences.getBoolean(STORED_MONTHLY_PING_BOOL, true));

        phoneNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tempTargetPhone = s.toString();
            }
        });

        phoneNumberInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus && tempTargetPhone.length() > 0 && !tempTargetPhone.equals(sharedPreferences.getString(STORED_PHONE_NUMBER, ""))) {
                    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
                    boolean hasCountryCode = false;

                    try {
                        Phonenumber.PhoneNumber tempPhoneNumber = phoneNumberUtil.parse(tempTargetPhone, null);
                        if (phoneNumberUtil.getRegionCodeForNumber(tempPhoneNumber) != null) {
                            hasCountryCode = true;
                        }
                    } catch (Exception e) {
                        //
                    }

                    if (!hasCountryCode) {
                        try {
                            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            String CarrierRegion = telephonyManager.getNetworkCountryIso().toUpperCase(Locale.getDefault());
                            String countryCode = String.valueOf(phoneNumberUtil.getCountryCodeForRegion(CarrierRegion));
                            tempTargetPhone = "+" + countryCode + " " + tempTargetPhone;
                        } catch (Exception e) {
                            //
                        }
                    }
                    editor.putString(STORED_PHONE_NUMBER, tempTargetPhone).apply();
                    phoneNumberInput.setText(sharedPreferences.getString(STORED_PHONE_NUMBER, ""));
                }
            }
        });

        SMTPServerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString(STORED_SMTP_SERVER, editable.toString()).apply();
            }
        });

        emailAddrInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString(STORED_EMAIL_ADDR, editable.toString()).apply();
            }
        });

        emailPwdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString(STORED_EMAIL_PWD, editable.toString()).apply();
            }
        });

        smsForwardingBool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean(STORED_SMS_FORWARDING_BOOL, smsForwardingBool.isChecked()).apply();
            }
        });

        emailForwardingBool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean(STORED_EMAIL_FORWARDING_BOOL, emailForwardingBool.isChecked()).apply();
                // Toggle visibility
                if (emailForwardingBool.isChecked()) {
                    SMTPServerInput.setVisibility(View.VISIBLE);
                    emailAddrInput.setVisibility(View.VISIBLE);
                    emailPwdInput.setVisibility(View.VISIBLE);
                } else {
                    SMTPServerInput.setVisibility(View.INVISIBLE);
                    emailAddrInput.setVisibility(View.INVISIBLE);
                    emailPwdInput.setVisibility(View.INVISIBLE);
                }
            }
        });

        monthlyPingBool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean(STORED_MONTHLY_PING_BOOL, monthlyPingBool.isChecked()).apply();

                // Start first alarm in a minute
                long time_from_now_to_run_at = TimeUnit.MINUTES.toMillis(1);
                long time_to_run_at = Calendar.getInstance().getTimeInMillis() + time_from_now_to_run_at;
                editor.putLong(STORED_NEXT_ALARM_IN_MILI, time_to_run_at).apply();

                new CreateAlarm(getApplicationContext());
            }
        });
    }

    // Handles unfocusing edittext if click away
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    focusGrabber.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
