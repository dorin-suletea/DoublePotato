package com.dsu.dev4fun.doublepotato.ui;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.MenuItem;
import android.widget.Button;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeSong;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.root.LocalModelRoot;

import java.io.File;
import java.util.List;

public class BusinessLogicHelper {
    public static String makePlaylistDownloadedLabelText(int alreadyDownloaded, int totalCount) {
        return new String(alreadyDownloaded + " of " + totalCount);
    }

    public static boolean isServiceRunning(String serviceClassName, Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }

    public static String replaceFileExtension(String inputFileAbsolutePath, String desiredExtension) {
        //get current file extension
        String fileExtension = "";
        int i = inputFileAbsolutePath.lastIndexOf('.') - 1;
        if (i > 0) {
            fileExtension = inputFileAbsolutePath.substring(i + 1);
        }

        //get positions
        StringBuilder fileNameBuilder = new StringBuilder(inputFileAbsolutePath);

        int extensionSize = fileExtension.length();
        int extensionLocationInString = inputFileAbsolutePath.lastIndexOf(fileExtension);
        fileNameBuilder = fileNameBuilder.replace(extensionLocationInString, extensionLocationInString + extensionSize, desiredExtension);
        return fileNameBuilder.toString();
    }

    public static void setButtonEnabled(Context context, Button theButton, boolean isEnabled, ButtonIcons icons) {
        theButton.setEnabled(isEnabled);
        Drawable icon = isEnabled ? context.getResources().getDrawable(icons.getEnabledIcon()) : context.getResources().getDrawable(icons.getDisabledIcon());
        theButton.setBackground(icon);
    }

    public static void setButtonEnabled(Context context, MenuItem theButton, boolean isEnabled, ButtonIcons icons) {
        theButton.setEnabled(isEnabled);
        Drawable icon = isEnabled ? context.getResources().getDrawable(icons.getEnabledIcon()) : context.getResources().getDrawable(icons.getDisabledIcon());
        theButton.setIcon(icon);
    }

    public static void replaceCurrentFragment(Activity activity, Bundle params, Fragment newFragment) {
        //set the playlist fragment
        newFragment.setArguments(params);
        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_place, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }

    public static Vibrator makeVibrator(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        return vibrator;
    }

    /**
     * @return the size of all the files downloaded by this application
     */
    public static long getCurrentlyOccupiedMemory() {
        int memory = 0;
        for (YoutubePlayList playlist : LocalModelRoot.getReadInstance().getPlayLists()) {
            for (YoutubeSong song : playlist.getPlayableSongList()) {
                File f = new File(song.getSongFileLocation());
                memory += f.length();
            }
        }
        //byte to mb conversion
        return memory;
    }

    public static long getFileSize(String path) {
        if (path.isEmpty()) {
            //invalid file path
            return 0;
        }

        File f = new File(path);
        if (f.exists()) {
            return f.length();
        } else {
            return 0;
        }
    }

    public static void showInfoDialog(Activity activity, String title, String message) {
        new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing except closing the error dialog
                    }
                })
                .show();
    }

    public static String convertToDisplayableTime(long totalSeconds) {
        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        long seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        long totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        long minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        long hours = totalMinutes / MINUTES_IN_AN_HOUR;

        String outTime = "(";
        if (totalMinutes != 0) {
            outTime += totalMinutes + ":";
        }
        if (seconds != 0) {
            outTime += seconds;
        }
        outTime += ")";
        return outTime;
    }
}
