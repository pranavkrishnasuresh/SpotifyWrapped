package com.example.spotifytutorialtrialrun;

import java.util.List;

public class Wrapped {
    private String date;
    private List<Artist> favoriteArtists;
    private List<Song> favoriteSongs;

    //this one is needed bc firebase requires that we have one w no constructors
    public Wrapped() {
    }

    public Wrapped(String date, List<Artist> favoriteArtists, List<Song> favoriteSongs) {
        this.date = date;
        this.favoriteArtists = favoriteArtists;
        this.favoriteSongs = favoriteSongs;
    }
    public String getDate() {
        return date;
    }

    public List<Artist> getFavoriteArtists() {
        return favoriteArtists;
    }

    public List<Song> getFavoriteSongs() {
        return favoriteSongs;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String toString() {
        return "Wrapped{" +
                "date='" + date + '\'' +
                "favoriteArtists='" + favoriteArtists + '\'' +
                '}';
    }
}