package com.dsu.dev4fun.doublepotato.model.meta.pojo;

import android.os.Parcel;

public class YoutubeSong extends YoutubeItem {
    private String thumbnailUrl;
    private String songFileLocation;
    private String playlistID;
    private Long duration;
    private YoutubeError error;

    public YoutubeSong(String id, String playlistID, String name, String thumbnailUrl, Long duration) {
        super(id, name);
        this.thumbnailUrl = thumbnailUrl;
        this.playlistID = playlistID;
        this.duration=duration;
        this.error=YoutubeError.NONE;
    }

    public YoutubeSong(String id, String playlistID, String name, String thumbnailUrl, String songFileLocation, Long duration,YoutubeError error) {
        super(id, name);
        this.thumbnailUrl = thumbnailUrl;
        this.songFileLocation = songFileLocation;
        this.playlistID = playlistID;
        this.duration=duration;
        this.error = error;
    }

    public Long getDuration(){
        return duration;
    }

    public String getSongFileLocation() {
        return songFileLocation;
    }

    public boolean isSavedLocally() {
        return songFileLocation != null && !songFileLocation.isEmpty();
    }

    public void setError(YoutubeError error){
        this.error = error;
    }

    public void setSongFileLocation(String songFileLocation) {
        this.songFileLocation = songFileLocation;
    }

    public String getPlaylistID() {
        return playlistID;
    }

    public YoutubeError getError(){
        return error;
    }
    @Override
    public String toString() {
        return "Song : " + name + " " + id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof YoutubeSong)) {
            return false;
        }
        return (super.equals(o) && this.playlistID.equals(((YoutubeSong) o).getPlaylistID()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.thumbnailUrl);
        dest.writeString(this.songFileLocation);
        dest.writeLong(this.duration);
        dest.writeInt(this.getError().getErrorCode());
    }

    protected YoutubeSong(Parcel in) {
        super(in);
        this.thumbnailUrl = in.readString();
        this.songFileLocation = in.readString();
        this.duration = in.readLong();
        this.error=YoutubeError.getErrorByCode(in.readInt());
    }

    public static final Creator<YoutubeSong> CREATOR = new Creator<YoutubeSong>() {
        public YoutubeSong createFromParcel(Parcel source) {
            return new YoutubeSong(source);
        }

        public YoutubeSong[] newArray(int size) {
            return new YoutubeSong[size];
        }
    };
}
