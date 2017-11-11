package com.dsu.dev4fun.doublepotato.model.meta.pojo;


import android.os.Parcel;
import android.os.Parcelable;

public class YoutubeItem implements Parcelable {
    protected final String name;
    protected final String id;

    protected YoutubeItem(String id, String name) {
        this.name = name;
        this.id = id;
    }

    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof YoutubeItem)) {
            return false;
        }
        return id.equals(((YoutubeItem) o).getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.id);
    }

    protected YoutubeItem(Parcel in) {
        this.name = in.readString();
        this.id = in.readString();
    }

}
