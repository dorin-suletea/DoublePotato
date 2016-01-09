package com.dsu.dev4fun.doublepotato.media.controls;


import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.Display;

import com.dsu.dev4fun.doublepotato.media.MediaPlayServiceProxy;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;

/**
 * Changes the current song using the Accelerometer:
 * =================================================
 * Usage : Phone is locked, screen is on.
 * Tilt the phone to front and shake it. The next song is played and the phone vibrates as feedback.
 * Tilt the phone to back and shake it. The previous song is played and the phone vibrates as feedback.
 * =================================================
 * //Setup (in activity):
 * SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
 * Vibrator vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
 * sensorMgr.registerListener(new MediaControllerSensor(vibrator, this), SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);
 * =================================================
 * Not used, not feasible. The shaker is hard to control inside the pocket.
 */
public class MediaControllerShaker implements SensorListener {
    /**Sensitivity**/
    private static final int SHAKE_THRESHOLD = 400;
    private static final int SENSOR_DELAY = 100;
    /**Delay between event dispatches**/
    private final static int MAX_ACTION_DISPATCH_RATE = 1000;
    private final static int VIBRATION_DURATION = 100;

    private long lastActionTime;
    private Vibrator vibrator;
    private Context context;

    private long lastUpdate = -1;
    private float x, y, z;
    private float last_x, last_y, last_z;


    public MediaControllerShaker(Vibrator vibrator, Context context) {
        this.vibrator = vibrator;
        this.context = context;
    }

    public void onBackTilt() {
        long now = System.currentTimeMillis();
        if (now - lastActionTime > MAX_ACTION_DISPATCH_RATE) {
            boolean playerAccepted = MediaPlayServiceProxy.getInstance().playPrevious();
            if (playerAccepted) {
                vibrator.vibrate(VIBRATION_DURATION);
            }
            lastActionTime = now;
        }
    }


    public void onForwardTilt() {
        long now = System.currentTimeMillis();
        if (now - lastActionTime > MAX_ACTION_DISPATCH_RATE) {
            boolean playerAccepted = MediaPlayServiceProxy.getInstance().playNext();
            if (playerAccepted) {
                vibrator.vibrate(VIBRATION_DURATION);
            }
            lastActionTime = now;
        }
    }

    public void onSensorChanged(int sensor, float[] values) {
        if (!BusinessLogicHelper.isScreenOn(context)) {
            //do nothing if the screen is off to prevent unwanted shake events
            return;
        }

        if (sensor != SensorManager.SENSOR_ACCELEROMETER) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastUpdate) < SENSOR_DELAY) {
            return;
        }

        long deltaTime = (currentTime - lastUpdate);
        lastUpdate = currentTime;
        x = values[SensorManager.DATA_X];
        y = values[SensorManager.DATA_Y];
        z = values[SensorManager.DATA_Z];
        float speed = Math.abs(x + y + z - last_x - last_y - last_z) / deltaTime * 10000;

        if (speed > SHAKE_THRESHOLD) {
            if (round(x, 4) > 5.0000) {
                onForwardTilt();
            } else if (round(x, 4) < -5.0000) {
                onBackTilt();
            }
        }
        last_x = x;
        last_y = y;
        last_z = z;
    }


    public static float round(float val, int zeros) {
        float p = (float) Math.pow(10, zeros);
        val = val * p;
        float tmp = Math.round(val);
        return tmp / p;
    }


    public void onAccuracyChanged(int arg0, int arg1) {
    }
}
