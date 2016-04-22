package com.luxary_team.photogallery;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        ActionBar actionBar = getSupportActionBar();

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();

        }
    }
}
