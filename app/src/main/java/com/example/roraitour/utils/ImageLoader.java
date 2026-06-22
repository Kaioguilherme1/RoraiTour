package com.example.roraitour.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.roraitour.R;

public final class ImageLoader {

    private ImageLoader() {
    }

    public static void load(ImageView imageView, String url) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.mipmap.ic_launcher_round);
            return;
        }

        Glide.with(imageView.getContext())
                .load(url)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Desabilitar cache para imagens locais de perfil
                .skipMemoryCache(true)
                .centerCrop()
                .into(imageView);
    }

    public static void preload(Context context, String url) {
        if (url == null || url.isEmpty()) return;
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .preload();
    }
}
