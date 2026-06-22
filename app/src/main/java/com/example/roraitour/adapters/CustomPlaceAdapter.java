package com.example.roraitour.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roraitour.R;
import com.example.roraitour.models.CustomPlace;
import com.example.roraitour.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class CustomPlaceAdapter extends RecyclerView.Adapter<CustomPlaceAdapter.CustomViewHolder> {

    public interface OnCustomPlaceClickListener {
        void onClick(CustomPlace place);
        default void onVisitedChange(CustomPlace place, boolean isVisited) {}
    }

    private final OnCustomPlaceClickListener listener;
    private final List<CustomPlace> places = new ArrayList<>();

    public CustomPlaceAdapter(OnCustomPlaceClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CustomPlace> list) {
        places.clear();
        places.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_place, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        CustomPlace place = places.get(position);
        holder.name.setText(place.getName());
        holder.category.setText(place.getCategory());
        holder.description.setText(place.getDescription());
        ImageLoader.load(holder.image, place.getImage());

        holder.checkVisited.setOnCheckedChangeListener(null);
        holder.checkVisited.setChecked(place.isVisited());
        holder.checkVisited.setOnCheckedChangeListener((buttonView, isChecked) -> {
            place.setVisited(isChecked);
            listener.onVisitedChange(place, isChecked);
        });

        holder.itemView.setOnClickListener(v -> listener.onClick(place));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView category;
        TextView description;
        android.widget.CheckBox checkVisited;

        CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imagePlace);
            name = itemView.findViewById(R.id.textName);
            category = itemView.findViewById(R.id.textCategory);
            description = itemView.findViewById(R.id.textDescription);
            checkVisited = itemView.findViewById(R.id.checkVisited);
        }
    }
}

