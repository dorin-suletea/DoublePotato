package com.dsu.dev4fun.doublepotato.media;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;

/**
 * This is used to keep track of the media player service,
 * It will mirror the data in the media server
 */
public class MediaPlayServiceProxy {
    private Context context;
    private static MediaPlayServiceProxy instance = new MediaPlayServiceProxy();
    private YoutubePlayList mediaPlaylist;
    private MediaConstants.MediaServiceState serviceState;

    private MediaPlayServiceProxy() {
    }

    public static MediaPlayServiceProxy getInstance() {
        return instance;
    }

    public void setup(Context context) {
        this.context = context;
    }

    public void setServiceState(MediaConstants.MediaServiceState state) {
        serviceState = state;
    }

    public boolean isStopped() {
        return MediaConstants.MediaServiceState.STOPPED == serviceState;
    }

    public MediaConstants.MediaServiceState  getServiceState(){
        return serviceState;
    }

    public void setPlaylist(YoutubePlayList playlist) {
        this.mediaPlaylist = playlist;
        Intent intent = new Intent(context, MediaPlayService.class);
        intent.setAction(MediaPlayService.SET_QUEUE_ACTION);
        intent.putExtra(MediaPlayService.SET_QUEUE_KEY, playlist);
        context.startService(intent);
    }

    public void playSong(String songID) {
        Intent previousIntent = new Intent(context, MediaPlayService.class);
        previousIntent.setAction(MediaPlayService.PLAY_SONG_ACTION);
        previousIntent.putExtra(MediaPlayService.PLAY_SONG_KEY, songID);
        context.startService(previousIntent);
    }

    public void stopService() {
        Intent previousIntent = new Intent(context, MediaPlayService.class);
        previousIntent.setAction(MediaPlayService.STOP_SERVICE_ACTION);
        context.startService(previousIntent);
    }

    public boolean playNext() {
        if (MediaConstants.MediaServiceState.STARTED == serviceState) {
            Intent playNext = new Intent(context, MediaPlayService.class);
            playNext.setAction(MediaConstants.ACTION_NEXT);
            context.startService(playNext);
            return true;
        }
        return false;
    }

    public boolean playPrevious() {
        if (MediaConstants.MediaServiceState.STARTED == serviceState) {
            Intent previousIntent = new Intent(context, MediaPlayService.class);
            previousIntent.setAction(MediaConstants.ACTION_PREVIOUS);
            context.startService(previousIntent);
            return true;
        }
        return false;
    }

    public void pause() {
        if (MediaConstants.MediaServiceState.STARTED == serviceState) {
            Intent pauseIntent = new Intent(context, MediaPlayService.class);
            pauseIntent.setAction(MediaConstants.ACTION_PAUSE);
            context.startService(pauseIntent);
        }
    }

    public void resume() {
        if (MediaConstants.MediaServiceState.PAUSED == serviceState) {
            Intent resumeIntent = new Intent(context, MediaPlayService.class);
            resumeIntent.setAction(MediaConstants.ACTION_PLAY);
            context.startService(resumeIntent);
        }
    }

    public YoutubePlayList getCurrentlyPlayingPlaylist(){
        return mediaPlaylist;
    }
}
