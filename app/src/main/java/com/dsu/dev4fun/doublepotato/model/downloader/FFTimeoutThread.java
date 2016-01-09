package com.dsu.dev4fun.doublepotato.model.downloader;


import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FFTimeoutThread extends Thread {
    private final long TIMEOUT_BETWEEN_UPDATES = 10000;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AtomicLong lastUpdateTime = new AtomicLong();
    private MethodWrapper onTimeout;

    public FFTimeoutThread(MethodWrapper<Void> onTimeout) {
        this.onTimeout=onTimeout;
    }

    public void setLastUpdateTime(long value) {
        lastUpdateTime.set(value);
    }

    @Override
    public void run() {
        final AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());
        while (isRunning.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (System.currentTimeMillis() - lastUpdateTime.get() > TIMEOUT_BETWEEN_UPDATES) {
                isRunning.set(false);
                onTimeout.execute(null);
            }
        }
    }

    public void stopThread(){
        isRunning.set(false);
    }
}

