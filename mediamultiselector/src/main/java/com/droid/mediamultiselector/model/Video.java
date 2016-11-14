package com.droid.mediamultiselector.model;

import android.graphics.Bitmap;
import android.os.Parcelable;

public class Video extends Media implements Parcelable{

    public Bitmap thumbnail;

    public Video(String path, String name, long dateCreated){
        this.path = path;
        this.name = name;
        this.dateCreated = dateCreated;
    }

    public Video(String path, String name, long dateCreated, Bitmap thumbnail){
        this.path = path;
        this.name = name;
        this.dateCreated = dateCreated;
        this.thumbnail = thumbnail;
    }
}
