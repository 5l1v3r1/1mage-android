package com.jitsik.onemageapi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A Gallery facilitates linear navigation through the list of images.
 *
 * Created by Alex Nichol on 3/1/15.
 */
public class Gallery {

    private class BitmapOrError {

        public Bitmap bitmap;
        public String error;

        BitmapOrError(Bitmap b) {
            this.bitmap = b;
            this.error = null;
        }

        BitmapOrError(String e) {
            this.bitmap = null;
            this.error = e;
        }

    }

    private class FetchImageTask extends AsyncTask<Integer, Object, BitmapOrError> {

        @Override
        protected BitmapOrError doInBackground(Integer... params) {
            String urlStr = "http://1mage.us/" + params[0];
            try {
                // Send the request.
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(new HttpGet(urlStr));
                if (isCancelled()) {
                    return null;
                }
                // Read the response body.
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                if (isCancelled()) {
                    return null;
                }
                // Parse the image.
                Bitmap b = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
                if (isCancelled()) {
                    return null;
                }
                if (b == null) {
                    return new BitmapOrError("Failed to load bitmap.");
                }
                return new BitmapOrError(b);
            } catch (Exception e) {
                return new BitmapOrError(e.getLocalizedMessage());
            }
        }

        @Override
        protected void onPostExecute(BitmapOrError res) {
            handleBitmapOrError(res);
        }

    }

    private static class LastOrError {

        public String error;
        public int last;

        LastOrError(String error) {
            this.error = error;
            this.last = -1;
        }

        LastOrError(int last) {
            this.error = null;
            this.last = last;
        }

    }

    private static class LastTask extends AsyncTask<Object, Object, LastOrError> {

        protected LastGalleryHandler handler;

        @Override
        protected LastOrError doInBackground(Object... params) {
            String urlStr = "http://1mage.us/last";
            try {
                // Get the redirect URL.
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setInstanceFollowRedirects(false);
                String redirect = conn.getHeaderField("Location");

                // Get the image ID from the redirect URL.
                int idx = redirect.lastIndexOf("/");
                if (idx < 0) {
                    return new LastOrError("Did not find '/'.");
                }
                String numStr = redirect.substring(idx + 1);
                int num = Integer.parseInt(numStr);
                return new LastOrError(num);
            } catch (Exception e) {
                return new LastOrError(e.getLocalizedMessage());
            }
        }

        @Override
        protected void onPostExecute(LastOrError le) {
            if (le.error != null) {
                handler.lastGalleryError(le.error);
            } else {
                handler.lastGalleryFound(new Gallery(le.last));
            }
        }

    }

    private class NextLast {

        public int next;
        public int last;
        public String error;

        NextLast(String err) {
            error = err;
            next = last = -1;
        }

        NextLast(int n, int l) {
            next = n;
            last = l;
            error = null;
        }

    }

    private class NextLastTask extends AsyncTask<Integer, Object, NextLast> {

        @Override
        protected NextLast doInBackground(Integer... params) {
            String urlStr = "http://1mage.us/nextlast/" + params[0];
            try {
                // Send the request.
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(new HttpGet(urlStr));
                if (isCancelled()) {
                    return null;
                }
                // Read the response body.
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                if (isCancelled()) {
                    return null;
                }
                // Parse the JSON.
                JSONObject object = new JSONObject(out.toString());
                int next = object.optInt("next", -1);
                int last = object.optInt("last", -1);
                return new NextLast(next, last);
            } catch (Exception e) {
                return new NextLast(e.getLocalizedMessage());
            }
        }

        @Override
        protected void onPostExecute(NextLast result) {
            handleNextLast(result);
        }

    }

    private FetchImageTask fetchImageTask = null;
    private GalleryHandler handler = null;
    private Bitmap currentImage = null;
    private NextLast nextLast = null;
    private NextLastTask nextLastTask = null;
    private int page = -1;

    Gallery(int start) {
        this.page = start;
    }

    public boolean canGoLast() {
        if (nextLast == null) {
            return false;
        }
        return nextLast.last >= 0;
    }

    public boolean canGoNext() {
        if (nextLast == null) {
            return false;
        }
        return nextLast.next >= 0;
    }

    public Bitmap getCurrentImage() {
        return currentImage;
    }

    public GalleryHandler getGalleryHandler() {
        return handler;
    }

    public int getPage() {
        return page;
    }

    public void goLast() {
        if (nextLastTask != null || fetchImageTask != null) {
            return;
        }
        if (!canGoLast()) {
            return;
        }
        page = nextLast.last;
        nextLast = null;
        currentImage = null;
        reload();
    }

    public void goNext() {
        if (nextLastTask != null || fetchImageTask != null) {
            return;
        }
        if (!canGoNext()) {
            return;
        }
        page = nextLast.next;
        nextLast = null;
        currentImage = null;
        reload();
    }

    private void handleBitmapOrError(BitmapOrError be) {
        fetchImageTask = null;
        if (be.error != null) {
            if (nextLastTask != null) {
                nextLastTask.cancel(true);
                nextLastTask = null;
            }
            if (getGalleryHandler() != null) {
                getGalleryHandler().loadFailed(be.error);
            }
        }
        currentImage = be.bitmap;
        if (nextLastTask != null) {
            return;
        }
        if (getGalleryHandler() != null) {
            getGalleryHandler().loadedImage();
        }
    }

    private void handleNextLast(NextLast nl) {
        nextLastTask = null;
        if (nl.error != null) {
            if (fetchImageTask != null) {
                fetchImageTask.cancel(true);
                fetchImageTask = null;
            }
            if (getGalleryHandler() != null) {
                getGalleryHandler().loadFailed(nl.error);
            }
        }
        nextLast = nl;
        if (fetchImageTask != null) {
            return;
        }
        if (getGalleryHandler() != null) {
            getGalleryHandler().loadedImage();
        }
    }

    public static void lastGallery(LastGalleryHandler h) {
        if (h == null) {
            throw new NullPointerException("LastGalleryHandler cannot be null.");
        }
        LastTask task = new LastTask();
        task.handler = h;
        task.execute();
    }

    public void reload() {
        if (fetchImageTask != null || nextLastTask != null) {
            return;
        }
        fetchImageTask = new FetchImageTask();
        fetchImageTask.execute(new Integer(page));
        nextLastTask = new NextLastTask();
        nextLastTask.execute(new Integer(page));
    }

    public void setGalleryHandler(GalleryHandler g) {
        handler = g;
    }

}
