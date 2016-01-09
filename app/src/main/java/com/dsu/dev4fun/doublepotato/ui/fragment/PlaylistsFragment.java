package com.dsu.dev4fun.doublepotato.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.model.DownloadService;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.root.LocalModelRoot;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;
import com.dsu.dev4fun.doublepotato.ui.ButtonIcons;
import com.dsu.dev4fun.doublepotato.ui.adapters.PlaylistListAdapter;
import com.dsu.dev4fun.doublepotato.ui.controllers.PlaylistController;

public class PlaylistsFragment extends AbstractServiceFragment {
    private PlaylistController controller;
    private ListView playlistsView;
    private ProgressBar syncProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        setHasOptionsMenu(true);
        controller = new PlaylistController(getActivity());
        playlistsView = (ListView) view.findViewById(R.id.playlist_list);
        playlistsView.setAdapter(new PlaylistListAdapter(getActivity(), LocalModelRoot.getReadInstance().getPlayLists(), controller));
        syncProgress = (ProgressBar) view.findViewById(R.id.sync_progress);
        return view;
    }

    /**
     * Does an adapter refresh , in case we come from settings after a purge db
     */
    @Override
    public void onStart() {
        super.onStart();
        controller.notifyDataSetChanged();
    }

    /**
     * Action bar
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_playlists, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {

            BusinessLogicHelper.setButtonEnabled(getActivity(), item, false, ButtonIcons.SYNC_BUTTON);
            syncProgress.setVisibility(View.VISIBLE);

            MethodWrapper<Void> onSyncOk = new MethodWrapper<Void>() {
                @Override
                public void execute(Void params) {
                    syncProgress.setVisibility(View.GONE);
                    BusinessLogicHelper.setButtonEnabled(PlaylistsFragment.this.getActivity(), item, true, ButtonIcons.SYNC_BUTTON);
                }
            };

            MethodWrapper<Void> onSyncFail = new MethodWrapper<Void>() {
                @Override
                public void execute(Void params) {
                    syncProgress.setVisibility(View.GONE);
                    BusinessLogicHelper.setButtonEnabled(PlaylistsFragment.this.getActivity(), item, true, ButtonIcons.SYNC_BUTTON);
                    BusinessLogicHelper.showInfoDialog(PlaylistsFragment.this.getActivity(), "Sync failed", "Is the phone connected to the internet? Are the appID and channelID correct?");
                }
            };

            controller.onSyncAction(onSyncOk, onSyncFail);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * AbstractServiceFragment implementation
     */
    @Override
    public void addServiceReceiver() {
        final Handler mHandler = new Handler();

        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                /**progress updated*/
                if (intent.getAction().equals(DownloadService.OUT_PROGRESS_UPDATED_ACTION)) {
                    Float progress = intent.getFloatExtra(DownloadService.PROGRESS_EXTRA_KEY, 0);
                    controller.sendCurrentRowUpdate(progress);
                }

                /**progress new song download started*/
                if (intent.getAction().equals(DownloadService.OUT_DOWNLOADING_NEW_SONG_ACTION)) {
                    String newSongName = intent.getStringExtra(DownloadService.SONG_NAME_EXTRA_KEY);
                    int totalSongCount = intent.getIntExtra(DownloadService.PLAYLIST_COMPLETION_TOTAL_KEY, 0);
                    int downloadedSongCount = intent.getIntExtra(DownloadService.PLAYLIST_COMPLETION_DOWNLOADED_KEY, 0);

                    controller.sendCurrentRowUpdate(newSongName, new int[]{downloadedSongCount, totalSongCount});
                }

                if (intent.getAction().equals(DownloadService.OUT_CONVERSION_STARTED_ACTION)) {
                    String songName = intent.getStringExtra(DownloadService.SONG_NAME_EXTRA_KEY);
                    controller.sendCurrentRowUpdate(songName);
                    //controller.sendLockCancelButton(false);
                }

                if (intent.getAction().equals(DownloadService.OUT_CONVERSION_FINISHED_ACTION)) {
                    //controller.sendLockCancelButton(true);
                }

                if (intent.getAction().equals(DownloadService.OUT_CURRENTLY_DOWNLOADING_PLAYLIST_ACTION)) {
                    YoutubePlayList currentlyDownloadingPlaylist = intent.getParcelableExtra(DownloadService.CURRENTLY_DOWNLOADING_PLAYLIST_KEY);
                    String currentlyDownloadingSong = intent.getStringExtra(DownloadService.CURRENTLY_DOWNLOADING_SONGNAME_KEY);
                    controller.sendDownloadServiceStatus(currentlyDownloadingPlaylist, currentlyDownloadingSong);
                }

                if (intent.getAction().equals(DownloadService.OUT_OUT_OF_STORAGE_MEM_ACTION)) {
                    BusinessLogicHelper.showInfoDialog(PlaylistsFragment.this.getActivity(), "Download stopped", "Max allocated memory exceeded. It can be increased from settings");
                    controller.onDownloadStopped();
                }


            }
        };
        IntentFilter intentToReceiveFilter = new IntentFilter();
        intentToReceiveFilter.addAction(DownloadService.OUT_PROGRESS_UPDATED_ACTION);
        intentToReceiveFilter.addAction(DownloadService.OUT_DOWNLOADING_NEW_SONG_ACTION);
        intentToReceiveFilter.addAction(DownloadService.OUT_CONVERSION_STARTED_ACTION);
        intentToReceiveFilter.addAction(DownloadService.OUT_CONVERSION_FINISHED_ACTION);
        intentToReceiveFilter.addAction(DownloadService.OUT_CURRENTLY_DOWNLOADING_PLAYLIST_ACTION);
        intentToReceiveFilter.addAction(DownloadService.OUT_OUT_OF_STORAGE_MEM_ACTION);

        getActivity().registerReceiver(mIntentReceiver, intentToReceiveFilter, null, mHandler);
    }
}
