package trash;


import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModelRoots {
    protected List<YoutubePlayList> playLists;

    protected AbstractModelRoots(){
        playLists= new ArrayList<>();
    }

    public YoutubePlayList getPlaylistByID(String searchedID) {
        for (YoutubePlayList dbPlaylist : playLists) {
            if (dbPlaylist.getId().equals(searchedID)) {
                return dbPlaylist;
            }
        }
        return null;
    }

    public List<YoutubePlayList> getPlayLists() {
        return playLists;
    }

    protected void setPlaylists(List<YoutubePlayList> playlists) {
        this.playLists.clear();
        this.playLists.addAll(playlists);
    }

}
