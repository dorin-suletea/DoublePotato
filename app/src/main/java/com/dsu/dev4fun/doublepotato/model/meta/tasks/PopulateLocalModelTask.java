package com.dsu.dev4fun.doublepotato.model.meta.tasks;

import android.os.AsyncTask;

import com.dsu.dev4fun.doublepotato.model.meta.pojo.root.LocalModelRoot;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.meta.DatabaseLinker;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;

import java.util.List;


public class PopulateLocalModelTask extends AsyncTask<Void, Void, Void> {
    private DatabaseLinker linker;
    private MethodWrapper<Void> postExecute;

    public PopulateLocalModelTask(DatabaseLinker dbLinker, MethodWrapper postExecute) {
        linker = dbLinker;
        this.postExecute = postExecute;
    }

    @Override
    protected Void doInBackground(Void[] params) {
        List<YoutubePlayList> savedPlaylists = linker.loadAllPlaylists();
        LocalModelRoot.getWriteInstance().initialize(savedPlaylists);
        return null;
    }

    protected void onPostExecute(Void s) {
        postExecute.execute(null);
    }
}
