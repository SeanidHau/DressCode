package com.dresscode.app.ui.weather;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dresscode.app.R;
import com.dresscode.app.data.local.entity.CityEntity;
import com.dresscode.app.viewmodel.WeatherViewModel;

import java.util.ArrayList;
import java.util.List;

public class CitySelectActivity extends AppCompatActivity {

    private WeatherViewModel weatherViewModel;
    private CityAdapter adapter;
    private List<CityEntity> recentCities = new ArrayList<>();
    private List<CityEntity> allCities = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_select);

        weatherViewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(WeatherViewModel.class);

        com.google.android.material.appbar.MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        ImageButton btnClear = findViewById(R.id.btnClear);
        View emptyView = findViewById(R.id.emptyView);
        EditText etSearch = findViewById(R.id.etSearch);
        RecyclerView rvCityList = findViewById(R.id.rvCityList);

        topAppBar.setNavigationOnClickListener(v -> finish());
        btnClear.setOnClickListener(v -> etSearch.setText(""));

        // ✅ 1) 先创建 adapter（否则下面 setOnFilteredListener 会 NPE）
        adapter = new CityAdapter(city -> {
            weatherViewModel.insertCityIfNotExists(city);
            weatherViewModel.setCurrentCity(city);
            finish();
        });

        // ✅ 2) 再绑定 filtered listener
        adapter.setOnFilteredListener((shownCount, keyword) -> {
            if (keyword != null && !keyword.trim().isEmpty() && shownCount == 0) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        });

        rvCityList.setLayoutManager(new LinearLayoutManager(this));
        rvCityList.setAdapter(adapter);

        // ✅ 3) 只保留一个 TextWatcher（不要重复 add）
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = s == null ? "" : s.toString();
                btnClear.setVisibility(key.trim().isEmpty() ? View.GONE : View.VISIBLE);
                adapter.filter(key);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        initBuiltInCitiesOnce();

        weatherViewModel.getRecentCityList().observe(this, cities -> {
            adapter.setData(cities);
            emptyView.setVisibility(View.GONE);
        });

        weatherViewModel.getRecentCityList().observe(this, list -> {
            recentCities.clear();
            if (list != null) recentCities.addAll(list);
            mergeAndShow();
        });

        weatherViewModel.getCityList().observe(this, list -> {
            allCities.clear();
            if (list != null) allCities.addAll(list);
            mergeAndShow();
        });
    }


    /** 每次进页面都跑一遍没关系，Repository 里会检查是否已存在 */
    private void initBuiltInCitiesOnce() {
        List<CityEntity> list = getBuiltInCities();
        for (CityEntity c : list) {
            weatherViewModel.insertCityIfNotExists(c);
        }
    }

    /** 内置城市列表：中文展示 + 英文查询名 */
    private List<CityEntity> getBuiltInCities() {
        List<CityEntity> list = new ArrayList<>();

        // —— 国内常见大城市 ——
        list.add(new CityEntity("北京", "Beijing", 39.9042, 116.4074, false));
        list.add(new CityEntity("上海", "Shanghai", 31.2304, 121.4737, false));
        list.add(new CityEntity("广州", "Guangzhou", 23.1291, 113.2644, false));
        list.add(new CityEntity("深圳", "Shenzhen", 22.5431, 114.0579, false));
        list.add(new CityEntity("杭州", "Hangzhou", 30.2741, 120.1551, false));
        list.add(new CityEntity("成都", "Chengdu", 30.5728, 104.0668, false));
        list.add(new CityEntity("重庆", "Chongqing", 29.5630, 106.5516, false));
        list.add(new CityEntity("武汉", "Wuhan", 30.5928, 114.3055, false));
        list.add(new CityEntity("西安", "Xi'an", 34.3416, 108.9398, false));
        list.add(new CityEntity("南京", "Nanjing", 32.0603, 118.7969, false));
        list.add(new CityEntity("苏州", "Suzhou", 31.2989, 120.5853, false));
        list.add(new CityEntity("青岛", "Qingdao", 36.0662, 120.3826, false));
        list.add(new CityEntity("厦门", "Xiamen", 24.4798, 118.0894, false));
        list.add(new CityEntity("香港", "Hong Kong", 22.3193, 114.1694, false));
        list.add(new CityEntity("台北", "Taipei", 25.0330, 121.5654, false));

        // —— 国外常见大城市 ——
        list.add(new CityEntity("纽约", "New York", 40.7128, -74.0060, false));
        list.add(new CityEntity("伦敦", "London", 51.5074, -0.1278, false));
        list.add(new CityEntity("巴黎", "Paris", 48.8566, 2.3522, false));
        list.add(new CityEntity("东京", "Tokyo", 35.6895, 139.6917, false));
        list.add(new CityEntity("首尔", "Seoul", 37.5665, 126.9780, false));
        list.add(new CityEntity("新加坡", "Singapore", 1.3521, 103.8198, false));
        list.add(new CityEntity("悉尼", "Sydney", -33.8688, 151.2093, false));
        list.add(new CityEntity("洛杉矶", "Los Angeles", 34.0522, -118.2437, false));
        list.add(new CityEntity("旧金山", "San Francisco", 37.7749, -122.4194, false));
        list.add(new CityEntity("柏林", "Berlin", 52.5200, 13.4050, false));

        return list;
    }

    private void mergeAndShow() {
        List<CityEntity> merged = new ArrayList<>();

        // 1. 先加最近使用过的
        for (CityEntity c : recentCities) {
            merged.add(c);
        }

        // 2. 不足 20，用全量城市补（按 queryName 去重）
        if (merged.size() < 20) {
            for (CityEntity c : allCities) {
                if (merged.size() >= 20) break;
                boolean exists = false;
                for (CityEntity e : merged) {
                    if (e.queryName.equalsIgnoreCase(c.queryName)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    merged.add(c);
                }
            }
        }

        adapter.setData(merged);
    }

}
