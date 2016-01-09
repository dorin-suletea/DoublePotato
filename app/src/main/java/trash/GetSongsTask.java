package trash;


import android.os.AsyncTask;
import android.util.Pair;

import java.util.List;

import com.dsu.dev4fun.doublepotato.model.meta.YoutubeLinker;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;

public class GetSongsTask extends AsyncTask<Pair<YoutubeLinker, String>, Void, Pair<List<String>, YoutubePlayList>> {
    private MethodWrapper postExecute;
    private YoutubePlayList playlist;

    public GetSongsTask(MethodWrapper postExecute, YoutubePlayList retPlaylist) {
        this.postExecute = postExecute;
        playlist = retPlaylist;
    }

    @Override
    protected Pair<List<String>, YoutubePlayList> doInBackground(Pair<YoutubeLinker, String>... params) {
        return null;
    }

    @Override
    protected void onPostExecute(Pair<List<String>, YoutubePlayList> s) {
        postExecute.execute(s);
    }
}
