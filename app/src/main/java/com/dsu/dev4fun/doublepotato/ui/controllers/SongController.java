package com.dsu.dev4fun.doublepotato.ui.controllers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.dsu.dev4fun.doublepotato.media.MediaConstants;
import com.dsu.dev4fun.doublepotato.media.MediaPlayService;
import com.dsu.dev4fun.doublepotato.media.MediaPlayServiceProxy;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeSong;
import com.dsu.dev4fun.doublepotato.ui.adapters.SongListsAdapter;

public class SongController {
    private Activity context;
    private SongListsAdapter listViewAdapter;

    /**
     * Info about current playlist. This is a local clone, changes done to this playlist (eg shuffling) are only reflected in controller's context
     */
    private YoutubePlayList playList;


    public SongController(Activity context, YoutubePlayList playlist) {
        this.context = context;

        // media player is playing current playlist, update the UI from media play service
        if (MediaPlayServiceProxy.getInstance().getCurrentlyPlayingPlaylist() != null && playlist.getId().equals(MediaPlayServiceProxy.getInstance().getCurrentlyPlayingPlaylist().getId())) {
            playList = MediaPlayServiceProxy.getInstance().getCurrentlyPlayingPlaylist();
            requestServiceState();
        } else {
            playList = new YoutubePlayList(playlist);
        }
    }

    public void setMediaServicePlayQueue() {
        MediaPlayServiceProxy.getInstance().setPlaylist(playList);
    }



    public void onPlayNext() {
        MediaPlayServiceProxy.getInstance().playNext();
    }

    public void onPlayPrevious() {
        MediaPlayServiceProxy.getInstance().playPrevious();
    }

    public void onPause() {
        MediaPlayServiceProxy.getInstance().pause();
    }

    public void onResume() {
        MediaPlayServiceProxy.getInstance().resume();
    }

    public void onPlaySong(String songID) {
        MediaPlayServiceProxy.getInstance().playSong(songID);
    }

    /**
     * Returns true if the media service is running and the current playing playlist is the same
     * as the playlist contained in this view.
     *
     * Do not use in constructor !!! of SongController
     *
     * @return
     */
    public boolean isServicePlayingThisPlaylist() {
        boolean serviceRunning = !MediaPlayServiceProxy.getInstance().isStopped();
        return serviceRunning &&
                MediaPlayServiceProxy.getInstance().getCurrentlyPlayingPlaylist() != null &&
                this.playList.getId().equals(MediaPlayServiceProxy.getInstance().getCurrentlyPlayingPlaylist().getId());
    }

    public MediaConstants.MediaServiceState getServiceState(){
        return MediaPlayServiceProxy.getInstance().getServiceState();
    }

    public void sendCurrentRowUpdate(String currentItemID) {
        listViewAdapter.receiveCurrentlyPlayingUpdate(currentItemID);
    }

    public View.OnClickListener getRowClickListener(final String songId) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMediaServicePlayQueue();
                onPlaySong(songId);
            }
        };
    }

    public void onAdapterAttached(SongListsAdapter adapter) {
        this.listViewAdapter = adapter;
    }

    public boolean getShuffle() {
        return playList.isShuffled();
    }

    public void switchShuffle() {
        boolean currentlyPlaying = listViewAdapter.getCurrentlyPlayingSong() != null;

        if (!playList.isShuffled()) {
            //this must not be shuffled, it is already playing, and the ui should set it as the first
            //make currently playing as the first position

            if (currentlyPlaying) {
                //if there is a currently playing item

                YoutubeSong currentSong = playList.getSongByID(listViewAdapter.getCurrentlyPlayingSong());
                playList.shuffle();
                playList.moveSongToFront(currentSong.getId());
                setMediaServicePlayQueue();
            } else {
                //no currently playing, just shuffle
                playList.shuffle();
            }

        } else {
            if (currentlyPlaying) {
                playList.unShuffle();// = new YoutubePlayList(LocalModelRoot.getReadInstance().getPlaylistByID(playList.getId()));
                setMediaServicePlayQueue();
            } else {
                playList.unShuffle();
            }
        }
        listViewAdapter.setData(playList.getPlayableSongList());
        listViewAdapter.notifyDataSetChanged();
        listViewAdapter.notifyDataSetInvalidated();

    }


    public YoutubePlayList getPlaylist() {
        return playList;
    }

    public void requestServiceState() {
        Intent playNext = new Intent(context, MediaPlayService.class);
        playNext.setAction(MediaPlayService.REQ_SERVICE_STATE_ACTION);
        context.startService(playNext);
        Log.d("=!=","req state");
    }
}


/**
 * public void sendMediaServiceState(YoutubePlayList playlistFromService) {
 * <p>
 * if (playList.getPlayableSongList().size() != playlistFromService.getPlayableSongList().size()) {
 * shufflePlaylist = false;
 * } else {
 * boolean sameOrder = true;
 * for (int i = 0; i < playList.getPlayableSongList().size(); i++) {
 * if (!playList.getPlayableSongList().get(i).getId().equals(playlistFromService.getPlayableSongList().get(i).getId())) {
 * sameOrder = false;
 * break;
 * }
 * }
 * shufflePlaylist = !sameOrder;
 * }
 * <p>
 * context.invalidateOptionsMenu();
 * playList = new YoutubePlayList(playlistFromService);
 * listViewAdapter.setData(playList.getPlayableSongList());
 * listViewAdapter.notifyDataSetChanged();
 * listViewAdapter.notifyDataSetInvalidated();
 * <p>
 * }
 * <p>
 * Service binding
 * <p>
 * private ServiceConnection makeServiceConnection() {
 * return new ServiceConnection() {
 * public void onServiceConnected(ComponentName className, IBinder binder) {
 * mediaPlayerService = ((MediaPlayService.MediaPlayerServiceBinder) binder).getService();
 * Log.d("ServiceConnection", "connected");
 * }
 * <p>
 * public void onServiceDisconnected(ComponentName className) {
 * mediaPlayerService = null;
 * }
 * };
 * }
 * <p>
 * <p>
 * private Handler makeHandler() {
 * return new Handler() {
 * public void handleMessage(Message message) {
 * Bundle data = message.getData();
 * }
 * };
 * }
 * <p>
 * public void doBindService() {
 * if (mediaPlayerConnection == null) {
 * mediaPlayerConnection = makeServiceConnection();
 * Intent intent = new Intent(context, MediaPlayService.class);
 * Messenger messenger = new Messenger(makeHandler());
 * intent.putExtra(MediaPlayService.HOOK_MESSENGER_KEY, messenger);
 * <p>
 * context.bindService(intent, mediaPlayerConnection, Context.BIND_AUTO_CREATE);
 * }
 * }
 * <p>
 * public void doUnbindService() {
 * if (mediaPlayerConnection != null) {
 * context.unbindService(mediaPlayerConnection);
 * }
 * }
 * <p>
 * Service binding
 * <p>
 * private ServiceConnection makeServiceConnection() {
 * return new ServiceConnection() {
 * public void onServiceConnected(ComponentName className, IBinder binder) {
 * mediaPlayerService = ((MediaPlayService.MediaPlayerServiceBinder) binder).getService();
 * Log.d("ServiceConnection", "connected");
 * }
 * <p>
 * public void onServiceDisconnected(ComponentName className) {
 * mediaPlayerService = null;
 * }
 * };
 * }
 * <p>
 * <p>
 * private Handler makeHandler() {
 * return new Handler() {
 * public void handleMessage(Message message) {
 * Bundle data = message.getData();
 * }
 * };
 * }
 * <p>
 * public void doBindService() {
 * if (mediaPlayerConnection == null) {
 * mediaPlayerConnection = makeServiceConnection();
 * Intent intent = new Intent(context, MediaPlayService.class);
 * Messenger messenger = new Messenger(makeHandler());
 * intent.putExtra(MediaPlayService.HOOK_MESSENGER_KEY, messenger);
 * <p>
 * context.bindService(intent, mediaPlayerConnection, Context.BIND_AUTO_CREATE);
 * }
 * }
 * <p>
 * public void doUnbindService() {
 * if (mediaPlayerConnection != null) {
 * context.unbindService(mediaPlayerConnection);
 * }
 * }
 * <p>
 * Service binding
 * <p>
 * private ServiceConnection makeServiceConnection() {
 * return new ServiceConnection() {
 * public void onServiceConnected(ComponentName className, IBinder binder) {
 * mediaPlayerService = ((MediaPlayService.MediaPlayerServiceBinder) binder).getService();
 * Log.d("ServiceConnection", "connected");
 * }
 * <p>
 * public void onServiceDisconnected(ComponentName className) {
 * mediaPlayerService = null;
 * }
 * };
 * }
 * <p>
 * <p>
 * private Handler makeHandler() {
 * return new Handler() {
 * public void handleMessage(Message message) {
 * Bundle data = message.getData();
 * }
 * };
 * }
 * <p>
 * public void doBindService() {
 * if (mediaPlayerConnection == null) {
 * mediaPlayerConnection = makeServiceConnection();
 * Intent intent = new Intent(context, MediaPlayService.class);
 * Messenger messenger = new Messenger(makeHandler());
 * intent.putExtra(MediaPlayService.HOOK_MESSENGER_KEY, messenger);
 * <p>
 * context.bindService(intent, mediaPlayerConnection, Context.BIND_AUTO_CREATE);
 * }
 * }
 * <p>
 * public void doUnbindService() {
 * if (mediaPlayerConnection != null) {
 * context.unbindService(mediaPlayerConnection);
 * }
 * }
 */


/**
 * Service binding
 * <p/>
 * private ServiceConnection makeServiceConnection() {
 * return new ServiceConnection() {
 * public void onServiceConnected(ComponentName className, IBinder binder) {
 * mediaPlayerService = ((MediaPlayService.MediaPlayerServiceBinder) binder).getService();
 * Log.d("ServiceConnection", "connected");
 * }
 * <p/>
 * public void onServiceDisconnected(ComponentName className) {
 * mediaPlayerService = null;
 * }
 * };
 * }
 * <p/>
 * <p/>
 * private Handler makeHandler() {
 * return new Handler() {
 * public void handleMessage(Message message) {
 * Bundle data = message.getData();
 * }
 * };
 * }
 * <p/>
 * public void doBindService() {
 * if (mediaPlayerConnection == null) {
 * mediaPlayerConnection = makeServiceConnection();
 * Intent intent = new Intent(context, MediaPlayService.class);
 * Messenger messenger = new Messenger(makeHandler());
 * intent.putExtra(MediaPlayService.HOOK_MESSENGER_KEY, messenger);
 * <p/>
 * context.bindService(intent, mediaPlayerConnection, Context.BIND_AUTO_CREATE);
 * }
 * }
 * <p/>
 * public void doUnbindService() {
 * if (mediaPlayerConnection != null) {
 * context.unbindService(mediaPlayerConnection);
 * }
 * }
 */
