package com.luxary_team.photogallery;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private boolean loading = true;
    public volatile int mPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();
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
                    updateItems();
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
            else if (s == null)
                s = "Nice picture";
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
        private String mQuery;

        public FetchItemsTask(Context c, String query) {
            this.c = c;
            mProgressDialog = new ProgressDialog(c);
            mQuery = query;
        }

        protected void onPreExecute() {
            this.mProgressDialog.setMessage("Loading");
            this.mProgressDialog.show();
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
//            String query = null;//"robot"; //todo for testing

            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos(mPage, mItems);
            } else {
                return new FlickrFetchr().searchPhotos(mPage, mQuery, mItems);
            }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView= (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit : " + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                mPage = 1;
                mItems.clear();
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            } });

        final MenuItem clearSearch = menu.findItem(R.id.menu_item_clear);
        clearSearch.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (!searchView.isIconified())
                    searchView.setIconified(true);

                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                mPage = 1;
                mItems.clear();
                updateItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(getActivity(), query).execute();
    }

}

