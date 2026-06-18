package com.example.roraitour.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roraitour.R;
import com.example.roraitour.models.FavoritePlace;
import com.example.roraitour.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoritePlace place);
    }

    private final List<FavoritePlace> places = new ArrayList<>();
    private final OnFavoriteClickListener listener;

    public FavoriteAdapter(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FavoritePlace> list) {
        places.clear();
        places.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_place, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoritePlace place = places.get(position);
        holder.name.setText(place.getName());
        holder.category.setText(place.getCategory());
        holder.distance.setText(holder.itemView.getContext().getString(R.string.distance_km, place.getDistance() / 1000d));
        ImageLoader.load(holder.image, place.getImage());
        holder.itemView.setOnClickListener(v -> listener.onFavoriteClick(place));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView category;
        TextView distance;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imagePlace);
            name = itemView.findViewById(R.id.textName);
            category = itemView.findViewById(R.id.textCategory);
            distance = itemView.findViewById(R.id.textDistance);
        }
    }
}

