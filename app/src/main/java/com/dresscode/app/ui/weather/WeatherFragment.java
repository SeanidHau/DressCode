package com.dresscode.app.ui.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dresscode.app.R;
import com.dresscode.app.data.local.entity.CityEntity;
import com.dresscode.app.data.local.entity.WeatherCacheEntity;
import com.dresscode.app.ui.weather.CitySelectActivity;
import com.dresscode.app.utils.WeatherTextMapper;
import com.dresscode.app.viewmodel.WeatherViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class WeatherFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final String TAG = "WeatherFragment";

    private WeatherViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvCityName;
    private TextView tvTemperature;
    private TextView tvCondition;
    private TextView tvError;
    private ProgressBar progressBar;
    private ImageButton btnLocation;
    private Button btnChangeCity;
    private TextView tvMeta, tvFeelsLike, tvHumidity, tvWind, tvClothingHint, tvOutfitTip;
    private Button btnGoFeed;

    public interface Navigator {
        void navigateToFeed();
    }

    private Navigator navigator;

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof Navigator) {
            navigator = (Navigator) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigator = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireContext());

        tvCityName = view.findViewById(R.id.tvCityName);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvCondition = view.findViewById(R.id.tvCondition);
        tvError = view.findViewById(R.id.tvError);
        progressBar = view.findViewById(R.id.progressBar);
        btnLocation = view.findViewById(R.id.btnLocation);
        btnChangeCity = view.findViewById(R.id.btnChangeCity);
        tvMeta = view.findViewById(R.id.tvMeta);
        tvFeelsLike = view.findViewById(R.id.tvFeelsLike);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvWind = view.findViewById(R.id.tvWind);
        tvClothingHint = view.findViewById(R.id.tvClothingHint);
        tvOutfitTip = view.findViewById(R.id.tvOutfitTip);
        btnGoFeed = view.findViewById(R.id.btnGoFeed);

        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
        ).get(WeatherViewModel.class);

        viewModel.getCurrentWeather().observe(
                getViewLifecycleOwner(),
                weather -> {
                    if (weather != null) {
                        bindWeather(weather);
                    }
                }
        );

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                progressBar.setVisibility(Boolean.TRUE.equals(loading)
                        ? View.VISIBLE
                        : View.GONE)
        );

        viewModel.getError().observe(getViewLifecycleOwner(), this::showStatus);

        btnLocation.setOnClickListener(v -> checkLocationPermission());

        btnChangeCity.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CitySelectActivity.class))
        );

        btnGoFeed.setOnClickListener(v -> {
            if (navigator != null) {
                navigator.navigateToFeed();
            } else {
                log("Host activity does not implement WeatherFragment.Navigator");
            }
        });

        viewModel.getCurrentCity().observe(getViewLifecycleOwner(), city -> {
            if (city != null) {
                tvCityName.setText(city.displayName);

                // ✅ 关键：切换 currentWeather 数据源（Room 监听立刻换到新 cityId）
                viewModel.observeWeatherForCity(city.id);

                // ✅ 关键：触发刷新（远端→Room），Room 更新会立刻推给 currentWeather observer
                viewModel.refreshWeather(city);
            } else {
                CityEntity defaultCity = new CityEntity("杭州", "Hangzhou", 0, 0, true);
                viewModel.setCurrentCity(defaultCity);
            }
        });

    }

    private void observeWeather() {
        if (viewModel.getCurrentWeather() == null) {
            log("getCurrentWeather() is null (not ready yet)");
            return;
        }
        viewModel.getCurrentWeather().observe(getViewLifecycleOwner(), weather -> {
            if (weather != null) bindWeather(weather);
        });
    }

    private void bindWeather(WeatherCacheEntity w) {
        // 主信息
        tvTemperature.setText(String.format("%.1f°C", w.temperature));

        // 你的 Repository 已经可能存中文了，这里不要重复 toChinese（会导致“中文再翻译”）
        // 保险做法：如果包含英文字母再 mapper
        String cond = (w.conditionText == null || w.conditionText.trim().isEmpty()) ? "—" : w.conditionText;
        if (containsAsciiLetter(cond)) {
            cond = WeatherTextMapper.toChinese(cond);
        }
        tvCondition.setText(cond);

        // 更新时间（你现在字段名是 updateTime，毫秒）
        if (w.updateTime > 0) {
            tvMeta.setText("更新于 " + formatTimeMillis(w.updateTime));
        } else {
            tvMeta.setText("刚刚更新");
        }

        // 指标四宫格
        double feels = (w.feelsLike != 0.0) ? w.feelsLike : w.temperature;
        tvFeelsLike.setText(String.format("%.1f°C", feels));

        tvHumidity.setText(w.humidity > 0 ? (w.humidity + "%") : "—");
        tvWind.setText(w.windSpeed > 0 ? String.format("%.1f m/s", w.windSpeed) : "—");

        // 穿衣建议（用温度）
        String clothing = getClothingHint(w.temperature);
        tvClothingHint.setText(clothing);

        // 穿搭建议文案：克制、短句
        tvOutfitTip.setText("建议：" + clothing + "。当前「" + cond + "」，以舒适与层次为主。");
    }

    /* ---------------- 定位流程 ---------------- */

    private void checkLocationPermission() {
        int state = ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        );

        if (state == PackageManager.PERMISSION_GRANTED) {
            showStatus("正在获取当前位置…");
            getCurrentLocation();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        int state = ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        );
        if (state != PackageManager.PERMISSION_GRANTED) {
            log("getCurrentLocation called without permission");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        handleLocation(location);
                    } else {
                        showStatus("正在获取当前位置…");
                        requestFreshLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    showStatus("定位失败，请手动选择城市");
                    log("getLastLocation failed: " + e.getMessage());
                });
    }

    @SuppressLint("MissingPermission")
    private void requestFreshLocation() {
        int state = ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        );
        if (state != PackageManager.PERMISSION_GRANTED) return;

        com.google.android.gms.location.LocationRequest request =
                new com.google.android.gms.location.LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        1000
                )
                        .setMaxUpdates(1)
                        .build();

        fusedLocationClient.requestLocationUpdates(
                request,
                new com.google.android.gms.location.LocationCallback() {

                    @Override
                    public void onLocationResult(
                            @NonNull com.google.android.gms.location.LocationResult result) {
                        fusedLocationClient.removeLocationUpdates(this);
                        if (result.getLastLocation() != null) {
                            handleLocation(result.getLastLocation());
                        } else {
                            showStatus("定位失败，请手动选择城市");
                        }
                    }

                    @Override
                    public void onLocationAvailability(
                            @NonNull LocationAvailability availability) {
                        if (!availability.isLocationAvailable()) {
                            showStatus("当前定位不可用，请手动选择城市");
                        }
                    }
                },
                Looper.getMainLooper()
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showStatus("正在获取当前位置…");
                getCurrentLocation();
            } else {
                showStatus("未授予定位权限，请手动选择城市");
            }
        }
    }

    private void handleLocation(Location location) {
        showStatus(null);
        log("Location: " + location.getLatitude() + ", " + location.getLongitude());
        viewModel.updateCityByLocation(
                location.getLatitude(),
                location.getLongitude()
        );
    }

    /* ---------------- UI & Log ---------------- */

    private void showStatus(@Nullable String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            tvError.setVisibility(View.GONE);
        } else {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(msg);
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }

    private String formatTimeMillis(long ms) {
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(ms));
    }

    private String getClothingHint(double t) {
        if (t >= 28) return "短袖 / 薄衬衫";
        if (t >= 20) return "薄外套 / 长袖";
        if (t >= 12) return "风衣 / 针织衫";
        return "厚外套 / 围巾";
    }

    private boolean containsAsciiLetter(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) return true;
        }
        return false;
    }

}
