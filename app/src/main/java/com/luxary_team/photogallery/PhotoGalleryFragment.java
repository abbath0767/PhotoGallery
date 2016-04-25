package com.luxary_team.photogallery;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {
    public static final String TAG = "photoTag";

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    private boolean loading = true;
    public int mPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask(getActivity()).execute();

        mThumbnailDownloader = new ThumbnailDownloader<>();
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.d(TAG, "background process started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_photo_gallery_recyclerView);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
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
                    new FetchItemsTask(getActivity()).execute();
                }
            }
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mPhotoImageView;
        private TextView mTextView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mPhotoImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_imageView);
            mTextView = (TextView) itemView.findViewById(R.id.text_view);
        }

        public void bindGalleryItem(GalleryItem item) {
            String s = item.getCaption();
            if (s.length() > 20)
                s = s.substring(0, 20) + "...";
            mTextView.setText(s);
            Picasso.with(getActivity())
                    .load(item.getUrl())
                    .placeholder(R.drawable.loading)
                    .into(mPhotoImageView);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> items) {
            mGalleryItems = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
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

        private ProgressDialog mProgressDialog;
        private Context c;

        public FetchItemsTask(Context c) {
            this.c = c;
            mProgressDialog = new ProgressDialog(c);
        }

        protected void onPreExecute() {
            this.mProgressDialog.setMessage("Loading");
            this.mProgressDialog.show();
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetchr().fetchItems(mPage, mItems);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
//            mItems.addAll(galleryItems);
            setupAdapter();
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    public static PhotoGalleryFragment newInstance() {
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();

        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.d(TAG, "background process destroy");
    }
}

