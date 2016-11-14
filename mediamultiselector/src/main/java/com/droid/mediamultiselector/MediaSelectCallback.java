package com.droid.mediamultiselector;

import com.droid.mediamultiselector.model.Media;

import java.io.File;

public interface MediaSelectCallback {

    /** Callback when a media has been selected in single mode selection. */
    void onSingleMediaSelected(Media media);

    /** Callback when a media item is selected. */
    void onMediaItemSelected(Media mediaSelected);

    /** Callback when a media item is unselected. */
    void onMediaItemUnselected(Media mediaUnselected);

    /** Callback when a new media has been created by taking picture or taking video. */
    void onCameraTake(File mediaFile);
}
