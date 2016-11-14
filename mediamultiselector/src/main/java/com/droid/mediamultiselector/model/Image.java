package com.droid.mediamultiselector.model;

import android.os.Parcelable;

public class Image extends Media implements Parcelable {

    public Image(String path, String name, long dateCreated){
        super();
        this.path = path;
        this.name = name;
        this.dateCreated = dateCreated;
    }
}
