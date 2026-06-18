package com.example.roraitour.api;

import com.google.gson.annotations.SerializedName;

/**
 * Model for OpenTripMap detail response.
 */
public final class OpenTripMapDetailResponse {

    @SerializedName("xid")
    private String xid;

    @SerializedName("name")
    private String name;

    @SerializedName("kinds")
    private String kinds;

    @SerializedName("point")
    private Point point;

    @SerializedName("preview")
    private Preview preview;

    @SerializedName("wikipedia_extracts")
    private WikipediaExtracts wikipediaExtracts;

    public String getXid() {
        return xid;
    }

    public String getName() {
        return name;
    }

    public String getKinds() {
        return kinds;
    }

    public Point getPoint() {
        return point;
    }

    public Preview getPreview() {
        return preview;
    }

    public WikipediaExtracts getWikipediaExtracts() {
        return wikipediaExtracts;
    }

    public static final class Point {

        @SerializedName("lon")
        private double lon;

        @SerializedName("lat")
        private double lat;

        public double getLon() {
            return lon;
        }

        public double getLat() {
            return lat;
        }
    }

    public static final class Preview {

        @SerializedName("source")
        private String source;

        public String getSource() {
            return source;
        }
    }

    public static final class WikipediaExtracts {

        @SerializedName("text")
        private String text;

        public String getText() {
            return text;
        }
    }
}

