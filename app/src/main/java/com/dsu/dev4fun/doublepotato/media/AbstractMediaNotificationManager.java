package com.dsu.dev4fun.doublepotato.media;

import android.app.Service;
import android.content.Intent;

import com.dsu.dev4fun.doublepotato.media.controls.MediaLockScreenNotification_4;
import com.dsu.dev4fun.doublepotato.media.controls.MediaLockScreenNotification_5;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;

public abstract class AbstractMediaNotificationManager {
    public final static AbstractMediaNotificationManager getMediaLockscreenNotificationManager(Service playbackService) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return new MediaLockScreenNotification_5(playbackService);
        } else {
            return new MediaLockScreenNotification_4(playbackService);
        }
    }

    public final void handleIntentActions(Intent intent, MethodWrapper<Void> onPlay, MethodWrapper<Void> onPause, MethodWrapper<Void> onNext, MethodWrapper<Void> onPrevious) {
        String action = intent.getAction();
        if (action.equalsIgnoreCase(MediaConstants.ACTION_PLAY)) {
            onPlay.execute(null);
        } else if (action.equalsIgnoreCase(MediaConstants.ACTION_PAUSE)) {
            onPause.execute(null);
        } else if (action.equalsIgnoreCase(MediaConstants.ACTION_PREVIOUS)) {
            onPrevious.execute(null);
        } else if (action.equalsIgnoreCase(MediaConstants.ACTION_NEXT)) {
            onNext.execute(null);
        }
    }

    public abstract void updateNotificationState(MediaConstants.MediaServiceState state, String metadataSongName);

    public abstract void release();
}
