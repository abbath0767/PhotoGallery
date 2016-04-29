package com.luxary_team.photogallery;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class PhotoPageActivity extends SingleFragmentActivity {

    Callbacks mCallbacks;

    interface Callbacks {
        boolean goBack();
    }

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        Fragment fr = PhotoPageFragment.newInstance(getIntent().getData());
        mCallbacks = (Callbacks) fr;
        return fr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (mCallbacks.goBack())
            super.onBackPressed();
    }
}
