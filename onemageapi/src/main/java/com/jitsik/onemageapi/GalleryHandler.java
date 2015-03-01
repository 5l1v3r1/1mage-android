package com.jitsik.onemageapi;

/**
 * A GalleryHandler handles events from the gallery.
 *
 * Created by Alex Nichol on 3/1/15.
 */
public interface GalleryHandler {

    public void loadFailed(String errorMessage);
    public void loadedImage();

}
