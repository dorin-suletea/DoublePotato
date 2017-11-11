package com.dsu.dev4fun.doublepotato.model.meta;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeError;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.root.LocalModelRoot;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubePlayList;
import com.dsu.dev4fun.doublepotato.model.meta.pojo.YoutubeSong;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseLinker extends SQLiteOpenHelper {
    public static final String DELETED_VIDEO_NAME_TAG = "Deleted video";

    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "doublePotatoSongs";

    private static final String TABLE_PLAYLISTS = "playlists";
    private static final String TABLE_SONGS = "songs";

    private static final String KEY_PLAYLISTS_PLAYLISTID = "playlist_id";
    private static final String KEY_PLAYLISTS_NAME = "playlist_name";


    private static final String KEY_SONGS_SONGID = "song_id";
    private static final String KEY_SONGS_NAME = "song_name";
    private static final String KEY_SONG_DURATION = "song_duration";
    private static final String KEY_SONG_ERROR = "song_error";
    private static final String KEY_SONGS_SONG_FILE = "song_file";
    private static final String KEY_SONGS_PLAYLIST_FK = "song_belongs_to_playlist";


    public DatabaseLinker(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("=!=", "Create new DB");
        String createPlayListsTableSQL = "CREATE TABLE " + TABLE_PLAYLISTS + "(" + KEY_PLAYLISTS_PLAYLISTID + " TEXT PRIMARY KEY," + KEY_PLAYLISTS_NAME + " TEXT)";
        String createSongsTableSQL = "CREATE TABLE " + TABLE_SONGS + "(" + KEY_SONGS_SONGID + " TEXT," + KEY_SONGS_NAME + " TEXT," + KEY_SONGS_SONG_FILE + " TEXT," + KEY_SONGS_PLAYLIST_FK + " TEXT,"
                + KEY_SONG_DURATION + " TEXT,"
                + KEY_SONG_ERROR + " TEXT,"
                + "FOREIGN KEY(" + KEY_SONGS_PLAYLIST_FK + ") REFERENCES " + TABLE_PLAYLISTS + "(" + KEY_PLAYLISTS_PLAYLISTID + "),PRIMARY KEY (" + KEY_SONGS_SONGID + "," + KEY_SONGS_PLAYLIST_FK + "))";


        db.execSQL(createPlayListsTableSQL);
        db.execSQL(createSongsTableSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("=!=", "Upgrading db");
        String dropStatement = "DROP TABLE IF EXISTS ";

        db.execSQL(dropStatement + TABLE_SONGS);
        db.execSQL(dropStatement + TABLE_PLAYLISTS);
        Log.d("=!=", "DB dropped db");
        onCreate(db);
    }

    public List<YoutubePlayList> loadAllPlaylists() {
        String selectQuery = "SELECT " + KEY_PLAYLISTS_PLAYLISTID + " FROM " + TABLE_PLAYLISTS;
        SQLiteDatabase db = this.getReadableDatabase();
        List<YoutubePlayList> ret = new ArrayList<>();
        Cursor c = db.rawQuery(selectQuery, null);

        while (c != null && c.moveToNext()) {
            ret.add(loadPlaylist(c.getString(c.getColumnIndex(KEY_PLAYLISTS_PLAYLISTID))));
        }
        return ret;
    }

    public YoutubePlayList loadPlaylist(String playlistYoutubeID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYLISTS + " WHERE " + KEY_PLAYLISTS_PLAYLISTID + " = '" + playlistYoutubeID + "'";

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null && c.moveToFirst()) {
            String playlistName = c.getString(c.getColumnIndex(KEY_PLAYLISTS_NAME));
            YoutubePlayList retPlaylist = new YoutubePlayList(playlistYoutubeID, playlistName);
            List<YoutubeSong> songs = loadSongsByPlaylist(playlistYoutubeID);
            retPlaylist.setSongs(songs);

            return retPlaylist;
        } else {
            return null;
        }
    }

    private List<YoutubeSong> loadSongsByPlaylist(String playlistYoutubeID) {
        String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_SONGS_PLAYLIST_FK + " ='" + playlistYoutubeID + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        List<YoutubeSong> songs = new ArrayList<>();
        while (c != null && c.moveToNext()) {
            String songID = c.getString(c.getColumnIndex(KEY_SONGS_SONGID));
            String songName = c.getString(c.getColumnIndex(KEY_SONGS_NAME));
            String songFile = c.getString(c.getColumnIndex(KEY_SONGS_SONG_FILE));
            String songDuration = c.getString(c.getColumnIndex(KEY_SONG_DURATION));
            int songErrorCode = new Integer(c.getString(c.getColumnIndex(KEY_SONG_ERROR)));
            songs.add(new YoutubeSong(songID, playlistYoutubeID, songName, "THUMBNAIL", songFile,new Long(songDuration), YoutubeError.getErrorByCode(songErrorCode)));
        }
        return songs;
    }

    public void saveModel() {
        for (YoutubePlayList playlist : LocalModelRoot.getReadInstance().getPlayLists()) {
            savePlaylist(playlist);
        }
    }

    private void saveSongsForPlaylist(String playlistYoutubeID, List<YoutubeSong> songs) {
        List<YoutubeSong> existingSongs = loadSongsByPlaylist(playlistYoutubeID);
        //use sets, don't allow song duplication
        List<YoutubeSong> songsToAdd = new ArrayList<>();

        for (YoutubeSong newSong : songs) {
            if (!existingSongs.contains(newSong) && !newSong.getName().equals(DELETED_VIDEO_NAME_TAG) && !songsToAdd.contains(newSong)) {
                songsToAdd.add(newSong);
            }
        }

        SQLiteDatabase db = this.getWritableDatabase();
        for (YoutubeSong song : songsToAdd) {
            ContentValues values = new ContentValues();
            values.put(KEY_SONGS_SONGID, song.getId());
            values.put(KEY_SONGS_NAME, song.getName());
            values.put(KEY_SONG_DURATION, song.getDuration());
            values.put(KEY_SONGS_SONG_FILE, song.getSongFileLocation());
            values.put(KEY_SONGS_PLAYLIST_FK, playlistYoutubeID);
            values.put(KEY_SONG_ERROR,song.getError().getErrorCode());
            db.insert(TABLE_SONGS, null, values);
        }
    }

    private void savePlaylist(YoutubePlayList playlist) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (!loadAllPlaylists().contains(playlist)) {
            ContentValues values = new ContentValues();
            values.put(KEY_PLAYLISTS_PLAYLISTID, playlist.getId());
            values.put(KEY_PLAYLISTS_NAME, playlist.getName());

            // insert row
            db.insert(TABLE_PLAYLISTS, null, values);
            Log.d("===", "Inserted new item into " + TABLE_PLAYLISTS);
        }
        saveSongsForPlaylist(playlist.getId(), playlist.getSongs());
    }

    public void updateSongFile(String songId, String fileName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String filter = KEY_SONGS_SONGID + "='" + songId + "'";
        ContentValues args = new ContentValues();

        args.put(KEY_SONGS_SONG_FILE, fileName);
        db.update(TABLE_SONGS, args, filter, null);
    }

    public void updateSongError(String songId, YoutubeError error) {
        SQLiteDatabase db = this.getWritableDatabase();
        String filter = KEY_SONGS_SONGID + "='" + songId + "'";
        ContentValues args = new ContentValues();

        args.put(KEY_SONG_ERROR, error.getErrorCode());
        db.update(TABLE_SONGS, args, filter, null);
    }

    public void purgeAll() {
        Log.d("=!=","Purging DB");
        SQLiteDatabase db = this.getWritableDatabase();
        String deletePlaylists = "DELETE FROM " + TABLE_PLAYLISTS;
        String deleteSongs = "DELETE FROM " + TABLE_SONGS;

        try {
            db.execSQL(deletePlaylists);
            db.execSQL(deleteSongs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
