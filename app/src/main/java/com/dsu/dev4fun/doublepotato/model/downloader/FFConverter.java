package com.dsu.dev4fun.doublepotato.model.downloader;


import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;
import com.dsu.dev4fun.doublepotato.model.util.UnrecoverableException;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class FFConverter {
    private static FFConverter instance = new FFConverter();
    private FFmpeg wrappedFFmpeg;
    private boolean libLoaded;
    private CountDownLatch executionLath;
    private Activity context;

    public static FFConverter getInstance() {
        return instance;
    }


    private FFConverter() {
        libLoaded = false;
    }

    public synchronized void loadNative(Activity context, final MethodWrapper<Boolean> onLoad) {
        if (libLoaded) {
            onLoad.execute(true);
            return;
        }
        this.context = context;
        try {
            FFConverter.this.wrappedFFmpeg = FFmpeg.getInstance(context);
            wrappedFFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                    libLoaded = false;
                }

                @Override
                public void onSuccess() {
                    libLoaded = true;
                }

                @Override
                public void onFinish() {
                    onLoad.execute(libLoaded);
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
            libLoaded = false;
            onLoad.execute(false);
        }
    }

    public String convertToMp3(String inputFilePath) throws UnrecoverableException {
        final String newFileName = BusinessLogicHelper.replaceFileExtension(inputFilePath, ".aac");
        final String command = "-i " + inputFilePath + " -vn -acodec copy " + newFileName;
        executionLath = new CountDownLatch(1);

        final int operationFail = 0;
        final int operationOk = 1;
        final int operationFreeze = -1;
        final AtomicInteger operationSuccess = new AtomicInteger(operationFail);


        MethodWrapper onTimeout = new MethodWrapper() {
            @Override
            public void execute(Object params) {
                //set failure, and exit this method
                operationSuccess.set(operationFreeze);
                executionLath.countDown();
            }
        };
        final FFTimeoutThread timeoutThread = new FFTimeoutThread(onTimeout);


        try {
            wrappedFFmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    timeoutThread.start();
                }

                @Override
                public void onProgress(String message) {
                    timeoutThread.setLastUpdateTime(System.currentTimeMillis());
                    Log.d("=!=", "FFMPEG = " + message);
                }

                @Override
                public void onFailure(String message) {
                    timeoutThread.stopThread();
                    operationSuccess.set(operationFail);

                }

                @Override
                public void onSuccess(String message) {
                    timeoutThread.stopThread();
                    operationSuccess.set(operationOk);
                }

                @Override
                public void onFinish() {
                    executionLath.countDown();
                    timeoutThread.stopThread();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e1) {
            e1.printStackTrace();
            Toast.makeText(context, "FFMPEG error: Please restart the application", Toast.LENGTH_LONG);
            operationSuccess.set(operationFail);
            executionLath.countDown();
        } finally {
            try {
                executionLath.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (operationSuccess.get() == operationFreeze) {
                throw new UnrecoverableException("FFmpeg froze, must restart the app");
            }


            return operationSuccess.get() == operationOk ? newFileName : "";
        }
    }
}
