package com.droid.mediamultiselector.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.droid.mediamultiselector.activity.MediaSelectorActivity;
import com.droid.mediamultiselector.fragment.ImageSelectorFragment;
import com.droid.mediamultiselector.fragment.VideoSelectorFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private int mediaTypeSelection;

    private ImageSelectorFragment imgFgmt;
    private VideoSelectorFragment vidFgmt;

    public PagerAdapter(FragmentManager fm, int mediaTypeSelection) {
        super(fm);
        this.mediaTypeSelection = mediaTypeSelection;
    }

    /** Set the instance of ImageSelectorFragment to use. */
    public void setImageFragment(ImageSelectorFragment fgmt) {
        this.imgFgmt = fgmt;
    }

    /** Set the instance of VideoSelectorFragment to use. */
    public void setVideoFragment(VideoSelectorFragment fgmt) {
        this.vidFgmt = fgmt;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return imgFgmt;
        } else if (position == 1) {
            return vidFgmt;
        }
        return null;
    }

    @Override
    public int getCount() {
        if (mediaTypeSelection == MediaSelectorActivity.MEDIA_TYPE_ALL) {
            return 2;// return tab length of 2 for Photo and Video
        }
        return 1;// Default tab length of 1, only tab is Photo
    }
}
