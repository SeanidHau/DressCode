package com.dresscode.app.ui.weather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dresscode.app.R;
import com.dresscode.app.data.local.entity.CityEntity;
import com.dresscode.app.data.local.entity.WeatherCacheEntity;
import com.dresscode.app.viewmodel.WeatherViewModel;
import com.dresscode.app.ui.weather.CitySelectActivity;
import com.dresscode.app.utils.WeatherTextMapper;

public class WeatherFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private WeatherViewModel viewModel;

    private TextView tvCityName;
    private TextView tvTemperature;
    private TextView tvCondition;
    private TextView tvError;
    private ProgressBar progressBar;
    private ImageButton btnLocation;
    private Button btnChangeCity;

    public WeatherFragment() { }

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

        tvCityName = view.findViewById(R.id.tvCityName);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvCondition = view.findViewById(R.id.tvCondition);
        tvError = view.findViewById(R.id.tvError);
        progressBar = view.findViewById(R.id.progressBar);
        btnLocation = view.findViewById(R.id.btnLocation);
        btnChangeCity = view.findViewById(R.id.btnChangeCity);

        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(WeatherViewModel.class);

        viewModel.getCurrentCity().observe(getViewLifecycleOwner(), city -> {
            if (city != null) {
                tvCityName.setText(city.displayName);  // 中文
                viewModel.observeWeatherForCity(city.id);
                observeWeather();
                viewModel.refreshWeather(city);
            } else {
                CityEntity defaultCity = new CityEntity("杭州", "Hangzhou", 0, 0, true);
                viewModel.setCurrentCity(defaultCity);
            }
        });

        // Loading 状态
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });

        // 错误提示
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText(msg);
            } else {
                tvError.setVisibility(View.GONE);
            }
        });

        // 定位按钮：先申请权限，定位逻辑可以后面再补
        btnLocation.setOnClickListener(v -> checkLocationPermission());

        btnChangeCity.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CitySelectActivity.class));
        });
    }

    private void observeWeather() {
        if (viewModel.getCurrentWeather() == null) return;
        viewModel.getCurrentWeather().observe(getViewLifecycleOwner(), weather -> {
            if (weather != null) {
                bindWeather(weather);
            }
        });
    }

    private void bindWeather(WeatherCacheEntity weather) {
        tvTemperature.setText(String.format("%.1f°C", weather.temperature));
        tvCondition.setText(weather.conditionText != null ? WeatherTextMapper.toChinese(weather.conditionText) : "—");
    }

    /** 简单定位权限逻辑，后续你可以接入 FusedLocationProvider */
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "可以开始做定位逻辑（TODO）", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "定位权限已授予，TODO: 开始获取当前位置", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "定位权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
