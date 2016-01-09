package com.dsu.dev4fun.doublepotato.ui.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dsu.dev4fun.doublepotato.model.DownloadService;
import com.dsu.dev4fun.doublepotato.model.NetworkConnectionManager;
import com.dsu.dev4fun.doublepotato.model.meta.DataBuilder;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;
import com.dsu.dev4fun.doublepotato.ui.adapters.PlaylistListAdapter;
import com.dsu.dev4fun.doublepotato.ui.fragment.SongsFragment;


public class PlaylistController {
    private Context context;
    private PlaylistListAdapter listAdapter;

    public PlaylistController(Context context) {
        this.context = context;
    }

    public View.OnClickListener getDownloadClickListener(final YoutubePlayList playlistClicked) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConnectionManager.isConnected(context)) {
                    Toast.makeText(context, "No internet , can't download", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent startDownload = new Intent(context, DownloadService.class);
                startDownload.setAction(DownloadService.START_DOWNLOAD);
                startDownload.putExtra(YoutubePlayList.PLAYLIST_EXTRA_KEY, playlistClicked);
                context.startService(startDownload);
            }
        };
        return listener;
    }

    public View.OnClickListener getDownloadCancelClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopDownload = new Intent(context, DownloadService.class);
                context.stopService(stopDownload);
            }
        };
        return listener;
    }

    public View.OnClickListener onPlaylistClick(final YoutubePlayList playlistClicked) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playlistClicked.getPlayableSongList().size() == 0) {
                    Log.d("=!=", "Empty playlist");
                    return;
                }
                Bundle params = new Bundle();
                params.putParcelable(SongsFragment.PLAYLIST_KEY, playlistClicked);
                BusinessLogicHelper.replaceCurrentFragment(((Activity) context), params, new SongsFragment());
            }
        };
        return listener;
    }

    public void sendCurrentRowUpdate(float progress) {
        listAdapter.receiveRowUpdate(progress);
    }

    public void sendCurrentRowUpdate(String currentSongName, int[] songDownloadedAndTotalCount) {
        listAdapter.receiveRowUpdate(currentSongName, songDownloadedAndTotalCount);
    }

    public void sendCurrentRowUpdate(String currentSongName) {
        listAdapter.receiveRowUpdate(currentSongName);
    }

    public void sendLockCancelButton(boolean unlocked) {
        listAdapter.receiveLockCancelDownloadButton(unlocked);
    }

    public void sendDownloadServiceStatus(YoutubePlayList playlist, String songName) {
        listAdapter.receiveDownloadServiceStatus(playlist, songName);
    }

    public void onAdapterAttached(PlaylistListAdapter adapter) {
        this.listAdapter = adapter;
    }

    public boolean isDownloadServiceRunning() {
        return BusinessLogicHelper.isServiceRunning(DownloadService.class.getName(), context);
    }

    public void onSyncAction(final MethodWrapper<Void> uiCallback,final MethodWrapper<Void> uiCallbackFail) {
        MethodWrapper<Void> afterSync = new MethodWrapper<Void>() {
            @Override
            public void execute(Void params) {
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.notifyDataSetChanged();
                        uiCallback.execute(null);
                    }
                });
            }
        };

        MethodWrapper<Void> afterSyncFailed = new MethodWrapper<Void>() {
            @Override
            public void execute(Void params) {
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        uiCallbackFail.execute(null);
                    }
                });
            }
        };

        DataBuilder.getInstance().syncLocalModelWithRemoteModel(afterSync,afterSyncFailed);
    }

    public void askForCurrentlyDownloadingPlaylist() {
        if (isDownloadServiceRunning()) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.setAction(DownloadService.DOWNLOAD_STATUS);
            context.startService(intent);
        }
    }

    public void notifyDataSetChanged(){
        listAdapter.notifyDataSetChanged();
        listAdapter.notifyDataSetInvalidated();
    }

    public void onDownloadStopped(){
        listAdapter.notifyDataSetChanged();
    }
}
