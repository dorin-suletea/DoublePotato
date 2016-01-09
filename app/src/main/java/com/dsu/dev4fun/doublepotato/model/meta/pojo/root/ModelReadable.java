package com.dsu.dev4fun.doublepotato.model.meta.pojo.root;

import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;

import java.util.List;


public interface ModelReadable {
    public YoutubePlayList getPlaylistByID(String searchedID);
    public List<YoutubePlayList> getPlayLists();
}
