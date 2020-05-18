package com.example.exerciseservice;

public class Song {
    private String artist;
    private long id;
    private String title;

    public Song(long songID, String songTitle, String songArtist) {
        this.id = songID;
        this.title = songTitle;
        this.artist = songArtist;
    }

    public long getID() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getArtist() {
        return this.artist;
    }
}