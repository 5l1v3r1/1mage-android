package com.jitsik.onemage;

import android.app.Fragment;
import android.os.Bundle;

import com.jitsik.onemageapi.Gallery;
import com.jitsik.onemageapi.LastGalleryHandler;

/**
 * Created by alex on 3/1/15.
 */
public class GalleryFragment extends Fragment implements LastGalleryHandler {

    private Gallery gallery = null;
    private String loadError = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        reload();
    }

    public Gallery getGallery() {
        return gallery;
    }

    public String getLoadError() {
        return loadError;
    }

    public void lastGalleryError(String e) {
        loadError = e;
        BrowseActivity activity = (BrowseActivity)getActivity();
        if (activity != null) {
            activity.errorLoading();
        }
    }

    public void lastGalleryFound(Gallery g) {
        g.reload();
        gallery = g;
        BrowseActivity activity = (BrowseActivity)getActivity();
        if (activity != null) {
            activity.doneLoading();
        }
    }

    public void reload() {
        loadError = null;
        gallery = null;
        Gallery.lastGallery(this);
    }

}
