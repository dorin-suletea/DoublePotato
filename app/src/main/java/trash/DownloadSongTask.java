package trash;

import android.os.AsyncTask;
import android.util.Log;

import java.net.URLEncoder;

import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeSong;


public class DownloadSongTask extends AsyncTask<YoutubeSong, Void, Boolean> {
    private final static String KEEPVID_URL = "http://keepvid.com/";
    private final static String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    @Override
    protected Boolean doInBackground(YoutubeSong... params) {
        //String videoId = "J2X5mJ3HDYE";//NCS
        /*String videoId = "IGBnA19XC7c";
        int format = 18; // http://en.wikipedia.org/wiki/YouTube#Quality_and_codecs
        String encoding = "UTF-8";
        String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13";
        File outputDir = Environment.getExternalStorageDirectory();
        String extension = "mp4";
        File retFile = null;
        try {
            retFile = JavaYoutubeDownloader.doDownload(videoId, format, encoding, userAgent, outputDir, extension);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return retFile;
        */
        String url = "";
        for (YoutubeSong song : params) {
            url = KEEPVID_URL + "?url=";
            Log.d("ID", song.getId());
            url += URLEncoder.encode(YOUTUBE_URL + song.getId());
            //new WebYoutubeDownloader().fetchVideo(url, song.getName(), song.getId());

        }
        String http = "http://keepvid.com/?url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DJ2X5mJ3HDYE";
        //String param = "url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DA1iCBl0dOuI";
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        //Log.d("=!=",s.getAbsolutePath());
    }
}
