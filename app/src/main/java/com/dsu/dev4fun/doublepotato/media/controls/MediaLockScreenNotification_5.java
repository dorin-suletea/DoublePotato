package com.dsu.dev4fun.doublepotato.media.controls;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.media.AbstractMediaNotificationManager;
import com.dsu.dev4fun.doublepotato.media.MediaConstants;

public class MediaLockScreenNotification_5 extends AbstractMediaNotificationManager {
    private Service playbackService;

    public MediaLockScreenNotification_5(Service playbackService) {
        this.playbackService = playbackService;
    }

    public void updateNotificationState(MediaConstants.MediaServiceState state, String metadataSongName) {
        if (MediaConstants.MediaServiceState.STARTED == state) {
            buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", MediaConstants.ACTION_PAUSE),metadataSongName);
        }
        if (MediaConstants.MediaServiceState.PAUSED == state) {
            buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", MediaConstants.ACTION_PLAY),metadataSongName);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildNotification(Notification.Action action,String metadataSongName) {
        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intent = new Intent();
        intent.setAction(MediaConstants.ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(playbackService, 1, intent, 0);

        Notification.Builder builder = new Notification.Builder(playbackService)
                .setSmallIcon(R.drawable.abc_ic_commit_search_api_mtrl_alpha)
                .setContentTitle(metadataSongName)
                .setDeleteIntent(pendingIntent)
                .setStyle(style);

        builder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", MediaConstants.ACTION_PREVIOUS));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", MediaConstants.ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2, 3, 4);

        NotificationManager notificationManager = (NotificationManager) playbackService.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Notification.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(playbackService, playbackService.getClass());
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(playbackService.getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    public void release() {
        NotificationManager notificationManager = (NotificationManager) playbackService.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }
}
