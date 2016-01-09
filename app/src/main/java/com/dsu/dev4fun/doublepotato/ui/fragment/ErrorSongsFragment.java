package com.dsu.dev4fun.doublepotato.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.ui.adapters.ErrorSongListAdapter;
import com.dsu.dev4fun.doublepotato.ui.controllers.ErrorSongController;


public class ErrorSongsFragment extends AbstractToolbarFragment {
    public static final String PLAYLIST_KEY = "PLAYLIST_BUNDLE_KEY";
    private ListView songsListView;
    private ErrorSongController controller;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_error_songs, container, false);

        YoutubePlayList playlist = (YoutubePlayList) getArguments().get(PLAYLIST_KEY);
        controller = new ErrorSongController(getActivity(), playlist);

        songsListView = (ListView) view.findViewById(R.id.error_songs_list);
        songsListView.setAdapter(new ErrorSongListAdapter(this.getActivity(), controller.getPlaylist().getSongsWithError()));


        setHasOptionsMenu(true);
        return view;
    }
}
