package com.dresscode.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.dresscode.app.data.local.AppDatabase;
import com.dresscode.app.data.local.dao.CityDao;
import com.dresscode.app.data.local.dao.WeatherDao;
import com.dresscode.app.data.local.entity.CityEntity;
import com.dresscode.app.data.local.entity.WeatherCacheEntity;
import com.dresscode.app.data.remote.RetrofitClient;
import com.dresscode.app.data.remote.api.WeatherApi;
import com.dresscode.app.common.ApiKeys;
import com.dresscode.app.model.WeatherResponse;
import com.dresscode.app.utils.WeatherTextMapper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {

    private static WeatherRepository INSTANCE;

    private final CityDao cityDao;
    private final WeatherDao weatherDao;
    private final WeatherApi weatherApi;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private WeatherRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.cityDao = db.cityDao();
        this.weatherDao = db.weatherDao();
        this.weatherApi = RetrofitClient.getInstance().create(WeatherApi.class);
    }

    public static WeatherRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WeatherRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WeatherRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<CityEntity> getCurrentCity() {
        return cityDao.getCurrentCity();
    }

    public LiveData<List<CityEntity>> getAllCities() {
        return cityDao.getAllCities();
    }

    public LiveData<WeatherCacheEntity> getWeatherForCity(int cityId) {
        return weatherDao.getWeatherForCity(cityId);
    }

    public void setCurrentCity(CityEntity city) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();

            // 1) 如果还没入库，先插入（因为 queryName 有 unique index，不会重复）
            long id = city.id;
            if (id == 0) {
                city.lastUsedTime = now;
                id = cityDao.insertCity(city);
                city.id = (int) id;
            } else {
                // 已存在：只更新最近使用时间
                cityDao.touchCity(city.id, now);
            }

            // 2) 切当前标记
            cityDao.clearCurrentFlags();
            cityDao.setCurrentCity(city.id);

            // 3) 再 touch 一下，确保 current 的那条一定在最近列表最前
            cityDao.touchCity(city.id, now);
        });
    }


    public void insertCityIfNotExists(CityEntity city) {
        executor.execute(() -> {
            int count = cityDao.countByQueryName(city.queryName);
            if (count == 0) {
                long id = cityDao.insertCity(city);
                city.id = (int) id;
            }
        });
    }

    public LiveData<List<CityEntity>> getRecentCities() {
        return cityDao.getRecentCities();
    }

    /** 从后端刷新天气并写入 Room */
    public void refreshWeatherForCity(CityEntity city, Runnable onSuccess, Runnable onError) {
        Call<WeatherResponse> call = weatherApi.getCurrentWeather(
                city.queryName,
                ApiKeys.OPEN_WEATHER_API_KEY,
                "metric",
                "zh_cn"
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse body = response.body();

                    double temp = (body.main != null) ? body.main.temp : 0.0;
                    double feels = (body.main != null) ? body.main.feelsLike : temp;
                    int humidity = (body.main != null) ? body.main.humidity : 0;
                    int pressure = (body.main != null) ? body.main.pressure : 0;

                    double wind = (body.wind != null) ? body.wind.speed : 0.0;

                    String desc = null;
                    if (body.weather != null && !body.weather.isEmpty()) {
                        // 已经 lang=zh_cn，这里可以不 mapper；但你保留 mapper 也行
                        desc = body.weather.get(0).description;
                        desc = WeatherTextMapper.toChinese(desc); // 如果你 mapper 会处理中文，就保留
                    }

                    long updateTime = (body.dt > 0) ? body.dt * 1000L : System.currentTimeMillis();

                    WeatherCacheEntity entity = new WeatherCacheEntity(
                            city.id,
                            city.displayName,
                            temp,
                            feels,
                            humidity,
                            wind,
                            pressure,
                            desc,
                            updateTime
                    );

                    executor.execute(() -> {
                        weatherDao.insertOrUpdate(entity);
                        if (onSuccess != null) onSuccess.run();
                    });

                } else {
                    if (onError != null) onError.run();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                if (onError != null) onError.run();
            }
        });
    }

    public void updateCityByLocation(double lat, double lon,
                                     Runnable onSuccess, Runnable onError) {

        Call<WeatherResponse> call = weatherApi.getCurrentWeatherByCoord(
                lat, lon,
                ApiKeys.OPEN_WEATHER_API_KEY,
                "metric",
                "zh_cn"
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse body = response.body();

                    // OpenWeather 返回的城市名是英文，例如 Shanghai、London
                    String queryName = body.cityName;
                    String displayName = WeatherTextMapper.cityToChinese(body.cityName);

                    CityEntity city = new CityEntity(
                            displayName,
                            queryName,
                            lat,
                            lon,
                            true
                    );

                    executor.execute(() -> {
                        city.lastUsedTime = System.currentTimeMillis();
                        long id = cityDao.insertCity(city);
                        city.id = (int) id;

                        cityDao.clearCurrentFlags();
                        cityDao.setCurrentCity(city.id);

                        double temp = (body.main != null) ? body.main.temp : 0.0;
                        double feels = (body.main != null) ? body.main.feelsLike : temp;
                        int humidity = (body.main != null) ? body.main.humidity : 0;
                        int pressure = (body.main != null) ? body.main.pressure : 0;
                        double wind = (body.wind != null) ? body.wind.speed : 0.0;

                        String desc = null;
                        if (body.weather != null && !body.weather.isEmpty()) {
                            desc = body.weather.get(0).description;
                            desc = WeatherTextMapper.toChinese(desc);
                        }

                        long updateTime = (body.dt > 0) ? body.dt * 1000L : System.currentTimeMillis();

                        WeatherCacheEntity cache = new WeatherCacheEntity(
                                city.id,
                                city.displayName,
                                temp,
                                feels,
                                humidity,
                                wind,
                                pressure,
                                desc,
                                updateTime
                        );
                        weatherDao.insertOrUpdate(cache);

                        if (onSuccess != null) onSuccess.run();
                    });
                } else {
                    if (onError != null) onError.run();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                if (onError != null) onError.run();
            }
        });
    }

}
