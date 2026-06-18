package com.example.roraitour.api;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class MediaWikiResponse {

    @SerializedName("query")
    private Query query;

    public Query getQuery() {
        return query;
    }

    public static class Query {
        @SerializedName("pages")
        private Map<String, Page> pages;

        public Map<String, Page> getPages() {
            return pages;
        }
    }

    public static class Page {
        @SerializedName("pageid")
        private Integer pageid;

        @SerializedName("thumbnail")
        private Thumbnail thumbnail;

        public Integer getPageid() {
            return pageid;
        }

        public Thumbnail getThumbnail() {
            return thumbnail;
        }
    }

    public static class Thumbnail {
        @SerializedName("source")
        private String source;

        @SerializedName("width")
        private Integer width;

        @SerializedName("height")
        private Integer height;

        public String getSource() {
            return source;
        }

        public Integer getWidth() {
            return width;
        }

        public Integer getHeight() {
            return height;
        }
    }
}
