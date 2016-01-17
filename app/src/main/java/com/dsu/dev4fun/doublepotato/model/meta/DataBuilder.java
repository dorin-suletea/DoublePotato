package com.dsu.dev4fun.doublepotato.model.meta;


import android.content.Context;
import android.util.Log;

import com.dsu.dev4fun.doublepotato.model.downloader.FFConverter;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeError;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.root.LocalModelRoot;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeSong;
import com.dsu.dev4fun.doublepotato.model.meta.tasks.PopulateLocalModelTask;
import com.dsu.dev4fun.doublepotato.model.meta.tasks.SynchronizeModelsTask;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;
import com.dsu.dev4fun.doublepotato.model.util.UnrecoverableException;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class DataBuilder {
    private YoutubeLinker youtubeLinker;
    private DatabaseLinker dbLinker;


    private final static DataBuilder instance = new DataBuilder();

    public static final DataBuilder getInstance() {
        return instance;
    }

    private DataBuilder() {
    }

    public void init(String youtubeChannelID, String myGoogleAppKey, Context appContext) {
        this.youtubeLinker = new YoutubeLinker(myGoogleAppKey, youtubeChannelID);
        this.dbLinker = new DatabaseLinker(appContext);
    }

    public void readLocalModel(MethodWrapper<Void> whenDone) {
        PopulateLocalModelTask localModelInitTask = new PopulateLocalModelTask(dbLinker, whenDone);
        localModelInitTask.execute();
    }

    public void syncLocalModelWithRemoteModel(final MethodWrapper<Void> whenDone, final MethodWrapper<Void> onFail) {
        new SynchronizeModelsTask(youtubeLinker, dbLinker, whenDone, onFail).execute();
    }


    public void onSongDownloaded(String songFilename, String songID, String playlistID) {
        YoutubePlayList playlist = LocalModelRoot.getReadInstance().getPlaylistByID(playlistID);
        YoutubeSong song = playlist.getSongByID(songID);

        song.setSongFileLocation(songFilename);
        removeSongError(song.getId());
        dbLinker.updateSongFile(song.getId(), songFilename);
    }

    public void onSongDownloadFail(String songID,YoutubeError error){
        dbLinker.updateSongError(songID, error);
    }

    public void purgeLocalDbAndFiles(){
        for (YoutubePlayList playlists : LocalModelRoot.getReadInstance().getPlayLists()){
            for (YoutubeSong song : playlists.getPlayableSongList()){
                File f = new File(song.getSongFileLocation());
                f.delete();
            }
        }
        dbLinker.purgeAll();
    }

    private void removeSongError(String songId){
        onSongDownloadFail(songId, YoutubeError.NONE);
    }

    /**
     * Discontinued : ffpeg not stable, freeze
     */
    @Deprecated
    private String convertToAac(final String songFileName, final YoutubeSong song) {
        try {
            String convertedSongPath = FFConverter.getInstance().convertToMp3(songFileName);
            if (!convertedSongPath.isEmpty()) {
                File mp4File = new File(songFileName);
                if (mp4File.exists()) {
                    mp4File.delete();
                }

                song.setSongFileLocation(convertedSongPath);
                removeSongError(song.getId());
                dbLinker.updateSongFile(song.getId(), convertedSongPath);
                return convertedSongPath;
            } else {
                Log.d("=!=", "Failed to convert song, Not saving");
                onSongDownloadFail(song.getId(),YoutubeError.FAIL_CONVERT);
                return "";
            }
        } catch (UnrecoverableException e) {
            //this is a FFmpeg freeze, the song must be saved as mp4 and app restarted
            //song.setSongFileLocation(songFileName);
            //dbLinker.updateSongFile(song.getId(), songFileName);

            onSongDownloadFail(song.getId(), YoutubeError.FAIL_CONVERT);
            Log.d("=!=", "FF freeze");

            CountDownLatch freezeLath = new CountDownLatch(1);
            try {
                freezeLath.await();
                return songFileName;
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        }
        //can't reach
        return "";
    }
}
