package com.luxary_team.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PollService extends IntentService {
    public static final String TAG = "PollService";
    public static final int POLL_ITERVAL = 1000 * 60; // one min

    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn)
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_ITERVAL, pi);
        else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlaramOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResult(this);
        List<GalleryItem> items = new ArrayList<>();

        if (query == null)
            items = new FlickrFetchr().fetchRecentPhotos(1, items);
        else
            items = new FlickrFetchr().searchPhotos(1, query, items);

        if (items.size() == 0)
            return;

        String resultId = items.get(0).getId();
        if (resultId.equals(lastResultId))
            Log.d(PhotoGalleryFragment.TAG, "Got on a old result + " + resultId);
        else
            Log.d(PhotoGalleryFragment.TAG, "Got on a new result + " + resultId);


        QueryPreferences.setLastResult(this, resultId);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
