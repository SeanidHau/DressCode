package com.dresscode.app.ui.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Looper;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import android.location.Location;

import com.dresscode.app.R;
import com.dresscode.app.data.local.entity.CityEntity;
import com.dresscode.app.data.local.entity.WeatherCacheEntity;
import com.dresscode.app.viewmodel.WeatherViewModel;
import com.dresscode.app.ui.weather.CitySelectActivity;
import com.dresscode.app.utils.WeatherTextMapper;

public class WeatherFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private WeatherViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
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

        btnLocation.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "点击了定位按钮", Toast.LENGTH_SHORT).show();
            checkLocationPermission();
        });

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

    private void checkLocationPermission() {
        int state = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (state == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "定位权限已授予，开始获取位置", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
        } else {
            Toast.makeText(requireContext(), "请求定位权限", Toast.LENGTH_SHORT).show();
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        }
    }

    @SuppressLint("MissingPermission") // 因为前面已经 check 过权限，压掉 Lint 警告
    private void getCurrentLocation() {
        int state = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (state != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "没有定位权限，getCurrentLocation 直接返回", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Toast.makeText(requireContext(), "getLastLocation 获取到位置", Toast.LENGTH_SHORT).show();
                            handleLocation(location);
                        } else {
                            Toast.makeText(requireContext(), "lastLocation 为 null，尝试主动请求当前位置", Toast.LENGTH_SHORT).show();
                            requestFreshLocation();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "获取 lastLocation 失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException se) {
            Toast.makeText(requireContext(), "SecurityException: " + se.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestFreshLocation() {
        int state = ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        );
        if (state != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(),
                    "没有定位权限，requestLocationUpdates 直接返回",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 构造一次性的位置请求：1 秒一次，只要 1 次更新
        com.google.android.gms.location.LocationRequest request =
                new com.google.android.gms.location.LocationRequest.Builder(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,   // 精度 & 省电平衡
                        1000                                         // 1 秒
                )
                        .setMaxUpdates(1)    // 只要 1 次结果
                        .build();

        Toast.makeText(requireContext(), "开始 requestLocationUpdates", Toast.LENGTH_SHORT).show();

        fusedLocationClient.requestLocationUpdates(
                request,
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(
                            @NonNull com.google.android.gms.location.LocationResult locationResult) {
                        fusedLocationClient.removeLocationUpdates(this); // 拿到就取消订阅

                        if (locationResult == null || locationResult.getLastLocation() == null) {
                            Toast.makeText(requireContext(),
                                    "requestLocationUpdates 仍然没有位置",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Location location = locationResult.getLastLocation();
                        Toast.makeText(requireContext(),
                                "requestLocationUpdates 获取到位置",
                                Toast.LENGTH_SHORT).show();
                        handleLocation(location);
                    }

                    @Override
                    public void onLocationAvailability(
                            @NonNull LocationAvailability availability) {
                        super.onLocationAvailability(availability);
                        if (!availability.isLocationAvailable()) {
                            Toast.makeText(requireContext(),
                                    "当前定位不可用，建议手动选择城市",
                                    Toast.LENGTH_SHORT).show();
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "用户同意了定位权限", Toast.LENGTH_SHORT).show();
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "用户拒绝了定位权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        Toast.makeText(requireContext(),
                "定位成功: " + lat + ", " + lon,
                Toast.LENGTH_SHORT).show();

        // 这里调用你之前写的 updateCityByLocation
        viewModel.updateCityByLocation(lat, lon);
    }
}
