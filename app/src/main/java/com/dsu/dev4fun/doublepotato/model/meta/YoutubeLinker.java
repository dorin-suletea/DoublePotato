package com.dsu.dev4fun.doublepotato.model.meta;

import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.util.ConnectionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YoutubeLinker {
    private final String applicationKey;
    private final String channelID;
    private final JSonHelper jsonHelper;

    public YoutubeLinker(final String appKey, final String chanID) {
        applicationKey = appKey;
        channelID = chanID;
        jsonHelper = new JSonHelper();
    }

    public String getPlaylists() throws ConnectionException {
        //https://www.googleapis.com/youtube/v3/playlists?part=snippet&channelId=UCHphW02wxlMKQGPJBkIei0w&key=AIzaSyB_TSE4ZG_9WFYCilHjWGAP1xYn9KwygA0
        String playlistURLString = "https://www.googleapis.com/youtube/v3/playlists?part=snippet&channelId=" +
                channelID +
                "&key=" + applicationKey;

        //this bellow partially returns  the playlists (do not use)
        //String playlistURLString = "https://www.googleapis.com/youtube/v3/search?part=id%2C+snippet&channelId=" +channelID + "&maxResults=50&order=date&type=video%2C+playlist&key=" +applicationKey;
        return executeURL(playlistURLString);
    }


    public List<String> getSongs(String playlistID) throws ConnectionException {
        List<String> playlistSongsJson = new ArrayList<>();
        try {
            String songsURLString = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + playlistID + "&key=" + applicationKey;
            String batchInfo = executeURL(songsURLString);
            playlistSongsJson.add(batchInfo);
            String nextPageToken = jsonHelper.extractNextPageToken(batchInfo);

            while (!nextPageToken.isEmpty()) {
                String batchUrl = songsURLString + "&pageToken=" + nextPageToken;
                batchInfo = executeURL(batchUrl);
                playlistSongsJson.add(batchInfo);
                nextPageToken = jsonHelper.extractNextPageToken(batchInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return playlistSongsJson;
    }

    public String getSongMetadata(String songID) throws ConnectionException {
        String durationURLString = "https://www.googleapis.com/youtube/v3/videos?id=" + songID + "&part=contentDetails&key=" + applicationKey;
        return executeURL(durationURLString);
    }

    private String executeURL(String url) throws ConnectionException {
        HttpURLConnection conn = null;
        String ret = "";
        try {
            URL playlistURL = new URL(url);
            conn = (HttpURLConnection) playlistURL.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            //conn.setRequestProperty("Accept", "application/json");
            conn.connect();


            if (conn.getResponseCode() != 200) {
                throw new ConnectionException("Failed get playlists", conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            while ((output = br.readLine()) != null) {
                ret += output;
            }

        } catch (IOException e) {

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return ret;
    }

    public JSonHelper getJsonHelper() {
        return jsonHelper;
    }

    public static class JSonHelper {

        public List<YoutubePlayList> extractPlaylists(String rawJson) throws JSONException {
            List<YoutubePlayList> playLists = new ArrayList<>();
            JSONObject obj = new JSONObject(rawJson);
            JSONArray arr = obj.getJSONArray("items");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                JSONObject snippet = item.getJSONObject("snippet");
                String playlistName = snippet.getString("title");

                String playlistId = item.getString("id");

                playLists.add(new YoutubePlayList(playlistId, playlistName));
            }
            return playLists;
        }

        public String extractNextPageToken(String rawJson) throws JSONException {
            JSONObject obj = new JSONObject(rawJson);
            if (!obj.has("nextPageToken")) {
                return "";
            }
            String nextPage = obj.getString("nextPageToken");
            return nextPage;
        }

        public String extractSongDuration(String rawJson) throws JSONException {
            JSONObject root = new JSONObject(rawJson);
            JSONArray arr = root.getJSONArray("items");
            if (arr.length() == 0) {
                //deleted video
                return null;
            }

            JSONObject dataObject = arr.getJSONObject(0);

            String duration = dataObject.getJSONObject("contentDetails").getString("duration");
            return duration;
        }

        public List<String[]> extractSongsInfo(String rawJson) throws JSONException {
            List<String[]> ret = new ArrayList<>();
            String songID = "";
            String songName = "";
            String songThumbnailURL = "";

            JSONObject obj = new JSONObject(rawJson);
            JSONArray arr = obj.getJSONArray("items");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);

                JSONObject snippet = item.getJSONObject("snippet");
                songName = snippet.getString("title");

                JSONObject resourceObject = snippet.getJSONObject("resourceId");
                songID = resourceObject.getString("videoId");

                if (snippet.has("thumbnails")) {
                    JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                    JSONObject defaultThumbnail = thumbnails.getJSONObject("default");
                    songThumbnailURL = defaultThumbnail.getString("url");
                } else {
                    songThumbnailURL = "DeletedVideo";
                }

                ret.add(new String[]{songID, songName, songThumbnailURL});
            }
            return ret;
        }
    }

    public long youtubeDurationToSec(String youtubeTime) {
        String time = youtubeTime.substring(2);
        long duration = 0L;
        Object[][] indexs = new Object[][]{{"H", 3600}, {"M", 60}, {"S", 1}};
        for (int i = 0; i < indexs.length; i++) {
            int index = time.indexOf((String) indexs[i][0]);
            if (index != -1) {
                String value = time.substring(0, index);
                duration += Integer.parseInt(value) * (int) indexs[i][1] * 1000;
                time = time.substring(value.length() + 1);
            }
        }
        return duration / 1000;
    }
}
