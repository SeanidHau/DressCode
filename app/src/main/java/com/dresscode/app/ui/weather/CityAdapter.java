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

    private final List<CityEntity> fullList = new ArrayList<>();
    private final List<CityEntity> displayList = new ArrayList<>();
    private final OnCityClickListener listener;

    public CityAdapter(OnCityClickListener listener) {
        this.listener = listener;
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
        if (keyword == null || keyword.trim().isEmpty()) {
            displayList.addAll(fullList);
        } else {
            String lower = keyword.toLowerCase(Locale.getDefault());
            for (CityEntity c : fullList) {
                if (c.queryName.toLowerCase(Locale.getDefault()).contains(lower)) {
                    displayList.add(c);
                }
            }
        }
        notifyDataSetChanged();
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

        private final TextView tvCityName;
        private final TextView tvCitySub;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityNameItem);
            tvCitySub = itemView.findViewById(R.id.tvCitySub);
        }

        public void bind(CityEntity city, OnCityClickListener listener) {
            tvCityName.setText(city.displayName);
            // 如果你以后想加拼音/国家等信息，可以显示在 tvCitySub
            tvCitySub.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCityClick(city);
                }
            });
        }
    }
}
