package com.luxary_team.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FlickrFetchr {

    public static final String API_KEY = "5a1d09ae4ba1463fb0379b9c886fa8e3";

    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0)
                out.write(buffer, 0, bytesRead);
            out.close();

            return out.toByteArray();

        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec)throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(int page, List<GalleryItem> items) {

        if (items != null)
            Log.d(PhotoGalleryFragment.TAG, "length before = " + items.size());

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter("page", String.valueOf(page))
                    .build().toString();

            String jsonString = getUrlString(url);
            Log.d(PhotoGalleryFragment.TAG, "Recived json " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);

            parseItems(items, jsonBody);

        } catch (IOException e) {
            Log.d(PhotoGalleryFragment.TAG, "failed to fetch items, " + e);
        } catch (JSONException e) {
            Log.d(PhotoGalleryFragment.TAG, "failed to parse json, " + e);
        }

        Log.d(PhotoGalleryFragment.TAG, "length after = " + items.size());

        return items;
    }

    private void parseItems(List<GalleryItem> items,JSONObject jsonBody)
            throws IOException, JSONException {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = gson.fromJson(photoJsonObject.toString(), GalleryItem.class);

            items.add(item);
        }
    }
}
