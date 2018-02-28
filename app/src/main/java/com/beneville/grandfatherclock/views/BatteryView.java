package com.beneville.grandfatherclock.views;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by joeja on 2/28/2018.
 */

public class BatteryView {

    private static final String ACTION_BATTERY_UPDATE = "UPDATE";
    private static final int REFRESH_TIMEOUT_SECONDS = 30;

    private BroadcastReceiver receiver;
    private TextView view;
    private Activity activity;

    public BatteryView(TextView v, Activity a) {
        view = v;
        activity = a;

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ACTION_BATTERY_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, intent, 0);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                getBatteryLevel();
            }
        };
        activity.registerReceiver(receiver, new IntentFilter(ACTION_BATTERY_UPDATE));

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, REFRESH_TIMEOUT_SECONDS * 1000, pendingIntent);

        getBatteryLevel();
    }

    private void getBatteryLevel() {
        Intent batteryStatus = activity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = Math.round((level * 100) / (float)scale);

        view.setText(batteryPct + "% Battery");
    }

}
