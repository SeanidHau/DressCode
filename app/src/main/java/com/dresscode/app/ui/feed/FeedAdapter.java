package com.dresscode.app.ui.feed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dresscode.app.R;
import com.dresscode.app.data.local.entity.OutfitEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeedAdapter extends ListAdapter<OutfitEntity, FeedAdapter.ViewHolder> {

    private final Set<Integer> favoriteIds = new HashSet<>();

    public void setFavoriteIds(Collection<Integer> ids) {
        favoriteIds.clear();
        favoriteIds.addAll(ids);
        notifyDataSetChanged();
    }

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

        // 1. 先清空旧的 tag
        holder.tagContainer.removeAllViews();

        // 2. 根据数据拼出标签列表（示例，按你实际字段改）
        List<String> tags = new ArrayList<>();
        if (item.style != null && !item.style.isEmpty())  tags.add(item.style);
        if (item.scene != null && !item.scene.isEmpty())  tags.add(item.scene);
        if (item.season != null && !item.season.isEmpty()) tags.add(item.season);

        // 最多显示 3 个，避免太挤
        int maxTags = Math.min(tags.size(), 3);

        for (int i = 0; i < maxTags; i++) {
            String tag = tags.get(i);

            TextView chip = new TextView(ctx);
            chip.setText("#" + tag);
            chip.setTextSize(13);
            chip.setTextColor(ContextCompat.getColor(ctx, R.color.dc_text_primary));
            chip.setTypeface(chip.getTypeface(), Typeface.BOLD);
            chip.setBackgroundResource(R.drawable.bg_tag_chip);

            int padV = dpToPx(4);
            int padH = dpToPx(10);
            chip.setPadding(padH, padV, padH, padV);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) {
                lp.setMarginStart(dpToPx(6));   // 标签之间间距
            }
            chip.setLayoutParams(lp);

            holder.tagContainer.addView(chip);
        }

        // ✅ 根据是否在 favoriteIds 里，决定星星亮不亮
        boolean isFav = favoriteIds.contains(item.id);
        holder.favoriteBtn.setImageResource(
                isFav ? R.drawable.ic_star_filled
                        : R.drawable.ic_star_border
        );
        // 点星星：UI 先本地切一下，然后再通知 ViewModel 改数据库
        holder.favoriteBtn.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            OutfitEntity current = getItem(pos);

            boolean newState = !favoriteIds.contains(current.id);
            if (newState) {
                favoriteIds.add(current.id);
            } else {
                favoriteIds.remove(current.id);
            }

            // ⭐ 播放星星动画（放大 → 回弹）
            holder.favoriteBtn.animate()
                    .scaleX(1.25f).scaleY(1.25f)
                    .setDuration(120)
                    .withEndAction(() -> holder.favoriteBtn.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(120)
                    );

            // UI 更新星星图标
            notifyItemChanged(pos);

            // 通知 ViewModel 更新收藏逻辑
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(current.id);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = OutfitDetailActivity.newIntent(ctx, item.id);
            ctx.startActivity(intent);
            if (ctx instanceof Activity) {
                ((Activity) ctx).overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton favoriteBtn;
        LinearLayout tagContainer;   // ✅ 用来装多个标签

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageOutfit);
            favoriteBtn = itemView.findViewById(R.id.btnFavorite);
            tagContainer = itemView.findViewById(R.id.tagContainer);  // ✅ 新的 id
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
