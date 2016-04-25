package com.luxary_team.photogallery;

import android.os.HandlerThread;
import android.util.Log;

public class ThumbnailDownloader<T> extends HandlerThread {
    public static final String TAG = "ThumbnailDownloader";

    public ThumbnailDownloader() {
        super(TAG);
    }

    public void queueThumbnail(T target, String url) {
        Log.d(PhotoGalleryFragment.TAG, "got a URL = " + url);
    }
}