package com.dsu.dev4fun.doublepotato.ui.fragment;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.ListView;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.media.MediaConstants;
import com.dsu.dev4fun.doublepotato.media.MediaPlayService;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;
import com.dsu.dev4fun.doublepotato.ui.adapters.SongListsAdapter;
import com.dsu.dev4fun.doublepotato.ui.controllers.SongController;


public class SongsFragment extends AbstractServiceFragment {
    public static final String PLAYLIST_KEY = "PLAYLIST_BUNDLE_KEY";
    private ListView songsListView;


    private SongController controller;

    private Button previousSongBtn;
    private Button pauseButton;
    private Button resumeButton;
    private Button nextSongBtn;
    private View mediaButtonsView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);

        YoutubePlayList playlist = (YoutubePlayList) getArguments().get(PLAYLIST_KEY);
        controller = new SongController(getActivity(), playlist);
        //set media play service play queue == playlist items


        songsListView = (ListView) view.findViewById(R.id.songs_list);
        songsListView.setAdapter(new SongListsAdapter(this.getActivity(), controller.getPlaylist().getPlayableSongList(), controller, songsListView));

        previousSongBtn = (Button) view.findViewById(R.id.previous_song_btn);
        pauseButton = (Button) view.findViewById(R.id.pause_btn);
        resumeButton = (Button) view.findViewById(R.id.resume_btn);
        nextSongBtn = (Button) view.findViewById(R.id.next_song_btn);
        mediaButtonsView = view.findViewById(R.id.media_button_view);

        setHasOptionsMenu(true);
        addListeners();
        mediaButtonsView.setVisibility(controller.isServicePlayingThisPlaylist() ? View.VISIBLE : View.INVISIBLE);
        return view;
    }

    private void addListeners() {
        nextSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onPlayNext();
            }
        });

        previousSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onPlayPrevious();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onPause();
            }
        });

        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onResume();
            }
        });
    }

    private void updateMediaPlaybackBtnState(int currentSongIndex, int playlistSize) {
        boolean hasPrevious = currentSongIndex >= 1;
        boolean hasNext = currentSongIndex < playlistSize - 1;
        previousSongBtn.setEnabled(hasPrevious);
        nextSongBtn.setEnabled(hasNext);

        //visually disable them
        previousSongBtn.setAlpha(hasPrevious ? 1f : 0.3f);
        nextSongBtn.setAlpha(hasNext ? 1f : 0.3f);

        if (MediaConstants.MediaServiceState.PAUSED.equals(controller.getServiceState())) {
            resumeButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        }
        if (MediaConstants.MediaServiceState.STARTED.equals(controller.getServiceState())) {
            resumeButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Action bar
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);


        inflater.inflate(R.menu.menu_songs, menu);
        menu.findItem(R.id.action_shuffle_toggle).setIcon(controller.getShuffle() ? getResources().getDrawable(R.drawable.shuffle_toggle_down) : getResources().getDrawable(R.drawable.shuffle_toggle_up));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_shuffle_toggle) {
            controller.switchShuffle();
            item.setIcon(controller.getShuffle() ? getResources().getDrawable(R.drawable.shuffle_toggle_down) : getResources().getDrawable(R.drawable.shuffle_toggle_up));
            return true;
        }
        if (id == R.id.action_playlistErrors){
            showErrorFragment();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showErrorFragment(){
        Bundle params = new Bundle();
        params.putParcelable(ErrorSongsFragment.PLAYLIST_KEY, controller.getPlaylist());
        BusinessLogicHelper.replaceCurrentFragment((getActivity()), params, new ErrorSongsFragment());
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

                if (intent.getAction().equals(MediaPlayService.PLAY_STATE_OUT_ACTION)) {
                    int currentSongIndex = intent.getIntExtra(MediaPlayService.PLAY_STATE_SONG_INDEX_KEY, 0);
                    String currentSongID = intent.getStringExtra(MediaPlayService.PLAY_STATE_SONG_ID_KEY);
                    Log.d("=!=", "Received broadcast " + currentSongID);
                    int totalSize = intent.getIntExtra(MediaPlayService.PLAY_STATE_QUEUE_SIZE_KEY, 0);
                    updateMediaPlaybackBtnState(currentSongIndex, totalSize);
                    controller.sendCurrentRowUpdate(currentSongID);
                }

                if (intent.getAction().equals(MediaPlayService.SERVICE_STATE_ACTION)) {
                    MediaConstants.MediaServiceState state = MediaConstants.MediaServiceState.values()[intent.getIntExtra(MediaPlayService.SERVICE_STATE_KEY, 0)];

                    switch (state) {
                        case STARTED:
                            mediaButtonsView.setVisibility(View.VISIBLE);
                            pauseButton.setVisibility(View.VISIBLE);
                            resumeButton.setVisibility(View.GONE);
                            break;
                        case STOPPED:
                            mediaButtonsView.setVisibility(View.INVISIBLE);
                            break;
                        case PAUSED:
                            pauseButton.setVisibility(View.GONE);
                            resumeButton.setVisibility(View.VISIBLE);
                            break;
                    }

                }

            }
        };

        IntentFilter intentToReceiveFilter = new IntentFilter();
        intentToReceiveFilter.addAction(MediaPlayService.PLAY_STATE_OUT_ACTION);
        intentToReceiveFilter.addAction(MediaPlayService.SERVICE_STATE_ACTION);
        getActivity().registerReceiver(mIntentReceiver, intentToReceiveFilter, null, mHandler);
    }
}
