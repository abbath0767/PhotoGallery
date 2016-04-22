package com.luxary_team.photogallery;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {
    public static final String TAG = "photoTag";

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();

    private boolean loading = true;
    public int mPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_photo_gallery_recyclerView);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.addOnScrollListener(new PhotoScrollListener());

        setupAdapter();

        return rootView;
    }

    private void setupAdapter() {
        if (isAdded()) {
            if (mPhotoRecyclerView.getAdapter() == null)
                mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
            else
                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private class PhotoScrollListener extends RecyclerView.OnScrollListener {

        private int pastVisiblesItems, visibleItemCount, totalItemCount;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > 0) {
                visibleItemCount = mPhotoRecyclerView.getLayoutManager().getChildCount();
                totalItemCount = mPhotoRecyclerView.getLayoutManager().getItemCount();
                pastVisiblesItems = ((GridLayoutManager) mPhotoRecyclerView.getLayoutManager()).
                        findFirstVisibleItemPosition();

                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;
                        Log.e(TAG, "LAST ITEM");
                    }
                }
                if (!loading) {
                    loading = true;
                    mPage++;
                    new FetchItemsTask().execute();
                }
            }
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem item) {
            mTitleTextView.setText(item.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> items) {
            mGalleryItems = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem item = mGalleryItems.get(position);
            holder.bindGalleryItem(item);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetchr().fetchItems(mPage, mItems);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
//            mItems.addAll(galleryItems);
            setupAdapter();
        }
    }

    public static PhotoGalleryFragment newInstance() {
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();

        return fragment;
    }
}
