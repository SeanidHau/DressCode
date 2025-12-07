package com.dresscode.app.ui.feed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dresscode.app.R;
import com.dresscode.app.data.local.entity.SearchHistoryEntity;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(String keyword);
    }

    private final List<SearchHistoryEntity> data = new ArrayList<>();
    private final OnItemClickListener listener;

    public SearchHistoryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SearchHistoryEntity> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SearchHistoryEntity item = data.get(position);
        holder.tvKeyword.setText(item.keyword);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item.keyword);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvKeyword;

        VH(@NonNull View itemView) {
            super(itemView);
            tvKeyword = itemView.findViewById(R.id.tvKeyword);
        }
    }
}
