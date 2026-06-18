package com.example.roraitour.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OpenTripMapService {
    /**
     * GET /radius
     * Matches OpenTripMap API which expects: radius, lon, lat, kinds, format, limit, apikey
     */
    @GET("radius")
    Call<List<OpenTripMapRadiusItem>> getNearbyPlaces(
            @Query("radius") int radius,
            @Query("lon") double longitude,
            @Query("lat") double latitude,
            @Query("kinds") String kinds,
            @Query("format") String format,
            @Query("limit") int limit,
            @Query("apikey") String apiKey
    );

    @GET("xid/{xid}")
    Call<OpenTripMapDetailResponse> getPlaceDetails(
            @Path("xid") String xid,
            @Query("apikey") String apiKey
    );
}
