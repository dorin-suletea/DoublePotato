package com.dsu.dev4fun.doublepotato.media.controls;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import com.dsu.dev4fun.doublepotato.media.AbstractMediaNotificationManager;
import com.dsu.dev4fun.doublepotato.media.MediaConstants;
import com.dsu.dev4fun.doublepotato.media.MediaPlayService;

public class MediaLockScreenNotification_4 extends AbstractMediaNotificationManager {
    private Service playbackService;
    private MediaSessionCompat mediaSession;

    public MediaLockScreenNotification_4(Service playbackService) {
        this.playbackService = playbackService;

        ComponentName receiver = new ComponentName(playbackService.getPackageName(), MediaPlayReceiver.class.getName());
        mediaSession = new MediaSessionCompat(playbackService, "PlayerService", receiver, null);
        mediaSession.setActive(true);
    }

    @Override
    public void updateNotificationState(MediaConstants.MediaServiceState state, String metadataSongName) {
        if (MediaConstants.MediaServiceState.STARTED.equals(state)) {
            PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 1, 0)
                    .setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build();
            buildNotification(playbackState, metadataSongName);
        }
        if (MediaConstants.MediaServiceState.PAUSED.equals(state)) {
            PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 2, 0)
                    .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build();
            buildNotification(playbackState, metadataSongName);
        }

    }

    @Override
    public void release() {
        mediaSession.release();
    }

    private void buildNotification(PlaybackStateCompat playbackState, String metadataSongName) {
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setPlaybackState(playbackState);


        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadataSongName)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 100)
                .build());

        AudioManager audioManager = (AudioManager) playbackService.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        ;
    }

    public static class MediaPlayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                return;
            }

            final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        Intent playIntent = new Intent(context, MediaPlayService.class);
                        playIntent.setAction(MediaConstants.ACTION_SWITCH_STATE);
                        context.startService(playIntent);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        Intent nextIntent = new Intent(context, MediaPlayService.class);
                        nextIntent.setAction(MediaConstants.ACTION_NEXT);
                        context.startService(nextIntent);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        Intent previousIntent = new Intent(context, MediaPlayService.class);
                        previousIntent.setAction(MediaConstants.ACTION_NEXT);
                        context.startService(previousIntent);
                        break;
                }
            }
        }

    }

}
