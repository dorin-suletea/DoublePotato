package trash;

import java.util.List;

import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;


public class PopulateRemoteModelTask {//extends AsyncTask<YoutubeLinker, Void, List<YoutubePlayList>> {
    private MethodWrapper<List<YoutubePlayList>> postExecute;
    /*
    public PopulateRemoteModelTask(MethodWrapper<List<YoutubePlayList>> postExecute) {
        this.postExecute = postExecute;
    }

    @Override
    protected List<YoutubePlayList> doInBackground(YoutubeLinker... params) {
        try {
            YoutubeLinker linker = params[0];
            String playListsString = linker.getPlaylists();
            //List<YoutubePlayList> playlists = YoutubeLinker.JSonHelper.getInstance().extractPlaylists(playListsString);

            //test
            //YoutubePlayList pl = playlists.get(0);
            //List<YoutubePlayList> pls = new ArrayList<>();
            //pls.add(pl);
            //playlists = pls;
            // test <----remove this
            List<YoutubeSong> pojoPlaylistSongs = new ArrayList<>();

            for (YoutubePlayList playlist : playlists) {
                List<String> songsJson = linker.getSongs(playlist.getId());


                List<String[]> playlistSongList = new ArrayList<>();
                for (String songBatch : songsJson) {
                    playlistSongList.addAll(YoutubeLinker.JSonHelper.getInstance().extractSongsInfo(songBatch));
                }

                for (String[] parsedSong : playlistSongList) {
                    pojoPlaylistSongs.add(new YoutubeSong(parsedSong[0], parsedSong[1], parsedSong[2]));
                }

                playlist.setSongs(pojoPlaylistSongs);
                playlistSongList.clear();
                pojoPlaylistSongs.clear();
            }
            return playlists;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(List<YoutubePlayList> s) {
        postExecute.execute(s);
    }

*/
}
