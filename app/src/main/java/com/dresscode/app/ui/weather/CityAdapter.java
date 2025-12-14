package com.dresscode.app.ui.weather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dresscode.app.R;
import com.dresscode.app.data.local.entity.CityEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    public interface OnCityClickListener {
        void onCityClick(CityEntity city);
    }

    // ✅ 用于 Activity 控制空态
    public interface OnFilteredListener {
        void onFiltered(int shownCount, String keyword);
    }

    private final List<CityEntity> fullList = new ArrayList<>();
    private final List<CityEntity> displayList = new ArrayList<>();
    private final OnCityClickListener listener;
    private OnFilteredListener filteredListener;

    public CityAdapter(OnCityClickListener listener) {
        this.listener = listener;
    }

    public void setOnFilteredListener(OnFilteredListener l) {
        this.filteredListener = l;
    }

    public void setData(List<CityEntity> cities) {
        fullList.clear();
        if (cities != null) {
            fullList.addAll(cities);
        }
        filter(""); // 默认不过滤
    }

    public void filter(String keyword) {
        displayList.clear();

        String key = keyword == null ? "" : keyword.trim();
        if (key.isEmpty()) {
            displayList.addAll(fullList);
        } else {
            String lower = key.toLowerCase(Locale.getDefault());
            for (CityEntity c : fullList) {
                String q = safeLower(c.queryName);
                String d = safeLower(c.displayName);

                // ✅ 中英文都支持：queryName / displayName 任意命中都行
                if (q.contains(lower) || d.contains(lower)) {
                    displayList.add(c);
                }
            }
        }

        notifyDataSetChanged();
        if (filteredListener != null) {
            filteredListener.onFiltered(displayList.size(), key);
        }
    }

    private String safeLower(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.getDefault());
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        CityEntity city = displayList.get(position);
        holder.bind(city, listener);
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class CityViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvCityDisplay;
        private final TextView tvCityQuery;
        private final TextView tvBadge;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityDisplay = itemView.findViewById(R.id.tvCityDisplay);
            tvCityQuery = itemView.findViewById(R.id.tvCityQuery);
            tvBadge = itemView.findViewById(R.id.tvBadge);
        }

        public void bind(CityEntity city, OnCityClickListener listener) {
            tvCityDisplay.setText(city.displayName);
            tvCityQuery.setText(city.queryName);

            // ✅ 当前城市 badge（CityEntity 里一般是 isCurrent / current）
            boolean isCurrent = false;
            try {
                // 兼容你字段名：如果是 public boolean isCurrent;
                isCurrent = city.isCurrent;
            } catch (Throwable ignore) {
                // 如果你字段叫 current（int/boolean），你在这里按实际改一行即可
                // isCurrent = city.current;
            }

            tvBadge.setVisibility(isCurrent ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCityClick(city);
            });
        }
    }
}
