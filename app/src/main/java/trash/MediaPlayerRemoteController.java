package trash;


import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

public class MediaPlayerRemoteController {
    private MediaSessionCompat mediaSession;
    private Context context;

    public MediaPlayerRemoteController(Context context) {
        this.context = context;
    }
    /*

    public void start() {
        ComponentName receiver = new ComponentName(context.getPackageName(), MediaPlayReceiver.class.getName());
        mediaSession = new MediaSessionCompat(context, "PlayerService", receiver, null);
        //flags
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        //playback state default
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());

        //meta
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Test Artist")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Test Album")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Test Track Name")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 10000)
                .build());
        //audio focus
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        mediaSession.setActive(true);
    }

    public void onStateChanged() {
        Log.d("=!=","State changed");
        if (mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());
        } else {
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());
        }
    }

    public void stop() {
        mediaSession.release();
    }
    */
}
