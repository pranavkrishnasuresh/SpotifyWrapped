package com.example.spotifytutorialtrialrun;

public class Song {
    private String name;
    private String imageUrl;
    private String previewUrl;


    //this one is needed bc firebase requires that we have one w no constructors
    public Song() {
    }
    public Song(String name, String imageUrl, String previewUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.previewUrl = previewUrl;
    }
    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}