package com.dresscode.app.ui.dressing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dresscode.app.R;
import com.dresscode.app.data.local.entity.OutfitEntity;

public class FavoriteOutfitAdapter extends ListAdapter<OutfitEntity, FavoriteOutfitAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(OutfitEntity outfit);
    }

    private OnItemClickListener listener;
    private int selectedId = -1;

    public FavoriteOutfitAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setSelectedId(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<OutfitEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<OutfitEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull OutfitEntity oldItem, @NonNull OutfitEntity newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull OutfitEntity oldItem, @NonNull OutfitEntity newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit_favorite, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OutfitEntity outfit = getItem(position);
        Glide.with(holder.ivOutfit.getContext())
                .load(outfit.imageUrl)
                .into(holder.ivOutfit);

        holder.selectionOverlay.setVisibility(
                outfit.id == selectedId ? View.VISIBLE : View.GONE
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(outfit);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivOutfit;
        View selectionOverlay;

        VH(@NonNull View itemView) {
            super(itemView);
            ivOutfit = itemView.findViewById(R.id.ivOutfit);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
        }
    }
}
