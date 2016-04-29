package com.luxary_team.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {
    public static final String TAG = "BroadcastReciver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(PhotoGalleryFragment.TAG, "received broadcast activity = " + intent.getAction());

        boolean isOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);
    }
}
