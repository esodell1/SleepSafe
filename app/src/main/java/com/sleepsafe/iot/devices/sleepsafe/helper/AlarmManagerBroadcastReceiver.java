package com.sleepsafe.iot.devices.sleepsafe.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * This class implements the main receiver of the broadcast alarms that are trigger by the monitor
 * service MonitorSvc. It will alert a user of an alarming condition.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    final public static String ONE_TIME = "onetime";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "SleepSafe");
        //Acquire the lock
        wl.acquire();

        Toast.makeText(context, "Alarm Tripped.",
                Toast.LENGTH_LONG).show();
        // Vibrate the mobile phone
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        if (settings.getBoolean("notifications_alarm_vibrate", true)) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(2000);
        }

        //Release the lock
        wl.release();
    }

    public void setOnetimeTimer(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.TRUE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
    }
}