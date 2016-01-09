package trash;


import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;

import java.util.List;

public class RemoteModelRootx extends AbstractModelRoots {
    private static RemoteModelRootx instance = new RemoteModelRootx();

    public static RemoteModelRootx getInstance() {
        return instance;
    }

    private RemoteModelRootx() {
        super();
        //state = ModelState.NEW;
    }

    public void initialize(List<YoutubePlayList> playListList) {
        setPlaylists(playListList);
       // state = ModelState.INITIALIZED;
    }

    public void synkWithRemote() throws Exception{
        //if (RemoteModelRoot.getInstance().getState()!=ModelState.INITIALIZED){
        //    throw new Exception("Remote model not initialized");
        //}
        //setPlaylists(RemoteModelRoot.getInstance().getPlayLists());
    }

}
