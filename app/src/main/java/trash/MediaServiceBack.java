package trash;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeSong;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GameDev on 11/22/2015.
 */
public class MediaServiceBack {
    /**
     * Incoming intent keys*
     */
    public static final String IN_PLAYBACK_QUEUE_ACTION = "PLAY_QUEUE_KEY";
    public static final String PLAY_PAUSE_ACTION = "PLAY_PAUSE_AssssCTION";
    public static final String REFRESH_STATE_ACTION = "REFRESH_STATE_ACTION";
    public static final String PLAY_QUEUE_PAYLOAD_KEY = "PLAY_QUEUE_KEY";
    public static final String HOOK_MESSENGER_KEY = "HOOTK_MESSANGER";
    /**
     * Outgoing intent keys*
     */
    public static final String OUT_CONTENT_STATE_ACTION = "MEDIAPLAYER_STATE_OUT";
    public static final String CURRENT_SONG_INDEX_KEY = "MEDIAPLAYER_OUT_SONG_KEY";
    public static final String PLAY_QUEUE_SIZE_KEY = "MEDIAPLAYER_OUT_LIST_SIZE";

    public static final String OUT_LIFECYCLE_ACTION = "OUT_LIFECYCLE_ACTION";
    public static final String SERVICE_STATE_KEY = "servstate";

/*
    public final IBinder serviceBinder = new MediaPlayerServiceBinder();
    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;

    private List<YoutubeSong> playbackQueue;
    private MediaServiceState internalState;
    private int currentSongIndex;
    private int currentPosition;

    @Override
    public void onCreate() {
        super.onCreate();
        playbackQueue = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        makeMediaSession();
        internalState = MediaServiceState.STOPPED;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        if (IN_PLAYBACK_QUEUE_ACTION.equals(intent.getAction())) {
            Log.d("=!=", "Q add");
            internalState = MediaServiceState.STARTED;
            sendLifecycleBroadcast(MediaServiceState.STARTED);
            List<YoutubeSong> playQueue = (List<YoutubeSong>) intent.getExtras().get(PLAY_QUEUE_PAYLOAD_KEY);
            playbackQueue.clear();
            playbackQueue.addAll(playQueue);
            currentSongIndex = 0;
            currentPosition = 0;
            onSongIndexChange();
            startPlayback();
        }

        if (PLAY_PAUSE_ACTION.equals(intent.getAction())) {
            switchPlayState();
        }
        updateMediaSessionState();

        return START_STICKY;
    }


    /**
     * External API
     **/
    /*
    public void pauseCurrent() {
        mediaPlayer.pause();
        currentPosition = mediaPlayer.getCurrentPosition();

        internalState = MediaServiceState.PAUSED;
        sendLifecycleBroadcast(MediaServiceState.PAUSED);
        //updateMediaSessionState();
    }

    public void resumeCurrent() {
        internalState = MediaServiceState.STARTED;
        sendLifecycleBroadcast(MediaServiceState.STARTED);

        mediaPlayer.seekTo(currentPosition);
        currentPosition = -1;

        startPlayback();
        //updateMediaSessionState();
    }

    public void playNext() {
        currentSongIndex++;
        onSongIndexChange();
        startPlayback();
    }

    public void playPrevious() {
        currentSongIndex--;
        onSongIndexChange();
        startPlayback();
    }

    /**
     * Internal

    private void onSongIndexChange() {
        sendContentStateBroadcast();
        String dataSrc = playbackQueue.get(currentSongIndex).getSongFileLocation();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(dataSrc);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateMediaSessionState() {
        if (MediaServiceState.STARTED.equals(internalState)) {
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
                    .setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build());
        } else {
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                    .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build());
        }

        mediaSession.setActive(true);
    }

    public void setDefault(){

    }

    public PlaybackStateCompat playbackState(int state) {
        return new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(state, 0, 0)
                .build();
    }

    private void startPlayback() {
        mediaPlayer.start();
    }

    private void switchPlayState() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            currentPosition = mediaPlayer.getCurrentPosition();

            internalState = MediaServiceState.PAUSED;
            sendLifecycleBroadcast(MediaServiceState.PAUSED);
        } else {
            internalState = MediaServiceState.STARTED;
            sendLifecycleBroadcast(MediaServiceState.STARTED);

            mediaPlayer.seekTo(currentPosition);
            currentPosition = -1;

            startPlayback();
        }
    }

    /**
     * Lifecycle

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (currentSongIndex < playbackQueue.size() - 1) {
            playNext();
        } else {
            mediaPlayer.stop();
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendLifecycleBroadcast(MediaServiceState.STOPPED);
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaSession.release();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public class MediaPlayerServiceBinder extends Binder {
        public MediaPlayService getService() {
            return MediaPlayService.this;
        }
    }

    public void sendContentStateBroadcast() {
        Intent progressIntent = new Intent();
        progressIntent.setAction(OUT_CONTENT_STATE_ACTION);
        progressIntent.putExtra(CURRENT_SONG_INDEX_KEY, currentSongIndex);
        progressIntent.putExtra(PLAY_QUEUE_SIZE_KEY, playbackQueue.size());
        sendBroadcast(progressIntent);
    }

    public void sendLifecycleBroadcast(MediaServiceState state) {
        Intent lifecycleIntent = new Intent();
        lifecycleIntent.setAction(OUT_LIFECYCLE_ACTION);
        int stateIndex = state.ordinal();
        lifecycleIntent.putExtra(SERVICE_STATE_KEY, stateIndex);
        sendBroadcast(lifecycleIntent);
    }

    private void makeMediaSession() {
        ComponentName receiver = new ComponentName(this.getPackageName(), MediaPlayReceiver.class.getName());
        mediaSession = new MediaSessionCompat(this, "PlayerService", receiver, null);
        //flags
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        //playback state default
        //mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
        //        .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
        //        .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        //        .build());
        //meta
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Test Artist")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Test Album")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Test Track Name")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 100)
                .build());
        //audio focus
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public enum MediaServiceState {
        STARTED,
        STOPPED,
        PAUSED,
        DEFAULT
    }
    */
}
