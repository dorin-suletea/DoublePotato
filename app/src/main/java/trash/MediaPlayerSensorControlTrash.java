package trash;


import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MediaPlayerSensorControlTrash implements SensorListener {
    private final static int MIN_SHAKE_SPEED = 500;
    private long lastUpdate;
    private float last_x;
    private float last_y;
    private float last_z;

    private long lastActionTime;
    private final static int MAX_ACTION_DISPATCH_RATE = 1000; //1s


    @Override
    public void onSensorChanged(int sensor, float[] values) {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float x = values[SensorManager.DATA_X];
                float y = values[SensorManager.DATA_Y];
                float z = values[SensorManager.DATA_Z];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > MIN_SHAKE_SPEED) {
                    float delta = last_x - x;
                    if (delta > 2) {
                        dispatchMediaAction(true);
                    }
                    if (delta < 2) {
                        dispatchMediaAction(false);
                    }
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {
    }

    public void dispatchMediaAction(boolean forward) {
        long now = System.currentTimeMillis();
        if (now - lastActionTime > MAX_ACTION_DISPATCH_RATE) {
            if (forward) {
                Log.d("=!=", ">>");
            } else {
                Log.d("=!=", "<<");
            }
            lastActionTime = now;
        }
    }
}
