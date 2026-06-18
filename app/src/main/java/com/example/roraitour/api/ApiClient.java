package com.example.roraitour.api;

import com.example.roraitour.utils.AppConfig;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public final class ApiClient {
    private static volatile Retrofit openTripMapRetrofit;
    private static volatile Retrofit wikipediaRetrofit;
    private static volatile Retrofit mediaWikiRetrofit;

    private ApiClient() {
    }

    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .retryOnConnectionFailure(true)
                .build();
    }

    public static OpenTripMapService openTripMapService() {
        if (openTripMapRetrofit == null) {
            synchronized (ApiClient.class) {
                if (openTripMapRetrofit == null) {
                    openTripMapRetrofit = new Retrofit.Builder()
                            .baseUrl(AppConfig.OPENTRIPMAP_BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(createOkHttpClient())
                            .build();
                }
            }
        }
        return openTripMapRetrofit.create(OpenTripMapService.class);
    }

    public static WikipediaService wikipediaService() {
        if (wikipediaRetrofit == null) {
            synchronized (ApiClient.class) {
                if (wikipediaRetrofit == null) {
                    wikipediaRetrofit = new Retrofit.Builder()
                            .baseUrl(AppConfig.WIKIPEDIA_BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(createOkHttpClient())
                            .build();
                }
            }
        }
        return wikipediaRetrofit.create(WikipediaService.class);
    }

    /**
     * MediaWiki API (action=query) client — used as fallback to fetch thumbnails.
     */
    public static MediaWikiService mediaWikiService() {
        if (mediaWikiRetrofit == null) {
            synchronized (ApiClient.class) {
                if (mediaWikiRetrofit == null) {
                    mediaWikiRetrofit = new Retrofit.Builder()
                            .baseUrl("https://pt.wikipedia.org/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(createOkHttpClient())
                            .build();
                }
            }
        }
        return mediaWikiRetrofit.create(MediaWikiService.class);
    }
}
