package com.dsu.dev4fun.doublepotato.ui.fragment;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.media.MediaPlayService;
import com.dsu.dev4fun.doublepotato.media.MediaPlayServiceProxy;
import com.dsu.dev4fun.doublepotato.model.DownloadService;
import com.dsu.dev4fun.doublepotato.model.UserPreferences;
import com.dsu.dev4fun.doublepotato.model.downloader.FFConverter;
import com.dsu.dev4fun.doublepotato.model.meta.DataBuilder;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;


public class MainActivity extends AppCompatActivity {
    public static final String API_KEY = "AIzaSyB_TSE4ZG_9WFYCilHjWGAP1xYn9KwygA0";
    public static final String CHANEL_ID = "UCHphW02wxlMKQGPJBkIei0w";

    private Toolbar applicationToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        //load user prefs
        UserPreferences.getInstance().initialize(this);

        //load converter
        MethodWrapper<Boolean> onLoadLib = new MethodWrapper<Boolean>() {
            @Override
            public void execute(Boolean params) {
                Log.d("=!=", "ffmpeg loaded " + params);
            }
        };
        FFConverter.getInstance().loadNative(this, onLoadLib);

        /**Initialize player proxy*/
        MediaPlayServiceProxy.getInstance().setup(this);

        /** Init the local model from the database **/
        String chanelID = UserPreferences.getInstance().getChannelId();
        String appId = UserPreferences.getInstance().getAppId();
        DataBuilder.getInstance().init(chanelID, appId, this);

        MethodWrapper<Void> afterLocalDbInit = new MethodWrapper<Void>() {
            @Override
            public void execute(Void params) {
                setContentView(R.layout.activity_main);
                // set this as the model and use it in the application
                BusinessLogicHelper.replaceCurrentFragment(MainActivity.this, new Bundle(), new PlaylistsFragment());
                applicationToolbar = (Toolbar) findViewById(R.id.app_bar);
                setSupportActionBar(applicationToolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        };
        DataBuilder.getInstance().readLocalModel(afterLocalDbInit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent stopDownloadService = new Intent(this, DownloadService.class);
        this.stopService(stopDownloadService);

        Intent stopPlaybackService = new Intent(this, MediaPlayService.class);
        this.stopService(stopPlaybackService);
    }

    @Override
    public void onBackPressed() {
        // keep PlaylistFragment on screen
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //if current fragment is not the last
                if (getFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    BusinessLogicHelper.replaceCurrentFragment(MainActivity.this, new Bundle(), new PlaylistsFragment());
                }
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*Log.d("=!=","Resume");
        if (BusinessLogicHelper.isServiceRunning(MediaPlayService.class.getName(),this)){
            //if playback service is running
            Intent refreshMusicPlaybackState = new Intent(this,MediaPlayService.class);
            refreshMusicPlaybackState.setAction(MediaPlayService.REFRESH_STATE_ACTION);
            this.startService(refreshMusicPlaybackState);
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (BusinessLogicHelper.isServiceRunning(MediaPlayService.class.getName(), this)) {
            //if playback service is running
            // Intent refreshMusicPlaybackState = new Intent(this,MediaPlayService.class);
            //refreshMusicPlaybackState.setAction(MediaPlayService.REFRESH_STATE_ACTION);
            // this.startService(refreshMusicPlaybackState);
        }
    }
}
