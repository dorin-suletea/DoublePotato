package trash;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.dsu.dev4fun.doublepotato.media.MediaPlayService;

class MedimanaPlayReceiverxx extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            return;
        }

        final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE :
                    Intent playPauseIntent = new Intent(context, MediaPlayService.class);
                    playPauseIntent.setAction(MediaPlayService.PLAY_PAUSE_ACTION);
                    context.startService(playPauseIntent);
                    break;
            }
        }
    }

}