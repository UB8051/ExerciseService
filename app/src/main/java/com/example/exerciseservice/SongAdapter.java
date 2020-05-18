package com.example.exerciseservice;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter {
    private LayoutInflater songInf;
    private ArrayList<Song> songs;

    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        this.songs = theSongs;
        this.songInf = LayoutInflater.from(c);
    }

    public int getCount() {
        return this.songs.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLay = (LinearLayout) this.songInf.inflate(R.layout.song, parent, false);
        Song currSong = this.songs.get(position);
        ((TextView) songLay.findViewById(R.id.song_title)).setText(currSong.getTitle());
        ((TextView) songLay.findViewById(R.id.song_artist)).setText(currSong.getArtist());
        songLay.setTag(Integer.valueOf(position));
        return songLay;
    }
}
