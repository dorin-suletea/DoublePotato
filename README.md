# DoublePotato

What is: YouTube playlist offline player.
  * This app will keep track of your youtube playlists and allow to play them in offline mode.

##### Features : #####
  * YouTube sync = extracts information about playlists created by an account.
  * Download manager = using a third party service does the downloading of the items.
    * Stops when reaches max memory allocated.
    * Can perform background downloads.
    * Auto conversion mp4 to aac (ffmpeg integrated com.github.hiteshsondhi88.libffmpeg:FFmpegAndroid:0.2.5)
  * Media player = integrated custom built media player. 
    * Background and lock-screen play.
    * Shuffling support.
    * Lock-screen controls for ISC and L
    * Volume buttons song navigator :
      * The player is running and the screen is unlit: pressing  the volume buttons will change the current song.
      * The player is running and the screen is lit : change the volume.
    * Pause playback on call.
      
      
##### Disclaimer : 
Downloading YouTube videos is forbidden by the Terms of Service (5.L).
This is just a proof of concept application and should not be treated and used as such.
The actual downloading process is done via a third party service and is not implemented by the application itself.
          
##### How to use (Android 5.1) :
    1 Build, install and start the .apk (see build instructions at the end of the document).
    2 Go to settings from the menu (top right).
      - set the YouTube channel ID (https://support.google.com/youtube/answer/3250431?hl=en).
      - set the max memory allocation (when the total size of the downloaded items reaches this nr, the download will stop automatically)
      - save settings
    3 Press the sync button in the toolbar (make sure your wireless is on no data transfer activated)
    4 After the sync is done all your playlists will be available in the application.
    5 For each playlist you can tap the download button (playlist row to the right).
    6 Tap the row to get to the songs.
    7 Tap a song and have fun.
              
##### Android versions: 
    Tested on Motorolla Moto G Android L (5.1).
              
    Will not work on Android M : Permissions not integrated
    No lock-screen controls for android ISC. (the implementation is committed, but not integrated yet)
            


