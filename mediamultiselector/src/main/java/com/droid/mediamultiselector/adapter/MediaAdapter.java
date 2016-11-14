package com.droid.mediamultiselector.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MediaItemClickListener;
import com.droid.mediamultiselector.R;
import com.droid.mediamultiselector.activity.MediaSelectorActivity;
import com.droid.mediamultiselector.adapter.viewholder.MediaViewHolder;
import com.droid.mediamultiselector.model.Media;

import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaViewHolder> {

    private static final String TAG = "MediaAdapter";

    private Activity activity;
    private int mediaType = MediaSelectorActivity.MEDIA_TYPE_IMAGE;
    private int selectionMode = MediaSelectorActivity.SELECTION_MODE_SINGLE;
    private int selectionLimit = MediaSelectorActivity.DEFAULT_SELECTION_LIMIT;
    private boolean isShowCamera = true;

    private MediaItemClickListener mediaItemClickListener;

    private ArrayList<Media> dataSet = new ArrayList<>();
    private ArrayList<Media> dataSetSelected = new ArrayList<>();

    public MediaAdapter(Activity activity, int mediaType, MediaItemClickListener mediaItemClickListener,
                        int selectionMode, int selectionLimit, boolean isShowCamera) {
        this.activity = activity;
        this.mediaType = mediaType;
        this.mediaItemClickListener = mediaItemClickListener;
        this.selectionMode = selectionMode;
        this.selectionLimit = selectionLimit;
        this.isShowCamera = isShowCamera;
    }

    /** Set the data to populate with the adapter. */
    public void setData(ArrayList<Media> dataSet) {
        dataSetSelected.clear();

        if (dataSet != null && dataSet.size() > 0) {
            this.dataSet = new ArrayList<>(dataSet);
        } else {
            this.dataSet.clear();
        }

        notifyDataSetChanged();
    }

    /** Set the previously selected media. */
    public void setSelectedData(ArrayList<Media> selectedDataSet) {
        this.dataSetSelected = new ArrayList<>(selectedDataSet);
        if (dataSetSelected != null && dataSetSelected.size() > 0) {
            notifyDataSetChanged();
        }
    }

    /** Select media in data of adapter. */
    public void selectData(Media media, int position) {
        if (dataSetSelected != null) {
            if (dataSetSelected.contains(media)) {
                dataSetSelected.remove(media);
            } else {
                dataSetSelected.add(media);
            }
        }
        notifyItemChanged(position);
    }

    /** Get the selected media */
    public ArrayList<Media> getSelectedData() {
        return this.dataSetSelected;
    }

    /** Get the media item click listener. */
    public MediaItemClickListener getMediaItemClickListener() {
        return mediaItemClickListener;
    }

    /** Return the adapter media type if image or video. */
    public int getMediaType() {
        return mediaType;
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_media, viewGroup, false);
        MediaViewHolder vh = new MediaViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        // Show camera view on first cell and only if set to be shown
        boolean isItemCameraButton = isShowCamera && position == 0;

        boolean isShowIndicator = selectionMode == MediaSelectorActivity.SELECTION_MODE_SINGLE ? false : true;
        boolean isSelected = isItemCameraButton == false && dataSetSelected.contains(dataSet.get(position -1));

        Media media = isItemCameraButton ? null: dataSet.get(position -1);

        holder.bindData(activity, this, position, media, selectionLimit, isItemCameraButton, isShowIndicator, isSelected);
    }

    @Override
    public int getItemCount() {
        int count = dataSet != null ? dataSet.size() : 0;
        if (isShowCamera) {
            return count + 1;// Add length of adapter items to show camera button
        }
        return count;
    }
}
