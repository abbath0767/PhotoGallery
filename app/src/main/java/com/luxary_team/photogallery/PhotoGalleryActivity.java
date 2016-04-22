package com.luxary_team.photogallery;

import android.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }

}
