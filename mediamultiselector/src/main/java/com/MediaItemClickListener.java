package com;

import com.droid.mediamultiselector.model.Media;

public interface MediaItemClickListener {

    /**
     * Callback when media item is clicked.
     * @param media The Media object that has been selected or unselected.
     * @param position The item index in the adapter data.
     */
    void onItemClick(Media media, int position);

    void onVideoItemPlay(Media media);
}
