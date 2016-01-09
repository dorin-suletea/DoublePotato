package com.dsu.dev4fun.doublepotato.model.meta.pojo.root;

import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;

import java.util.List;

public interface ModelWritable {
    public void initialize(List<YoutubePlayList> playListList);
    public void importRemotePlaylists(List<YoutubePlayList> remotePlaylists);

}
