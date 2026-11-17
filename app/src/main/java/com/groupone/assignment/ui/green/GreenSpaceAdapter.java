package com.groupone.assignment.ui.green;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.groupone.assignment.R;
import com.groupone.assignment.data.GreenSpace;

import java.util.Locale;

/**
 * Renders nearby parks and gardens.
 */
public class GreenSpaceAdapter extends ListAdapter<GreenSpace, GreenSpaceAdapter.Holder> {

    private static final DiffUtil.ItemCallback<GreenSpace> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull GreenSpace oldItem,
                                               @NonNull GreenSpace newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getLatitude() == newItem.getLatitude()
                            && oldItem.getLongitude() == newItem.getLongitude();
                }

                @Override
                public boolean areContentsTheSame(@NonNull GreenSpace oldItem,
                                                  @NonNull GreenSpace newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getType().equals(newItem.getType())
                            && oldItem.getDistanceMeters() == newItem.getDistanceMeters();
                }
            };

    public GreenSpaceAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_green_space, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        GreenSpace space = getItem(position);
        holder.name.setText(space.getName());
        holder.type.setText(space.getType());
        holder.distance.setText(formatDistance(space.getDistanceMeters()));
    }

    private String formatDistance(double meters) {
        if (meters < 1_000) {
            return Math.round(meters) + " m";
        }
        return String.format(Locale.US, "%.1f km", meters / 1_000);
    }

    static final class Holder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView type;
        private final TextView distance;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.green_name);
            type = itemView.findViewById(R.id.green_type);
            distance = itemView.findViewById(R.id.green_distance);
        }
    }
}
