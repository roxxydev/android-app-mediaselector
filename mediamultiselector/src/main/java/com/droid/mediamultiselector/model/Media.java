package com.droid.mediamultiselector.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Media implements Parcelable {

    public String path;
    public String name;
    public long dateCreated;

    public Media() {
    }

    public Media(String path, String name, long dateCreated) {
        this.path = path;
        this.name = name;
        this.dateCreated = dateCreated;
    }

    protected Media(Parcel in) {
        path = in.readString();
        name = in.readString();
        dateCreated = in.readLong();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            Media other = (Media) o;
            return TextUtils.equals(this.path, other.path);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(path);
        parcel.writeString(name);
        parcel.writeLong(dateCreated);
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };
}
