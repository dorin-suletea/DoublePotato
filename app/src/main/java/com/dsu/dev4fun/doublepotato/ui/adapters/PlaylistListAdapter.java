package com.dsu.dev4fun.doublepotato.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;
import com.dsu.dev4fun.doublepotato.ui.ButtonIcons;
import com.dsu.dev4fun.doublepotato.ui.CompositeOnClickListener;
import com.dsu.dev4fun.doublepotato.ui.controllers.PlaylistController;

import java.util.List;

public class PlaylistListAdapter extends ArrayAdapter<YoutubePlayList> {
    private List<YoutubePlayList> shownItems;
    private LayoutInflater inflater;
    private PlaylistController controller;
    private View currentDownloadView;

    private YoutubePlayList currentlyDownloadingAUX;
    private String currentlyDownloadingSongNameAUX;

    public PlaylistListAdapter(Context context, List<YoutubePlayList> objects, PlaylistController fragmentController) {
        super(context, 0, objects);
        shownItems = objects;
        controller = fragmentController;
        controller.onAdapterAttached(this);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        controller.askForCurrentlyDownloadingPlaylist();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        YoutubePlayList currentPlaylist = shownItems.get(position);


        View rowView;
        if (convertView == null) {
            rowView = inflater.inflate(R.layout.row_playlist, parent, false);
        } else {
            rowView = convertView;
        }

        rowView.setOnClickListener(controller.onPlaylistClick(currentPlaylist));

        /**Button listeners**/
        Button downloadButton = (Button) rowView.findViewById(R.id.download_playlist);
        CompositeOnClickListener downloadClickListenerComposite = new CompositeOnClickListener();
        //store selected view
        downloadClickListenerComposite.addOnClickListener(downloadStartListener(rowView));
        //trigger download service
        downloadClickListenerComposite.addOnClickListener(controller.getDownloadClickListener(currentPlaylist));
        //update button states
        downloadClickListenerComposite.addOnClickListener(getButtonStateChangeListener());

        downloadButton.setOnClickListener(downloadClickListenerComposite);

        Button cancelDownloadButton = (Button) rowView.findViewById(R.id.cancel_download_playlist);
        CompositeOnClickListener downloadCancelClickListenerComposite = new CompositeOnClickListener();
        //stop the download service
        downloadCancelClickListenerComposite.addOnClickListener(controller.getDownloadCancelClickListener());
        //update button states
        downloadCancelClickListenerComposite.addOnClickListener(getButtonStateChangeListener());
        //delete the stored view
        downloadCancelClickListenerComposite.addOnClickListener(downloadStopListener());
        cancelDownloadButton.setOnClickListener(downloadCancelClickListenerComposite);

        /**Info labels**/
        TextView playlistNameView = (TextView) rowView.findViewById(R.id.playlist_name);
        playlistNameView.setText(currentPlaylist.getName());

        TextView playlistDownloadedSongsLabel = (TextView) rowView.findViewById(R.id.playlist_download_completion_status);
        playlistDownloadedSongsLabel.setText(BusinessLogicHelper.makePlaylistDownloadedLabelText(currentPlaylist.getDownloadedSongsCount(), currentPlaylist.getSongs().size()));


        /**Button states**/
        if (controller.isDownloadServiceRunning()) {
            //if this if passes, the list was recreated while download in progress, update the list to show
            //the service state (current view in getView is the current view)
            if (currentDownloadView == null && currentlyDownloadingAUX != null && currentlyDownloadingAUX.equals(currentPlaylist)) {
                currentDownloadView = rowView;
                receiveRowUpdate(currentlyDownloadingSongNameAUX);

                currentlyDownloadingSongNameAUX = null;
                currentlyDownloadingAUX = null;

            }
            //service running, disable everything, we will update the currentDownloadingView later
            BusinessLogicHelper.setButtonEnabled(this.getContext(), downloadButton, false, ButtonIcons.DOWNLOAD_BUTTON);
            cancelDownloadButton.setEnabled(true);


        } else {
            //service not running, enable all
            BusinessLogicHelper.setButtonEnabled(this.getContext(), downloadButton, true, ButtonIcons.DOWNLOAD_BUTTON);
            cancelDownloadButton.setEnabled(false);
        }


        if (currentDownloadView != null) {
            Button currentViewCancelDownloadButton = (Button) currentDownloadView.findViewById(R.id.cancel_download_playlist);
            Button currentViewDownloadButton = (Button) currentDownloadView.findViewById(R.id.download_playlist);
            TextView currentDownloadSong = (TextView) currentDownloadView.findViewById(R.id.playlist_download_current_song);
            ProgressBar downloadProgress = (ProgressBar) currentDownloadView.findViewById(R.id.playlist_download_progress);


            if (controller.isDownloadServiceRunning()) {
                currentViewCancelDownloadButton.setVisibility(View.VISIBLE);
                currentViewDownloadButton.setVisibility(View.GONE);

                currentDownloadSong.setVisibility(View.VISIBLE);
                downloadProgress.setVisibility(View.VISIBLE);

            } else {
                currentViewCancelDownloadButton.setVisibility(View.GONE);
                currentViewDownloadButton.setVisibility(View.VISIBLE);

                currentDownloadSong.setVisibility(View.GONE);
                downloadProgress.setVisibility(View.GONE);
            }
        }
        return rowView;
    }

    private View.OnClickListener getButtonStateChangeListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyDataSetChanged();

            }
        };
    }

    private View.OnClickListener downloadStartListener(final View clickedFromView) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDownloadView = clickedFromView;
            }
        };

    }

    private View.OnClickListener downloadStopListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyDataSetChanged();
                notifyDataSetInvalidated();
            }
        };
    }

    public void receiveRowUpdate(float progress) {
        if (currentDownloadView == null) {
            //download was canceled
            return;
        }
        //setting progress
        ProgressBar progressBar = (ProgressBar) currentDownloadView.findViewById(R.id.playlist_download_progress);
        progressBar.setProgress((int) (progress * 100));
    }

    public void receiveRowUpdate(String songName) {
        if (currentDownloadView == null) {
            //download was canceled
            return;
        }
        //setting progress
        TextView currentSongNameText = (TextView) currentDownloadView.findViewById(R.id.playlist_download_current_song);
        currentSongNameText.setText(songName);
    }

    public void receiveRowUpdate(String currentSongName, int[] songDownloadedAndTotalCount) {
        if (currentDownloadView == null) {
            //download was canceled
            return;
        }
        //setting current song name
        TextView currentSongNameText = (TextView) currentDownloadView.findViewById(R.id.playlist_download_current_song);
        currentSongNameText.setText(currentSongName);

        //setting playlist global progress
        TextView playlistDownloadedSongsLabel = (TextView) currentDownloadView.findViewById(R.id.playlist_download_completion_status);
        playlistDownloadedSongsLabel.setText(BusinessLogicHelper.makePlaylistDownloadedLabelText(songDownloadedAndTotalCount[0], songDownloadedAndTotalCount[1]));
        Log.d("=!=", "Receiving row update" + songDownloadedAndTotalCount[0]);
    }

    public void receiveLockCancelDownloadButton(boolean unlocked) {
        if (currentDownloadView == null) {
            return;
        }

        Button cancelDownload = (Button) currentDownloadView.findViewById(R.id.cancel_download_playlist);
        cancelDownload.setEnabled(unlocked);
    }

    /**
     * If this list is recreated while the download service is running,
     * we must feed the currently downloading playlist from there
     * so we can restore the current view
     */
    public void receiveDownloadServiceStatus(YoutubePlayList currentlyDownloadingPlaylist, String currentlyDownloadedSong) {
        this.currentlyDownloadingAUX = currentlyDownloadingPlaylist;
        this.currentlyDownloadingSongNameAUX = currentlyDownloadedSong;
        notifyDataSetChanged();
    }

}
