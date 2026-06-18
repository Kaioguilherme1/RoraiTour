package com.example.roraitour.models;

import com.google.gson.annotations.SerializedName;

public class WikipediaResponse {
    private String title;
    private String extract;
    @SerializedName("originalimage")
    private ImageInfo originalImage;

    public String getTitle() {
        return title;
    }

    public String getExtract() {
        return extract;
    }

    public ImageInfo getOriginalImage() {
        return originalImage;
    }

    public static class ImageInfo {
        private String source;

        public String getSource() {
            return source;
        }
    }
}

