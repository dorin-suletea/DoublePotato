package com.dsu.dev4fun.doublepotato.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeSong;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;
import com.dsu.dev4fun.doublepotato.ui.controllers.SongController;

import java.util.List;

public class SongListsAdapter extends ArrayAdapter<YoutubeSong> {
    private List<YoutubeSong> shownItems;
    private LayoutInflater inflater;
    private SongController controller;
    private String currentlyPlayingSong;
    private ListView listView;

    public SongListsAdapter(Context context, List<YoutubeSong> objects, SongController controller, ListView listView) {
        super(context, 0, objects);
        shownItems = objects;
        this.controller = controller;
        this.listView = listView;
        controller.onAdapterAttached(this);
        currentlyPlayingSong = null;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        YoutubeSong currentSong = shownItems.get(position);

        //TODO attach to root, what means
        View rowView = inflater.inflate(R.layout.row_song, parent, false);

        TextView songNameView = (TextView) rowView.findViewById(R.id.song_name);
        songNameView.setText(currentSong.getName());

        TextView songDuration = (TextView) rowView.findViewById(R.id.song_duration);
        songDuration.setText(BusinessLogicHelper.convertToDisplayableTime(currentSong.getDuration()));

        if (currentSong.getId().equals(currentlyPlayingSong)) {
            //this view represents the currently playing song
            rowView.setBackgroundColor(Color.GRAY);
        }
        rowView.setOnClickListener(controller.getRowClickListener(currentSong.getId()));

        return rowView;
    }

    public void setData(List<YoutubeSong> newSongs) {
        shownItems.clear();
        shownItems.addAll(newSongs);
    }

    public void receiveCurrentlyPlayingUpdate(String currentlyPlayingSong) {
        this.currentlyPlayingSong = currentlyPlayingSong;
        notifyDataSetChanged();
        scrollToCurrentlyPlaying();
    }

    private void scrollToCurrentlyPlaying(){
        //TODO optimize this
        //this scrolls the list to currently playing item
        int last = listView.getLastVisiblePosition();
        int first = listView.getFirstVisiblePosition();

        for (int i = 0; i < shownItems.size(); i++) {
            if (shownItems.get(i).getId().equals(currentlyPlayingSong)) {
                if (i>last || i<first) {
                    //only scroll if the item is not visible on the screen
                    listView.setSelection(i);
                }
                break;
            }
        }
    }

    public String getCurrentlyPlayingSong() {
        return currentlyPlayingSong;
    }
}
