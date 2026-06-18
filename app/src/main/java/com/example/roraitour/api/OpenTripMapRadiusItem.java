package com.example.roraitour.api;

import com.google.gson.annotations.SerializedName;

/**
 * Model for OpenTripMap /radius response item.
 */
public final class OpenTripMapRadiusItem {

    @SerializedName("xid")
    private String xid;

    @SerializedName("name")
    private String name;

    @SerializedName("kinds")
    private String kinds;

    @SerializedName("dist")
    private double distance;

    @SerializedName("point")
    private Point point;

    public String getXid() {
        return xid;
    }

    public String getName() {
        return name;
    }

    public String getKinds() {
        return kinds;
    }

    public double getDistance() {
        return distance;
    }

    public Point getPoint() {
        return point;
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
}

