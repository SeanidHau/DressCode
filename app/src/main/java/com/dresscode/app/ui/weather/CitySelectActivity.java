package com.dresscode.app.ui.weather;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_select);

        // ViewModel
        weatherViewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(WeatherViewModel.class);

        ImageButton btnBack = findViewById(R.id.btnBack);
        EditText etSearch = findViewById(R.id.etSearch);
        RecyclerView rvCityList = findViewById(R.id.rvCityList);

        // RecyclerView 设置
        adapter = new CityAdapter(city -> {
            // 确保写入数据库
            weatherViewModel.insertCityIfNotExists(city);
            // 设为当前城市
            weatherViewModel.setCurrentCity(city);
            // 关闭页面
            finish();
        });
        rvCityList.setLayoutManager(new LinearLayoutManager(this));
        rvCityList.setAdapter(adapter);

        // ✅ 关键点：先把内置城市全部写入数据库（不会重复）
        initBuiltInCitiesOnce();

        // 然后永远监听 DB 里的城市列表
        weatherViewModel.getCityList().observe(this, cities -> {
            if (cities != null) {
                adapter.setData(cities);
            }
        });

        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 搜索框监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
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
}
