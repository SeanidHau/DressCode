package com.dresscode.app.ui.feed;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dresscode.app.R;
import com.dresscode.app.data.local.entity.OutfitEntity;

public class FeedAdapter extends ListAdapter<OutfitEntity, FeedAdapter.ViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(int outfitId);
    }

    private final OnFavoriteClickListener favoriteClickListener;

    public FeedAdapter(OnFavoriteClickListener listener) {
        super(DIFF_CALLBACK);
        this.favoriteClickListener = listener;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OutfitEntity item = getItem(position);
        Context ctx = holder.itemView.getContext();

        Glide.with(ctx)
                .load(item.imageUrl)
                .into(holder.imageView);

        holder.favoriteBtn.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(item.id);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = OutfitDetailActivity.newIntent(ctx, item.id);
            ctx.startActivity(intent);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton favoriteBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.outfitImage);
            favoriteBtn = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
