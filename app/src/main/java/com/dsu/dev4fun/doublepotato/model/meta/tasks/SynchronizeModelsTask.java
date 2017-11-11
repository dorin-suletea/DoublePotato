package com.dsu.dev4fun.doublepotato.model.meta.tasks;


import android.os.AsyncTask;
import android.util.Log;

import com.dsu.dev4fun.doublepotato.model.meta.DatabaseLinker;
import com.dsu.dev4fun.doublepotato.model.meta.YoutubeLinker;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeSong;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.root.LocalModelRoot;
import com.dsu.dev4fun.doublepotato.model.util.ConnectionException;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SynchronizeModelsTask extends AsyncTask<Void, Void, Void> {
    private MethodWrapper<Void> postExecute;
    private MethodWrapper<Void> onFail;
    private YoutubeLinker youtubeLinker;
    private DatabaseLinker dbLinker;

    public SynchronizeModelsTask(YoutubeLinker youtubeLinker, DatabaseLinker dbLinker, MethodWrapper<Void> postExecute, MethodWrapper<Void> onFail) {
        super();
        this.youtubeLinker = youtubeLinker;
        this.dbLinker = dbLinker;
        this.postExecute = postExecute;
        this.onFail = onFail;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            List<YoutubePlayList> playlists = getRemoteModel();
            importToLocalModel(playlists);
        } catch (ConnectionException e) {
            onFail.execute(null);
            e.printStackTrace();
        }

        postExecute.execute(null);
        return null;
    }

    private void importToLocalModel(List<YoutubePlayList> remotePlaylists) {
        /**Import from remote model all that is not already in local model**/
        LocalModelRoot.getWriteInstance().importRemotePlaylists(remotePlaylists);
        //persist the new data now in case we crash later on we don't loose data

        dbLinker.saveModel();
    }


    private List<YoutubePlayList> getRemoteModel() throws ConnectionException {
        String playListsString = youtubeLinker.getPlaylists();
        try {
            List<YoutubePlayList> playlists = youtubeLinker.getJsonHelper().extractPlaylists(playListsString);

            //test
            //YoutubePlayList pl = playlists.get(0);
            //List<YoutubePlayList> pls = new ArrayList<>();
            //pls.add(pl);
            //playlists = pls;
            // test <----remove this
            List<YoutubeSong> pojoPlaylistSongs = new ArrayList<>();

            for (YoutubePlayList playlist : playlists) {
                List<String> songsJson = youtubeLinker
                        .getSongs(playlist.getId());


                List<String[]> playlistSongList = new ArrayList<>();
                for (String songBatch : songsJson) {
                    playlistSongList.addAll(youtubeLinker.getJsonHelper().extractSongsInfo(songBatch));
                }


                for (String[] parsedSong : playlistSongList) {
                    //song ID's contain playlistID since a song with he same id can be in 2 playlists (aggregate key)
                    String songId = parsedSong[0];
                    String playlistID = playlist.getId();
                    String songName = parsedSong[1];
                    String songThumbnail = parsedSong[2];
                    String metadataString = youtubeLinker.getSongMetadata(songId);
                    String duration = youtubeLinker.getJsonHelper().extractSongDuration(metadataString);
                    Long songDuration = 0L;
                    if (duration!=null) {
                        songDuration = youtubeLinker.youtubeDurationToSec(duration);
                    }

                    if (!pojoPlaylistSongs.contains(songId)) {
                        pojoPlaylistSongs.add(new YoutubeSong(songId, playlistID, songName, songThumbnail,songDuration));
                    }
                }

                playlist.setSongs(pojoPlaylistSongs);
                playlistSongList.clear();
                pojoPlaylistSongs.clear();
            }
            return playlists;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}
