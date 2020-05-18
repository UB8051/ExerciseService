package com.example.exerciseservice;

import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final int NOTIFY_ID = 1;
    private final IBinder musicBind = new MusicBinder();
    private MediaPlayer player;
    private Random rand;
    private boolean shuffle = false;
    private int songPosn;
    private String songTitle = BuildConfig.FLAVOR;
    private ArrayList<Song> songs;

    //membuat service
    public void onCreate() {
        super.onCreate();
        //inisiasi posisi
        this.songPosn = 0;
        this.rand = new Random();
        //membuat pemutar
        this.player = new MediaPlayer();
        initMusicPlayer();
    }

    //mengatur property pemutar
    public void initMusicPlayer() {
        this.player.setWakeMode(getApplicationContext(), 1);
        this.player.setAudioStreamType(3);
        this.player.setOnPreparedListener(this);
        this.player.setOnCompletionListener(this);
        this.player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs) {
        this.songs = theSongs;
    }

    public class MusicBinder extends Binder {
        public MusicBinder() {
        }

        public MusicService getService() {
            return MusicService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return this.musicBind;
    }

    public boolean onUnbind(Intent intent) {
        this.player.stop();
        this.player.release();
        return false;
    }

    //memutar musik
    public void playSong() {
        this.player.reset();
        //mendapatkan musik
        Song playSong = this.songs.get(this.songPosn);
        this.songTitle = playSong.getTitle();
        try {
            this.player.setDataSource(getApplicationContext(), ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, playSong.getID()));
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        this.player.prepareAsync();
    }

    public void setSong(int songIndex) {
        this.songPosn = songIndex;
    }

    public void onCompletion(MediaPlayer mp) {
        if (this.player.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v("MUSIC PLAYER", "Playback Error");
        mp.reset();
        return false;
    }

    //memulai pemutaran
    @SuppressLint("WrongConstant")
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(67108864);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, 134217728);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play).setTicker(this.songTitle).setOngoing(true).setContentTitle("Playing").setContentText(this.songTitle);

    }

    public int getPosn() {
        return this.player.getCurrentPosition();
    }

    public int getDur() {
        return this.player.getDuration();
    }

    public boolean isPng() {
        return this.player.isPlaying();
    }

    public void pausePlayer() {
        this.player.pause();
    }

    public void seek(int posn) {
        this.player.seekTo(posn);
    }

    public void go() {
        this.player.start();
    }

    public void playPrev() {
        this.songPosn--;
        if (this.songPosn < 0) {
            this.songPosn = this.songs.size() - 1;
        }
        playSong();
    }

    public void playNext() {
        if (this.shuffle) {
            int newSong = this.songPosn;
            while (newSong == this.songPosn) {
                newSong = this.rand.nextInt(this.songs.size());
            }
            this.songPosn = newSong;
        } else {
            this.songPosn++;
            if (this.songPosn >= this.songs.size()) {
                this.songPosn = 0;
            }
        }
        playSong();
    }

    public void onDestroy() {
        stopForeground(true);
    }

    public void setShuffle() {
        if (this.shuffle) {
            this.shuffle = false;
        } else {
            this.shuffle = true;
        }
    }
}
