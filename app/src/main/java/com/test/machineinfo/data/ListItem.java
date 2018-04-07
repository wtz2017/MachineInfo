package com.test.machineinfo.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by WTZ on 2017/11/27.
 */

public class ListItem implements Parcelable {

    String name;
    String className;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ListItem(String name, String className) {
        this.name = name;
        this.className = className;
    }

    protected ListItem(Parcel in) {
        name = in.readString();
        className = in.readString();
    }

    public static final Creator<ListItem> CREATOR = new Creator<ListItem>() {
        @Override
        public ListItem createFromParcel(Parcel in) {
            return new ListItem(in);
        }

        @Override
        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(className);
    }
}
