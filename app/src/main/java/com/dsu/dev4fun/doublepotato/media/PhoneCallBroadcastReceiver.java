package com.dsu.dev4fun.doublepotato.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneCallBroadcastReceiver extends BroadcastReceiver {
    private boolean mediaPausedByPhoneCall;


    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            mediaPausedByPhoneCall = true;
            MediaPlayServiceProxy.getInstance().pause();
            Log.d("=!=", "Pause on call");
            return;
        }

        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            if (mediaPausedByPhoneCall) {
                // resume on phone call end only if was paused by phone call
                mediaPausedByPhoneCall = false;
                Log.d("=!=", "Resume on call end");
                MediaPlayServiceProxy.getInstance().resume();
            }
            return;
        }

    }
}
