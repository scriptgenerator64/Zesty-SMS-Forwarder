
# SMS Forwarder
Anyone who has tried transitioning all of their telecommunications over to Voice Over IP (VOIP) knows that there are a few companies that refuse to accept their phone VOIP number as an SMS verification method.

For that reason, it is mandatory to always keep a legacy SMS line active; this Android project will allow you to forward any received SMS messages to an email or phone number of your choice and has numerous other quality-of-life features.

## Usage
Simply install this app onto a secondary Android phone (with the SMS SIM card in it) and leave it plugged in and powered on. Here are the app's features :

**1. Receive Messages**
When a message is received, it will be redirected to the target phone. Message body will have `from {source number}:` (and a line break) prepended, message content starts at the second line.

*Alternatively*, you can also specify an email SMTP server with login credentials and have the SMS forwarded to you via email instead of or along with SMS.

**2. Send Messages**
You can control the Android phone to send messages by sending a message to the Android phone from the target phone you specified earlier. Such messages should start with `to {receive number}:` (and a line break) followed by the message content to be forwarded (only the 2nd line onwards will be forwarded).

**3. Keep-Alive Monthly Ping**
You can toggle the "Monthly Ping" switch to make the app send a randomized greeting to your target number each month (the first message will be sent 1 minute after the toggle is first enabled, future messages will come in 30 day intervals). This feature exists because some carriers, such as 7-Eleven Speakout (which this app was tested on), require monthly activity to keep the line active.

## Setup
1. Pick a cheap Android phone and install this app onto it using the `.apk` file downloaded from the [releases page](https://github.com/scriptgenerator64/personal_sms_forwarder/releases). You can also build the app from source to ensure safety.
2. Launch the app and accept the SMS receive and send permission prompt. The app won't work without this.
3. Enter the target phone number.
4. Enable email forwarding and enter credentials if you so choose to (nothing leaves the app, your data is safe!)
5. If using a budget SIM provider, you may be required to use the phone at least once a month to keep the line active. The monthly ping feature will send a randomized keep-alive message to your target number once a month (first message will come in a minute after it is toggled on, will consume a bit more battery).
6. Put down the Android phone and keep it charged for continuous service. NOTE: If you reboot the phone, unlock it and wait until the app auto-launches itself to ensure no disruption of service.
7. Certain lower-powered Android phone will kill any backgrounded apps. If this is the case with your phone, do not remove this app from recent apps (keep it running in the foreground).

## Compatibility
This app has only been tested on Android 8 Go Edition, but it should work for Android 5.0-12.x devices. At the moment, the app only supports single-SIM devices, but messages received on a multi-SIM device should still be forwarded to the target number just fine.
