package com.dsu.dev4fun.doublepotato.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeSong;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;

import java.util.List;


public class ErrorSongListAdapter extends ArrayAdapter<YoutubeSong> {
    private List<YoutubeSong> shownItems;
    private LayoutInflater inflater;

    public ErrorSongListAdapter(Context context, List<YoutubeSong> objects) {
        super(context, 0, objects);
        shownItems = objects;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        YoutubeSong currentSong = shownItems.get(position);
        View rowView = inflater.inflate(R.layout.row_song_with_error, parent, false);

        TextView songNameView = (TextView) rowView.findViewById(R.id.song_with_error_name);
        songNameView.setText(currentSong.getName());

        TextView songErrorView = (TextView) rowView.findViewById(R.id.error_description);
        songErrorView.setText(currentSong.getError().toString());

        return rowView;
    }

}
