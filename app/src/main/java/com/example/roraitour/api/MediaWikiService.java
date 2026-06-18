package com.example.roraitour.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MediaWikiService {
    @GET("w/api.php")
    Call<MediaWikiResponse> queryPageImages(
            @Query("action") String action,
            @Query("format") String format,
            @Query("titles") String titles,
            @Query("prop") String prop,
            @Query("pithumbsize") int pithumbsize
    );
}
