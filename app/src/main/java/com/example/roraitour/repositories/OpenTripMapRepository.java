package com.example.roraitour.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.roraitour.api.ApiClient;
import com.example.roraitour.api.OpenTripMapDetailResponse;
import com.example.roraitour.api.OpenTripMapRadiusItem;
import com.example.roraitour.models.TouristPlace;
import com.example.roraitour.utils.AppConfig;
import com.example.roraitour.repositories.WikipediaRepository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenTripMapRepository {

    public interface PlacesCallback {
        void onSuccess(List<TouristPlace> places);
        void onError(String message);
    }

    public interface DetailsCallback {
        /**
         * @param details Full OpenTripMap details (may be null if unavailable)
         * @param wikipediaExtract Short extract from Wikipedia (may be null)
         * @param wikipediaImageUrl URL of an image from Wikipedia (may be null)
         */
        void onSuccess(OpenTripMapDetailResponse details, String wikipediaExtract, String wikipediaImageUrl);

        void onError(String message);
    }

    private static final String CACHE_PREF = "opentripmap_cache";
    private static final String CACHE_KEY_PLACES = "last_places";
    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    public OpenTripMapRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(CACHE_PREF, Context.MODE_PRIVATE);
    }

    public void getNearbyPlaces(double latitude, double longitude, String kinds, PlacesCallback callback) {
        if (AppConfig.OPENTRIPMAP_API_KEY == null || AppConfig.OPENTRIPMAP_API_KEY.isEmpty() || AppConfig.OPENTRIPMAP_API_KEY.equals("SUA_CHAVE_AQUI")) {
            callback.onError("Chave da API não configurada em AppConfig.java.");
            return;
        }

        String searchKinds = (kinds == null || kinds.isEmpty()) ? "interesting_places" : kinds;
        int radius = 15000; 

        ApiClient.openTripMapService()
                .getNearbyPlaces(radius, longitude, latitude, searchKinds, "json", 50, AppConfig.OPENTRIPMAP_API_KEY)
                .enqueue(new Callback<List<OpenTripMapRadiusItem>>() {
                    @Override
                    public void onResponse(Call<List<OpenTripMapRadiusItem>> call, Response<List<OpenTripMapRadiusItem>> response) {
                        if (!response.isSuccessful()) {
                            String errorMsg = "Erro " + response.code();
                            if (response.code() == 401) errorMsg = "Chave da API inválida.";
                            callback.onError(errorMsg);
                            return;
                        }
                        
                        if (response.body() == null || response.body().isEmpty()) {
                            callback.onError("Nenhum local encontrado nesta região.");
                            return;
                        }

                        List<TouristPlace> places = new ArrayList<>();
                        for (OpenTripMapRadiusItem item : response.body()) {
                            TouristPlace place = new TouristPlace();
                            place.setXid(item.getXid());
                            place.setName(item.getName());
                            place.setCategory(extractCategory(item.getKinds()));
                            place.setDistance(item.getDistance());
                            if (item.getPoint() != null) {
                                place.setLatitude(item.getPoint().getLat());
                                place.setLongitude(item.getPoint().getLon());
                            }
                            places.add(place);
                        }
                        cachePlaces(places);
                        callback.onSuccess(places);
                    }

                    @Override
                    public void onFailure(Call<List<OpenTripMapRadiusItem>> call, Throwable throwable) {
                        List<TouristPlace> cached = getCachedPlaces();
                        if (!cached.isEmpty()) {
                            callback.onSuccess(cached);
                            return;
                        }
                        callback.onError("Falha na conexão: " + throwable.getLocalizedMessage());
                    }
                });
    }

    public void getPlaceDetails(String xid, DetailsCallback callback) {
        if (AppConfig.OPENTRIPMAP_API_KEY == null || AppConfig.OPENTRIPMAP_API_KEY.isEmpty() || AppConfig.OPENTRIPMAP_API_KEY.equals("SUA_CHAVE_AQUI")) {
            callback.onError("Chave da API não configurada.");
            return;
        }

        ApiClient.openTripMapService()
                .getPlaceDetails(xid, AppConfig.OPENTRIPMAP_API_KEY)
                .enqueue(new Callback<OpenTripMapDetailResponse>() {
                    @Override
                    public void onResponse(Call<OpenTripMapDetailResponse> call, Response<OpenTripMapDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            OpenTripMapDetailResponse details = response.body();

                            String wikiExtract = null;
                            String wikiImage = null;

                            if (details.getWikipediaExtracts() != null && details.getWikipediaExtracts().getText() != null && !details.getWikipediaExtracts().getText().isEmpty()) {
                                wikiExtract = details.getWikipediaExtracts().getText();
                            }

                            if (details.getPreview() != null && details.getPreview().getSource() != null && !details.getPreview().getSource().isEmpty()) {
                                wikiImage = details.getPreview().getSource();
                            }

                            if ((wikiExtract == null || wikiImage == null) && details.getName() != null && !details.getName().isEmpty()) {
                                // Enrich with Wikipedia fallback when OTM detail lacks data
                                final String baseExtract = wikiExtract;
                                final String baseImage = wikiImage;
                                new WikipediaRepository().fetchSummary(details.getName(), new WikipediaRepository.WikipediaCallback() {
                                    @Override
                                    public void onSuccess(String summary, String imageUrl) {
                                        String finalExtract = (baseExtract == null) ? summary : baseExtract;
                                        String finalImage = (baseImage == null) ? imageUrl : baseImage;
                                        callback.onSuccess(details, finalExtract, finalImage);
                                    }

                                    @Override
                                    public void onError() {
                                        callback.onSuccess(details, baseExtract, baseImage);
                                    }
                                });
                            } else {
                                callback.onSuccess(details, wikiExtract, wikiImage);
                            }

                        } else {
                            callback.onError("Detalhes não encontrados.");
                        }
                    }

                    @Override
                    public void onFailure(Call<OpenTripMapDetailResponse> call, Throwable t) {
                        callback.onError("Erro de conexão: " + t.getMessage());
                    }
                });
    }

    private void cachePlaces(List<TouristPlace> places) {
        sharedPreferences.edit().putString(CACHE_KEY_PLACES, gson.toJson(places)).apply();
    }

    private List<TouristPlace> getCachedPlaces() {
        String json = sharedPreferences.getString(CACHE_KEY_PLACES, "");
        if (json == null || json.isEmpty()) return new ArrayList<>();
        Type type = new TypeToken<List<TouristPlace>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private String extractCategory(String kinds) {
        if (kinds == null || kinds.isEmpty()) return "Turismo";
        String category = kinds.split(",")[0].replace("_", " ").trim();
        if (category.isEmpty()) return "Turismo";
        return category.substring(0, 1).toUpperCase() + category.substring(1);
    }
}
