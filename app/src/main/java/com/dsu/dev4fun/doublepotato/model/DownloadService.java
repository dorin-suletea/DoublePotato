package com.dsu.dev4fun.doublepotato.model;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.dsu.dev4fun.doublepotato.model.downloader.YoutubeDirectDownloader;
import com.dsu.dev4fun.doublepotato.model.meta.DataBuilder;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeError;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeSong;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.root.LocalModelRoot;
import com.dsu.dev4fun.doublepotato.model.util.DownloadManualInterruptedException;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadService extends Service {
    /**
     * Constants*
     */
    private final static String KEEPVID_URL = "http://keepvid.com/";
    private final static String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    /**
     * Outgoing intent keys*
     */
    public static final String OUT_DOWNLOADING_NEW_SONG_ACTION = "DOWNLOAD_CURRENT_SONG_CHANGED_ACTION_KEY";
    public static final String OUT_PROGRESS_UPDATED_ACTION = "DOWNLOAD_PROGRESS_ACTION_KEY";
    public static final String OUT_CONVERSION_STARTED_ACTION = "CONVERSION";
    public static final String OUT_CONVERSION_FINISHED_ACTION = "CONVERSION_FINISH";
    public static final String OUT_CURRENTLY_DOWNLOADING_PLAYLIST_ACTION = "OUT_CURRENTLY_DOWNLOADING_ACTION";
    public static final String OUT_OUT_OF_STORAGE_MEM_ACTION = "OUT_OUT_OF_STORAGE_MEM";

    /**
     * Outgoing Extra keys*
     */
    public static final String PROGRESS_EXTRA_KEY = "DOWNLOAD_PROGRESS_EXTRA_KEY";
    public static final String SONG_NAME_EXTRA_KEY = "DOWNLOAD_CURRENT_SONG";
    public static final String PLAYLIST_COMPLETION_DOWNLOADED_KEY = "DOWNLOAD_ALREADY_DOWNLOADED_SONG_COUNT";
    public static final String PLAYLIST_COMPLETION_TOTAL_KEY = "DOWNLOAD_TOTAL_SONG_COUNT";
    public static final String CURRENTLY_DOWNLOADING_PLAYLIST_KEY = "OUT_CURRENTLY_DOWNLOADING_PLAYLIST_KEY";
    public static final String CURRENTLY_DOWNLOADING_SONGNAME_KEY = "CURRENTLY_DOWNLOADING_SONGNAME_KEY";

    /**
     * Incoming intent keys*
     */
    public static final String START_DOWNLOAD = "ADD_QUEUE";
    public static final String DOWNLOAD_STATUS = "GET_CURRENTLY_DOWNLOADING_PLAYLIST";

    private List<YoutubePlayList> downloadQueue;
    private Thread workerThread;
    private AtomicBoolean isRunning;
    //do not download songs that are longer than 20 min
    private final long MAX_ACCEPTED_SONG_DURATION = 1200;


    private String currentDownloadSongNameAUX;
    private long currentMemoryBytes;


    public DownloadService() {
        downloadQueue = new ArrayList<>();
        isRunning = new AtomicBoolean(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Thread makeWorkerThread() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                YoutubeDirectDownloader downloader = new YoutubeDirectDownloader();
                while (isRunning.get()) {
                    for (YoutubePlayList playList : downloadQueue) {
                        for (YoutubeSong song : playList.getSongs()) {
                            if (!isRunning.get()) {
                                stopSelf();
                                return;
                            }
                            if (currentMemoryBytes / 1000000 >= UserPreferences.getInstance().getMemoryAllocation()) {
                                sendOutOfStorageSpace();
                                stopSelf();
                                return;
                            }
                            if (song.isSavedLocally()) {
                                continue;
                            }
                            if (song.getDuration() > MAX_ACCEPTED_SONG_DURATION) {
                                //Song too long , skipping
                                onDownloadError(YoutubeError.TOO_BIG, song.getId(), playList.getId());
                                continue;
                            }

                            currentDownloadSongNameAUX = song.getName();
                            onNewSongDownloading(song.getName(), playList.getId());
                            try {
                                File songFile = downloader.fetchVideo(song.getId(), song.getName(), getSongProgressUpdateDelegate(), isRunning, DownloadService.this.getApplicationContext());
                                if (songFile == null) {
                                    onDownloadError(YoutubeError.FAIL_DOWNLOAD, song.getId(), playList.getId());
                                    continue;
                                }

                                onConversionStarting("Saving " + song.getName());
                                DataBuilder.getInstance().onSongDownloaded(songFile.getAbsolutePath(), song.getId(), playList.getId());
                                onConversionFinished();
                                currentMemoryBytes += BusinessLogicHelper.getFileSize(songFile.getAbsolutePath());
                                currentDownloadSongNameAUX = null;

                            } catch (DownloadManualInterruptedException e) {
                                //do not do anything if manual interruption
                            }

                        }
                    }
                    isRunning.set(false);
                }
            }
        });
        return t;
    }

    private void onDownloadError(YoutubeError error, String songId, String playlistId) {
        YoutubePlayList playlist = LocalModelRoot.getReadInstance().getPlaylistByID(playlistId);
        YoutubeSong saveAbleSong = playlist.getSongByID(songId);
        saveAbleSong.setError(error);
        DataBuilder.getInstance().onSongDownloadFail(songId, error);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && START_DOWNLOAD.equals(intent.getAction())) {
            final Object playlistObject = intent.getExtras().get(YoutubePlayList.PLAYLIST_EXTRA_KEY);
            if (playlistObject != null && playlistObject instanceof YoutubePlayList) {
                YoutubePlayList playlist = (YoutubePlayList) playlistObject;
                downloadQueue.add(playlist);
            }
            if (!isRunning.get()) {
                currentMemoryBytes = BusinessLogicHelper.getCurrentlyOccupiedMemory();
                Log.d("=!=", "Bootstrap download service, current mem full is " + currentMemoryBytes);

                isRunning.set(true);
                workerThread = makeWorkerThread();
                workerThread.start();
            }
        }
        if (intent != null && DOWNLOAD_STATUS.equals(intent.getAction())) {
            sendServiceDownloadStatus();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("=!=", "Stopping download Service");
        if (isRunning.get()) {
            isRunning.set(false);
            workerThread = null;
        }
        stopSelf();
    }

    private MethodWrapper<Float> getSongProgressUpdateDelegate() {
        return new MethodWrapper<Float>() {
            @Override
            public void execute(Float params) {
                Intent progressIntent = new Intent();
                progressIntent.setAction(OUT_PROGRESS_UPDATED_ACTION);
                progressIntent.putExtra(PROGRESS_EXTRA_KEY, params);

                sendBroadcast(progressIntent);
            }
        };
    }

    private void onNewSongDownloading(final String currentSongName, String playlistID) {
        YoutubePlayList playlist = LocalModelRoot.getReadInstance().getPlaylistByID(playlistID);


        int downloadedSongsCount = playlist.getDownloadedSongsCount();
        int totalSongCount = playlist.getSongs().size();

        Intent progressIntent = new Intent();
        progressIntent.setAction(OUT_DOWNLOADING_NEW_SONG_ACTION);
        progressIntent.putExtra(SONG_NAME_EXTRA_KEY, currentSongName);
        progressIntent.putExtra(PLAYLIST_COMPLETION_DOWNLOADED_KEY, downloadedSongsCount);
        progressIntent.putExtra(PLAYLIST_COMPLETION_TOTAL_KEY, totalSongCount);

        sendBroadcast(progressIntent);
    }

    private void onConversionStarting(final String currentSongName) {
        Intent progressIntent = new Intent();
        progressIntent.putExtra(SONG_NAME_EXTRA_KEY, currentSongName);
        progressIntent.setAction(OUT_CONVERSION_STARTED_ACTION);
        sendBroadcast(progressIntent);
    }

    private void onConversionFinished() {
        Intent progressIntent = new Intent();
        progressIntent.setAction(OUT_CONVERSION_FINISHED_ACTION);
        sendBroadcast(progressIntent);
    }

    private void sendServiceDownloadStatus() {
        Intent intent = new Intent();
        intent.setAction(OUT_CURRENTLY_DOWNLOADING_PLAYLIST_ACTION);
        intent.putExtra(CURRENTLY_DOWNLOADING_PLAYLIST_KEY, downloadQueue.size() != 0 ? downloadQueue.get(0) : null);
        intent.putExtra(CURRENTLY_DOWNLOADING_SONGNAME_KEY, currentDownloadSongNameAUX);
        sendBroadcast(intent);

    }

    public void sendOutOfStorageSpace() {
        Intent intent = new Intent();
        intent.setAction(OUT_OUT_OF_STORAGE_MEM_ACTION);
        sendBroadcast(intent);
    }

}
