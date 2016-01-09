package com.dsu.dev4fun.doublepotato.model.meta.pojo.root;


import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LocalModelRoot implements ModelReadable, ModelWritable {
    protected List<YoutubePlayList> playLists;


    private static LocalModelRoot instance = new LocalModelRoot();

    private LocalModelRoot() {
        playLists = new ArrayList<>();
    }

    public static ModelWritable getWriteInstance() {
        return instance;
    }

    public static ModelReadable getReadInstance() {
        return instance;
    }

    public YoutubePlayList getPlaylistByID(String searchedID) {
        for (YoutubePlayList dbPlaylist : playLists) {
            if (dbPlaylist.getId().equals(searchedID)) {
                return dbPlaylist;
            }
        }
        return null;
    }

    public List<YoutubePlayList> getPlayLists() {
        return Collections.unmodifiableList(playLists);
    }

    @Override
    public void importRemotePlaylists(List<YoutubePlayList> remotePlaylists) {
        /**Import from remote model all that is not already in local model**/
        for (YoutubePlayList playlist : remotePlaylists) {
            if (!playLists.contains(playlist)) {
                playLists.add(playlist);
            } else {
                //the youtube playlist is in the db , find that object
                YoutubePlayList localPlaylist = getPlaylistByID(playlist.getId());
                localPlaylist.importNewSongs(playlist);
            }
        }
    }

    public void initialize(List<YoutubePlayList> playListList) {
        this.playLists.clear();
        this.playLists.addAll(playListList);
    }

}
