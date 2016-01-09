package trash;

import android.os.AsyncTask;

import com.dsu.dev4fun.doublepotato.model.meta.YoutubeLinker;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;

public class GetPlaylistsTask extends AsyncTask<YoutubeLinker, Void, String> {
    private MethodWrapper postExecute;

    public GetPlaylistsTask(MethodWrapper postExecute) {
        this.postExecute = postExecute;
    }

    @Override
    protected String doInBackground(YoutubeLinker... params) {
        YoutubeLinker linker = params[0];
        return null;
        //return linker.getPlaylists();
    }

    @Override
    protected void onPostExecute(String s) {
        postExecute.execute(s);
    }
}
