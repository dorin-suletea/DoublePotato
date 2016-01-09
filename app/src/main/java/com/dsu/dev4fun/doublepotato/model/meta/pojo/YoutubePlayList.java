package com.dsu.dev4fun.doublepotato.model.meta.pojo;


import android.os.Parcel;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class YoutubePlayList extends YoutubeItem {
    public final static String PLAYLIST_EXTRA_KEY = "PLAYLIST";
    private List<YoutubeSong> songs;
    private List<YoutubeSong> shuffledList;

    private boolean isShuffled;


    public YoutubePlayList(String id, String name) {
        super(id, name);
        songs = new ArrayList<>();
        shuffledList = new ArrayList<>();
        isShuffled = false;
    }

    public YoutubePlayList(YoutubePlayList other) {
        super(other.getId(), other.getName());
        this.songs = new ArrayList<>(other.songs);
        this.shuffledList = new ArrayList<>(other.shuffledList);
        isShuffled = other.isShuffled();
    }

    public void shuffle() {
        isShuffled = true;

        long seed = System.nanoTime();
        Collections.shuffle(shuffledList, new Random(seed));
    }

    public void unShuffle() {
        isShuffled = false;
    }

    public boolean isShuffled() {
        return isShuffled;
    }

    public void moveSongToFront(String songId) {
        int index = 0;
        for (int i = 0; i < getSongs().size();i++) {
            if (getSongs().get(i).getId().equals(songId)) {
                index = i;
                break;
            }
        }
        YoutubeSong currentSong = getSongs().get(index);
        getSongs().remove(index);
        getSongs().add(0, currentSong);
    }

    public List<YoutubeSong> getSongs() {
        //keep the same list reference for adapters
        if (isShuffled) {
            return  shuffledList;
        } else {
            return songs;
        }
    }

    public List<YoutubeSong> getSongsWithError(){
        List<YoutubeSong> ret = new ArrayList<>();
        for (YoutubeSong song : songs){
            if (YoutubeError.NONE!=song.getError()){
                ret.add(song);
            }
        }
        return ret;
    }

    public YoutubeSong getSongByID(String id) {
        for (YoutubeSong song : songs) {
            if (id.equals(song.getId())) {
                return song;
            }
        }
        return null;
    }

    public void setSongs(List<YoutubeSong> newSongs) {
        songs.clear();
        this.songs.addAll(newSongs);

        shuffledList.clear();
        shuffledList.addAll(newSongs);
    }

    public int getDownloadedSongsCount() {
        //TODO this is just to costy, cache the downloaded song count and implement a song downloaded observer to update the playlist downloaded song count
        int ret = 0;
        for (YoutubeSong song : songs) {
            if (song.isSavedLocally()) {
                ret++;
            }
        }
        return ret;
    }

    public void importNewSongs(YoutubePlayList otherPlaylist) {
        List<YoutubeSong> songsToAdd = new ArrayList<>();
        for (YoutubeSong otherSong : otherPlaylist.getSongs()) {
            if (!this.songs.contains(otherSong)) {
                songsToAdd.add(otherSong);
            }
        }
        songs.addAll(songsToAdd);
        shuffledList.addAll(songsToAdd);
    }

    public List<YoutubeSong> getPlayableSongList() {
        List<YoutubeSong> ret = new ArrayList<>();
        for (YoutubeSong s : getSongs()) {
            if (s.getSongFileLocation() != null && !s.getSongFileLocation().isEmpty()) {
                ret.add(s);
            }
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof YoutubePlayList))
            return false;
        return super.equals(o);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(songs);
        dest.writeTypedList(shuffledList);
        dest.writeByte(isShuffled ? (byte) 1 : (byte) 0);
    }

    protected YoutubePlayList(Parcel in) {
        super(in);
        this.songs = in.createTypedArrayList(YoutubeSong.CREATOR);
        this.shuffledList = in.createTypedArrayList(YoutubeSong.CREATOR);
        this.isShuffled = in.readByte() != 0;
    }

    public static final Creator<YoutubePlayList> CREATOR = new Creator<YoutubePlayList>() {
        public YoutubePlayList createFromParcel(Parcel source) {
            return new YoutubePlayList(source);
        }

        public YoutubePlayList[] newArray(int size) {
            return new YoutubePlayList[size];
        }
    };
}
