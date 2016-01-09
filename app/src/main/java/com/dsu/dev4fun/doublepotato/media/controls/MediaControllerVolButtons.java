package com.dsu.dev4fun.doublepotato.media.controls;


import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Vibrator;

import com.dsu.dev4fun.doublepotato.media.MediaPlayServiceProxy;
import com.dsu.dev4fun.doublepotato.model.UserPreferences;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;

/**
 * Changes the current song using the VolumeButtons:
 * If the screen is off, this will change the song on volume presses, and discard the volume change itself (will not change volume while screen is unlit)
 * In order to change the volume when this controller is in use press the power button while in lockscreen (do not unlock), the screen will be lit and the
 * volumes can be changed normally.
 * =================================================
 * Usage : Phone is locked, screen is off.
 * Press volume up for next song. Press volume down for previous song.
 * Usage : Phone is locked, screen is on.
 * The volume is changed.
 * =================================================
 * //Setup (in media player service preferably):
 * playerController = new MediaControllerVolButtons(this,new Handler(), BusinessLogicHelper.makeVibrator(getApplicationContext()));
 * playerController.registerSelf();
 * //do not forget to release with unregisterSelf(); when destroying the service
 * =================================================
 */
public class MediaControllerVolButtons extends ContentObserver {
    private final static int VIBRATION_DURATION = 100;
    private Vibrator vibrator;
    private int previousVolume;
    private Context context;
    private AudioManager audio;


    public MediaControllerVolButtons(Context context, Handler handler, Vibrator vibrator) {
        super(handler);
        this.context = context;
        this.vibrator = vibrator;
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }



    @Override
    public synchronized void onChange(boolean selfChange) {
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (BusinessLogicHelper.isScreenOn(context)) {
            //screen is on change the volumes accordingly
            previousVolume = currentVolume;
            return;
        }

        if (!UserPreferences.getInstance().isVolControlsEnabled()) {
            //the module is disabled
            previousVolume = currentVolume;
            return;
        }


        int delta = previousVolume - currentVolume;

        // this will trigger a recursive call, since change volume will redispatch the onChange
        // unregisters, set volume to previous volume value, re-register listener
        unregisterSelf();
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
        registerSelf();


        if (delta > 0) {
            onPrevious();
        }
        if (delta < 0) {
            onNext();
        }
    }

    private void onNext() {
        boolean playerAccepted = MediaPlayServiceProxy.getInstance().playNext();
        if (playerAccepted) {
            vibrator.vibrate(VIBRATION_DURATION);
        }
    }

    private void onPrevious() {
        boolean playerAccepted = MediaPlayServiceProxy.getInstance().playPrevious();
        if (playerAccepted) {
            vibrator.vibrate(VIBRATION_DURATION);
        }
    }

    public void unregisterSelf() {
        context.getContentResolver().unregisterContentObserver(this);
    }

    public void registerSelf() {
        context.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, this);
    }
}
