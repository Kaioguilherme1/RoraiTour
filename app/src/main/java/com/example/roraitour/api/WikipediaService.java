package com.example.roraitour.api;

import com.example.roraitour.models.WikipediaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WikipediaService {
    @GET("page/summary/{title}")
    Call<WikipediaResponse> getSummary(@Path("title") String title);
}

