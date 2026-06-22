package com.example.roraitour.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roraitour.R;
import com.example.roraitour.models.TouristPlace;
import com.example.roraitour.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TouristPlaceAdapter extends RecyclerView.Adapter<TouristPlaceAdapter.PlaceViewHolder> implements Filterable {

    public interface OnPlaceClickListener {
        void onPlaceClick(TouristPlace place);
        default void onVisitedChange(TouristPlace place, boolean isVisited) {}
    }

    private final OnPlaceClickListener listener;
    private final List<TouristPlace> original = new ArrayList<>();
    private final List<TouristPlace> filtered = new ArrayList<>();

    public TouristPlaceAdapter(OnPlaceClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TouristPlace> places) {
        original.clear();
        filtered.clear();
        original.addAll(places);
        filtered.addAll(places);
        notifyDataSetChanged();
    }

    public void filterByCategory(String category) {
        filtered.clear();
        if (category == null || category.equalsIgnoreCase("Todas")) {
            filtered.addAll(original);
        } else {
            for (TouristPlace place : original) {
                if (place.getCategory() != null && place.getCategory().toLowerCase(Locale.ROOT).contains(category.toLowerCase(Locale.ROOT))) {
                    filtered.add(place);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tourist_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        TouristPlace place = filtered.get(position);
        holder.name.setText(place.getDisplayName());
        holder.category.setText(place.getCategory());
        holder.distance.setText(holder.itemView.getContext().getString(R.string.distance_km, place.getDistance() / 1000d));
        ImageLoader.load(holder.image, place.getImage());
        
        holder.checkVisited.setOnCheckedChangeListener(null);
        holder.checkVisited.setChecked(place.isVisited());
        holder.checkVisited.setOnCheckedChangeListener((buttonView, isChecked) -> {
            place.setVisited(isChecked);
            listener.onVisitedChange(place, isChecked);
        });

        holder.itemView.setOnClickListener(v -> listener.onPlaceClick(place));
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<TouristPlace> results = new ArrayList<>();
                String query = constraint == null ? "" : constraint.toString().toLowerCase(Locale.ROOT).trim();
                if (query.isEmpty()) {
                    results.addAll(original);
                } else {
                    for (TouristPlace place : original) {
                        if (place.getDisplayName().toLowerCase(Locale.ROOT).contains(query)) {
                            results.add(place);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = results;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered.clear();
                if (results != null && results.values instanceof List<?>) {
                    filtered.addAll((List<TouristPlace>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView category;
        TextView distance;
        android.widget.CheckBox checkVisited;

        PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imagePlace);
            name = itemView.findViewById(R.id.textName);
            category = itemView.findViewById(R.id.textCategory);
            distance = itemView.findViewById(R.id.textDistance);
            checkVisited = itemView.findViewById(R.id.checkVisited);
        }
    }
}
