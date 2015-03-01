package com.jitsik.onemage;

import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.jitsik.onemageapi.GalleryHandler;


public class BrowseActivity extends Activity implements GalleryHandler {

    private static final String TAG_GALLERY_FRAGMENT = "gallery_fragment";
    private GalleryFragment gallery;
    private ImageView imageView;

    public void doneLoading() {
        setContentView(R.layout.activity_browse);
        imageView = (ImageView) findViewById(R.id.current_image);
        gallery.getGallery().setGalleryHandler(this);
        showImage();
    }

    public void errorLoading() {
        setContentView(R.layout.browse_error);
        // TODO: set error message here
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the gallery fragment.
        FragmentManager fm = getFragmentManager();
        gallery = (GalleryFragment)fm.findFragmentByTag(TAG_GALLERY_FRAGMENT);
        if (gallery == null) {
            gallery = new GalleryFragment();
            fm.beginTransaction().add(gallery, TAG_GALLERY_FRAGMENT).commit();
        }

        // Create the content view.
        if (gallery.getGallery() != null) {
            doneLoading();
        } else if (gallery.getLoadError() != null) {
            errorLoading();
        } else {
            setContentView(R.layout.browse_loading);
        }
    }

    public void retryLoad(View view) {
        gallery.reload();
        setContentView(R.layout.browse_loading);
    }

    public void showImage() {
        Bitmap bm = gallery.getGallery().getCurrentImage();
        if (bm != null) {
            imageView.setImageBitmap(bm);
        }
    }

    @Override
    public void loadFailed(String errorMessage) {
        retryLoad(null);
    }

    @Override
    public void loadedImage() {
        showImage();
    }

    public void lastPage(View view) {
        if (gallery.getGallery() != null) {
            gallery.getGallery().goLast();
        }
    }

    public void nextPage(View view) {
        if (gallery.getGallery() != null) {
            gallery.getGallery().goNext();
        }
    }
}
