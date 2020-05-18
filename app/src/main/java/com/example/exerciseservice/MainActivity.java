package com.example.exerciseservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.exerciseservice.MusicService.MusicBinder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private MusicController controller;
    public boolean musicBound = false;
    //mengkoneksikan ke service
    private ServiceConnection musicConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            //mendapatkan service
            MusicService unused = MainActivity.this.musicSrv = ((MusicService.MusicBinder) service).getService();
            //menambahkan ke daftar
            MainActivity.this.musicSrv.setList(MainActivity.this.songList);
            boolean unused2 = MainActivity.this.musicBound = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            boolean unused = MainActivity.this.musicBound = false;
        }
    };
    public MusicService musicSrv;
    private boolean paused = false;
    private Intent playIntent;
    private boolean playbackPaused = false;
    public ArrayList<Song> songList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
            ListView songView = (ListView) findViewById(R.id.song_list);
            this.songList = new ArrayList<>();
            getSongList();
            Collections.sort(this.songList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getTitle().compareTo(b.getTitle());
                }
            });
            songView.setAdapter(new SongAdapter(this, this.songList));
            setController();
        }
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(Context context) {
        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(context, "android.permission.READ_EXTERNAL_STORAGE") == 0) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, "android.permission.READ_EXTERNAL_STORAGE")) {
            showDialog("External storage", context, "android.permission.READ_EXTERNAL_STORAGE");
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        return false;
    }

    @SuppressLint("ResourceType")
    public void showDialog(String msg, final Context context, final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(17039379, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, MainActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }


        });
        alertBuilder.create().show();
    }

    @SuppressLint("WrongConstant")
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != 123) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (grantResults[0] != 0) {
            Toast.makeText(getApplicationContext(), "GET_ACCOUNTS Denied",0).show();
        }
    }

    @SuppressLint("WrongConstant")
    public void onStart() {
        super.onStart();
        if (this.playIntent == null) {
            this.playIntent = new Intent(this, MusicService.class);
            bindService(this.playIntent, this.musicConnection, 1);
            startService(this.playIntent);
        }
    }

    public void songPicked(View view) {
        this.musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        this.musicSrv.playSong();
        if (this.playbackPaused) {
            setController();
            this.playbackPaused = false;
        }
        this.controller.show(0);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //item menu yang dipilih
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        //memberhentikan pemutar musik
        if (itemId == R.id.action_end) {
            stopService(this.playIntent);
            this.musicSrv = null;
            System.exit(0);
            //mengacak list
        } else if (itemId == R.id.action_shuffle) {
            this.musicSrv.setShuffle();
        }
        return super.onOptionsItemSelected(item);
    }

    //mengambil info lagu
    public void getSongList() {
        Cursor musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, (String[]) null, (String) null, (String[]) null, (String) null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //mendapatkan kolom
            int titleColumn = musicCursor.getColumnIndex("title");
            int idColumn = musicCursor.getColumnIndex("_id");
            int artistColumn = musicCursor.getColumnIndex("artist");
            //menambahkan list lagu
            do {
                this.songList.add(new Song(musicCursor.getLong(idColumn), musicCursor.getString(titleColumn), musicCursor.getString(artistColumn)));
            } while (musicCursor.moveToNext());
        }
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    public int getAudioSessionId() {
        return 0;
    }

    public int getBufferPercentage() {
        return 0;
    }

    public int getCurrentPosition() {
        MusicService musicService = this.musicSrv;
        if (musicService == null || !this.musicBound || !musicService.isPng()) {
            return 0;
        }
        return this.musicSrv.getPosn();
    }

    //durasi musik
    public int getDuration() {
        MusicService musicService = this.musicSrv;
        if (musicService == null || !this.musicBound || !musicService.isPng()) {
            return 0;
        }
        return this.musicSrv.getDur();
    }

    public boolean isPlaying() {
        MusicService musicService = this.musicSrv;
        if (musicService == null || !this.musicBound) {
            return false;
        }
        return musicService.isPng();
    }

    //menjeda musik
    public void pause() {
        this.playbackPaused = true;
        this.musicSrv.pausePlayer();
    }

    public void seekTo(int pos) {
        this.musicSrv.seek(pos);
    }

    public void start() {
        this.musicSrv.go();
    }

    //menggatur controller
    private void setController() {
        this.controller = new MusicController(this);
        this.controller.setPrevNextListeners(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.playNext();
            }
        }, new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.playPrev();
            }
        });
        this.controller.setMediaPlayer(this);
        this.controller.setAnchorView(findViewById(R.id.song_list));
        this.controller.setEnabled(true);
    }

    //memutar musik selanjutnya
    public void playNext() {
        this.musicSrv.playNext();
        if (this.playbackPaused) {
            setController();
            this.playbackPaused = false;
        }
        this.controller.show(0);
    }

    //memutar musik sebelumya
    public void playPrev() {
        this.musicSrv.playPrev();
        if (this.playbackPaused) {
            setController();
            this.playbackPaused = false;
        }
        this.controller.show(0);
    }

    //memberhentikan musik
    public void onPause() {
        super.onPause();
        this.paused = true;
    }

    //melanjutkan lagu
    public void onResume() {
        super.onResume();
        if (this.paused) {
            setController();
            this.paused = false;
        }
    }

    //fungsi memberhentikan lagu
    public void onStop() {
        this.controller.hide();
        super.onStop();
    }

    public void onDestroy() {
        stopService(this.playIntent);
        this.musicSrv = null;
        super.onDestroy();
    }
}
