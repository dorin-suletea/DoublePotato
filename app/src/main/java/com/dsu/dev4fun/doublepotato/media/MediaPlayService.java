package com.dsu.dev4fun.doublepotato.media;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.dsu.dev4fun.doublepotato.media.controls.MediaControllerVolButtons;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;

import java.io.IOException;

public class MediaPlayService extends Service implements OnCompletionListener, OnErrorListener {
    /**
     * Incoming intent keys*
     */
    public static final String SET_QUEUE_ACTION = "PLAY_QUEUE_KEY";
    public static final String SET_QUEUE_KEY = "PLAY_QUEUE_KEY";

    public static final String PLAY_PAUSE_ACTION = "PLAY_PAUSE_ACTION";
    public static final String STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION";

    public static final String PLAY_SONG_ACTION = "PLAY_SONG_ACTION";
    public static final String PLAY_SONG_KEY = "PLAY_SONG_KEY";

    public static final String REQ_SERVICE_STATE_ACTION = "SERVICE_STATE_ACTION";

    /**
     * Outgoing intent keys*
     */
    public static final String PLAY_STATE_OUT_ACTION = "MEDIAPLAYER_STATE_OUT";
    public static final String PLAY_STATE_SONG_INDEX_KEY = "MEDIAPLAYER_OUT_SONG_KEY";
    public static final String PLAY_STATE_SONG_ID_KEY = "PLAY_STATE__SONG_ID_KEY";
    public static final String PLAY_STATE_QUEUE_SIZE_KEY = "MEDIAPLAYER_OUT_LIST_SIZE";

    public static final String SERVICE_STATE_ACTION = "OUT_LIFECYCLE_ACTION";
    public static final String SERVICE_STATE_KEY = "servstate";


    private MediaControllerVolButtons playerController;
    private MediaPlayer mediaPlayer;
    private YoutubePlayList playbackList;

    private MediaConstants.MediaServiceState internalState;
    private AbstractMediaNotificationManager lockScreenControl;

    private String currentSongID;
    private int currentPosition;

    private MethodWrapper<Void> onPlay;
    private MethodWrapper<Void> onPause;
    private MethodWrapper<Void> onPrevious;
    private MethodWrapper<Void> onNext;


    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        setInternalState(MediaConstants.MediaServiceState.STOPPED);
        lockScreenControl = AbstractMediaNotificationManager.getMediaLockscreenNotificationManager(this);
        makeCallbacks();
        currentSongID = null;
        playerController = new MediaControllerVolButtons(this, new Handler(), BusinessLogicHelper.makeVibrator(getApplicationContext()));
        playerController.registerSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //return serviceBinder;
        return null;
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        if (SET_QUEUE_ACTION.equals(intent.getAction())) {
            YoutubePlayList newPlaylist = (YoutubePlayList) intent.getExtras().get(SET_QUEUE_KEY);
            boolean isUpdateListOnShuffle = playbackList != null && playbackList.getId().equals(newPlaylist.getId());
            playbackList = newPlaylist;
            if (isUpdateListOnShuffle) {
                //currentSongID doesn't change since it's the same playlist, but shuffled
            } else {
                currentSongID = playbackList.getPlayableSongList().get(0).getId();
            }
            sendContentStateBroadcast();
        }


        //if MediaLockScreenNotification_4 is in use, a switch state is triggered, this must decide if it's a play or stop action, and resend the intent
        if (MediaConstants.ACTION_SWITCH_STATE.equals(intent.getAction())) {
            if (this.internalState == MediaConstants.MediaServiceState.STARTED) {
                intent.setAction(MediaConstants.ACTION_PAUSE);
            } else {
                intent.setAction(MediaConstants.ACTION_PLAY);
            }
        }

        if (PLAY_SONG_ACTION.equals(intent.getAction())) {
            currentSongID = intent.getStringExtra(PLAY_SONG_KEY);
            Log.d("=!=", "Changed " + currentSongID);
            onSongIndexChange();
            startPlayback();
        }

        if (STOP_SERVICE_ACTION.equals(intent.getAction())) {
            stopSelf();
            currentSongID = null;
            currentPosition = -1;
            sendContentStateBroadcast();
            setInternalState(MediaConstants.MediaServiceState.STOPPED);
        }

        if (MediaConstants.MediaServiceState.STOPPED != internalState) {
            lockScreenControl.handleIntentActions(intent, onPlay, onPause, onNext, onPrevious);
            int currentSongIndex = getCurrentSongIndex();
            lockScreenControl.updateNotificationState(internalState, playbackList.getPlayableSongList().get(currentSongIndex).getName());
            sendContentStateBroadcast();
        }

        return START_STICKY;
    }

    private void resume() {
        mediaPlayer.seekTo(currentPosition);
        currentPosition = -1;
        startPlayback();

    }

    private void pause() {
        mediaPlayer.pause();
        currentPosition = mediaPlayer.getCurrentPosition();
        setInternalState(MediaConstants.MediaServiceState.PAUSED);
    }

    private void playNext() {
        if (playbackList.getPlayableSongList().size() != 0) {
            int currentSongIndex = getCurrentSongIndex();
            currentSongIndex = (currentSongIndex == playbackList.getPlayableSongList().size() - 1) ? 0 : currentSongIndex + 1;
            currentSongID = playbackList.getPlayableSongList().get(currentSongIndex).getId();
            onSongIndexChange();
            startPlayback();
        }
    }

    private void playPrevious() {
        if (playbackList.getPlayableSongList().size() != 0) {
            int currentSongIndex = getCurrentSongIndex();
            currentSongIndex = (currentSongIndex == 0) ? playbackList.getPlayableSongList().size() - 1 : currentSongIndex - 1;
            currentSongID = playbackList.getPlayableSongList().get(currentSongIndex).getId();
            onSongIndexChange();
            startPlayback();
        }
    }

    private int getCurrentSongIndex() {
        for (int i = 0; i < playbackList.getPlayableSongList().size(); i++) {
            if (playbackList.getPlayableSongList().get(i).getId().equals(currentSongID)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Internal
     */
    private void onSongIndexChange() {
        int currentSongIndex = getCurrentSongIndex();
        String dataSrc = playbackList.getPlayableSongList().get(currentSongIndex).getSongFileLocation();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(dataSrc);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendContentStateBroadcast();
    }

    private void makeCallbacks() {
        onPlay = new MethodWrapper<Void>() {
            @Override
            public void execute(Void params) {
                resume();
            }
        };
        onPause = new MethodWrapper<Void>() {
            @Override
            public void execute(Void params) {
                pause();
            }
        };
        onNext = new MethodWrapper<Void>() {
            @Override
            public void execute(Void params) {
                playNext();
            }
        };
        onPrevious = new MethodWrapper<Void>() {
            @Override
            public void execute(Void params) {
                playPrevious();
            }
        };
    }

    private void startPlayback() {
        mediaPlayer.start();
        setInternalState(MediaConstants.MediaServiceState.STARTED);
    }

    /**
     * Lifecycle
     */

    private void setInternalState(MediaConstants.MediaServiceState newState) {
        this.internalState = newState;
        MediaPlayServiceProxy.getInstance().setServiceState(newState);
        Intent lifecycleIntent = new Intent();
        lifecycleIntent.setAction(SERVICE_STATE_ACTION);
        int stateIndex = internalState.ordinal();
        lifecycleIntent.putExtra(SERVICE_STATE_KEY, stateIndex);
        sendBroadcast(lifecycleIntent);
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        int currentSongIndex = getCurrentSongIndex();
        if (currentSongIndex < playbackList.getPlayableSongList().size() - 1) {
            playNext();
        } else {
            mediaPlayer.stop();
            setInternalState(MediaConstants.MediaServiceState.STOPPED);
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setInternalState(MediaConstants.MediaServiceState.STOPPED);
        mediaPlayer.stop();
        playerController.unregisterSelf();
        lockScreenControl.release();
        mediaPlayer.release();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    /*
    public class MediaPlayerServiceBinder extends Binder {
        public MediaPlayService getService() {
            return MediaPlayService.this;
        }
    }
    */

    public void sendContentStateBroadcast() {
        Intent progressIntent = new Intent();
        progressIntent.setAction(PLAY_STATE_OUT_ACTION);
        progressIntent.putExtra(PLAY_STATE_SONG_INDEX_KEY, getCurrentSongIndex());
        progressIntent.putExtra(PLAY_STATE_SONG_ID_KEY, currentSongID);
        progressIntent.putExtra(PLAY_STATE_QUEUE_SIZE_KEY, playbackList.getPlayableSongList().size());

        sendBroadcast(progressIntent);
    }
}
