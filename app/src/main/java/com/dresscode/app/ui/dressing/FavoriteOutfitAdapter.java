package com.dresscode.app.ui.dressing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    public interface OnCheckedChangeListener {
        void onCheckedCountChanged(int count);
    }

    private final OnItemClickListener listener;
    private OnCheckedChangeListener checkedListener;

    private int selectedId = -1;

    private boolean manageMode = false;
    private final java.util.Set<Integer> checkedIds = new java.util.HashSet<>();

    public FavoriteOutfitAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
        this.checkedListener = l;
    }

    public void setManageMode(boolean enable) {
        manageMode = enable;
        if (!manageMode) checkedIds.clear();
        notifyDataSetChanged();
        if (checkedListener != null) checkedListener.onCheckedCountChanged(checkedIds.size());
    }

    public boolean isManageMode() { return manageMode; }

    public void toggleChecked(int id) {
        if (checkedIds.contains(id)) checkedIds.remove(id);
        else checkedIds.add(id);
        notifyDataSetChanged();
        if (checkedListener != null) checkedListener.onCheckedCountChanged(checkedIds.size());
    }

    public java.util.List<Integer> getCheckedIds() {
        return new java.util.ArrayList<>(checkedIds);
    }

    public void clearChecked() {
        checkedIds.clear();
        notifyDataSetChanged();
        if (checkedListener != null) checkedListener.onCheckedCountChanged(0);
    }

    public void setSelectedId(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_outfit, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OutfitEntity outfit = getItem(position);

        // 卡片选中态：正常模式用 selectedId；管理模式用 checkedIds
        boolean selected = manageMode ? checkedIds.contains(outfit.id) : (outfit.id == selectedId);
        holder.itemView.setSelected(selected);

        Glide.with(holder.ivCover.getContext())
                .load(outfit.imageUrl)
                .into(holder.ivCover);

        String title = safe(outfit.keyword);
        if (title.isEmpty()) title = safe(outfit.style);
        holder.tvTitle.setText(title.isEmpty() ? "收藏穿搭" : title);

        holder.tvMeta.setText(composeMeta(outfit));

        // 管理勾选 UI
        if (manageMode) {
            holder.manageOverlay.setVisibility(selected ? View.VISIBLE : View.GONE);
            holder.manageCheck.setVisibility(selected ? View.VISIBLE : View.GONE);
        } else {
            holder.manageOverlay.setVisibility(View.GONE);
            holder.manageCheck.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (manageMode) {
                toggleChecked(outfit.id);
            } else {
                setSelectedId(outfit.id);
                if (listener != null) listener.onItemClick(outfit);
            }
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvMeta;

        View manageOverlay;
        TextView manageCheck;

        VH(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMeta = itemView.findViewById(R.id.tvMeta);

            manageOverlay = itemView.findViewById(R.id.manageCheckOverlay);
            manageCheck = itemView.findViewById(R.id.tvManageCheck);
        }
    }

    private static String composeMeta(OutfitEntity o) {
        String a = safe(o.season);
        String b = safe(o.scene);
        String c = safe(o.weather);

        StringBuilder sb = new StringBuilder();
        if (!a.isEmpty()) sb.append(a);
        if (!b.isEmpty()) { if (sb.length() > 0) sb.append(" · "); sb.append(b); }
        if (sb.length() == 0 && !c.isEmpty()) sb.append(c);
        return sb.length() == 0 ? "点击选择" : sb.toString();
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static final DiffUtil.ItemCallback<OutfitEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<OutfitEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull OutfitEntity oldItem,
                                               @NonNull OutfitEntity newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull OutfitEntity oldItem,
                                                  @NonNull OutfitEntity newItem) {
                    return oldItem.equals(newItem);
                }
            };
}

