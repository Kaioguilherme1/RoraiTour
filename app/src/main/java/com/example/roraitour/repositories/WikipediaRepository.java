package com.example.roraitour.repositories;

import com.example.roraitour.api.ApiClient;
import com.example.roraitour.api.MediaWikiResponse;
import com.example.roraitour.models.WikipediaResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WikipediaRepository {

    public interface WikipediaCallback {
        void onSuccess(String summary, String imageUrl);

        void onError();
    }

    public void fetchSummary(String title, WikipediaCallback callback) {
        if (title == null || title.isEmpty()) {
            callback.onError();
            return;
        }

        // Formata o título para o padrão da Wikipédia (espaços -> underscores)
        String formattedTitle = title.trim().replace(" ", "_");

        // Primeiro tenta REST summary (mais rico)
        ApiClient.wikipediaService().getSummary(formattedTitle).enqueue(new Callback<WikipediaResponse>() {
            @Override
            public void onResponse(Call<WikipediaResponse> call, Response<WikipediaResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    // tenta MediaWiki query como fallback
                    fetchThumbnailFromMediaWiki(formattedTitle, callback);
                    return;
                }
                WikipediaResponse data = response.body();
                String imageUrl = data.getOriginalImage() != null ? data.getOriginalImage().getSource() : null;

                if (imageUrl == null || imageUrl.isEmpty()) {
                    // fallback para MediaWiki API para obter thumbnail
                    fetchThumbnailFromMediaWiki(formattedTitle, new WikipediaCallback() {
                        @Override
                        public void onSuccess(String summary2, String imageUrl2) {
                            String summaryFinal = (data.getExtract() == null || data.getExtract().isEmpty()) ? summary2 : data.getExtract();
                            String imageFinal = imageUrl2 == null || imageUrl2.isEmpty() ? null : imageUrl2;
                            callback.onSuccess(summaryFinal, imageFinal);
                        }

                        @Override
                        public void onError() {
                            callback.onSuccess(data.getExtract(), null);
                        }
                    });
                    return;
                }

                callback.onSuccess(data.getExtract(), imageUrl);
            }

            @Override
            public void onFailure(Call<WikipediaResponse> call, Throwable throwable) {
                // REST failed, try MediaWiki
                fetchThumbnailFromMediaWiki(formattedTitle, callback);
            }
        });
    }

    private void fetchThumbnailFromMediaWiki(String title, WikipediaCallback callback) {
        ApiClient.mediaWikiService().queryPageImages("query", "json", title, "pageimages", 640).enqueue(new Callback<MediaWikiResponse>() {
            @Override
            public void onResponse(Call<MediaWikiResponse> call, Response<MediaWikiResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getQuery() == null || response.body().getQuery().getPages() == null) {
                    callback.onError();
                    return;
                }
                for (MediaWikiResponse.Page page : response.body().getQuery().getPages().values()) {
                    if (page != null && page.getThumbnail() != null && page.getThumbnail().getSource() != null) {
                        callback.onSuccess(null, page.getThumbnail().getSource());
                        return;
                    }
                }
                callback.onError();
            }

            @Override
            public void onFailure(Call<MediaWikiResponse> call, Throwable t) {
                callback.onError();
            }
        });
    }
}

