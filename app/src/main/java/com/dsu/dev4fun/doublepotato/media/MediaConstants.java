package com.dsu.dev4fun.doublepotato.media;


public class MediaConstants {
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";


    //for android 4
    public static final String ACTION_SWITCH_STATE = "action_stop";

    public enum MediaServiceState {
        STARTED,
        STOPPED,
        PAUSED,
        DEFAULT
    }
}
