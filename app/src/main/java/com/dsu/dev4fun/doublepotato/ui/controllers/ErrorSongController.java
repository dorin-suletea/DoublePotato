package com.dsu.dev4fun.doublepotato.ui.controllers;

import android.app.Activity;

import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.ui.adapters.SongListsAdapter;

public class ErrorSongController {
    private Activity context;
    private SongListsAdapter listViewAdapter;

    /**
     * Info about current playlist. This is a local clone, changes done to this playlist (eg shuffling) are only reflected in controller's context
     */
    private YoutubePlayList playList;

    public ErrorSongController(Activity context, YoutubePlayList playlist) {
        this.context = context;
        this.playList = playlist;
    }

    public YoutubePlayList getPlaylist() {
        return playList;
    }
}
