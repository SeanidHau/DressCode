package com.dresscode.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.dresscode.app.data.local.entity.CityEntity;
import com.dresscode.app.data.local.entity.WeatherCacheEntity;
import com.dresscode.app.data.repository.WeatherRepository;

import java.util.List;

public class WeatherViewModel extends AndroidViewModel {

    private final WeatherRepository repository;

    private final LiveData<CityEntity> currentCity;
    private final LiveData<List<CityEntity>> cityList;

    private final MutableLiveData<Integer> currentCityId = new MutableLiveData<>();
    private final LiveData<WeatherCacheEntity> currentWeather;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        repository = WeatherRepository.getInstance(application);
        currentCity = repository.getCurrentCity();
        cityList = repository.getAllCities();

        // ✅ 关键：在构造函数里初始化，避免 repository 未初始化问题
        currentWeather = Transformations.switchMap(
                currentCityId,
                cityId -> repository.getWeatherForCity(cityId)
        );
    }

    public LiveData<CityEntity> getCurrentCity() {
        return currentCity;
    }

    public LiveData<List<CityEntity>> getCityList() {
        return cityList;
    }

    public LiveData<WeatherCacheEntity> getCurrentWeather() {
        return currentWeather;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    /** 切换当前城市时调用 */
    public void observeWeatherForCity(int cityId) {
        currentCityId.setValue(cityId);
    }

    public void insertCityIfNotExists(CityEntity city) {
        repository.insertCityIfNotExists(city);
    }

    /** 刷新天气（远端 → Room） */
    public void refreshWeather(CityEntity city) {
        loading.setValue(true);
        error.setValue(null);
        repository.refreshWeatherForCity(
                city,
                () -> loading.postValue(false),
                () -> {
                    loading.postValue(false);
                    error.postValue("刷新天气失败");
                }
        );
    }

    public void updateCityByLocation(double lat, double lon) {
        loading.setValue(true);
        error.setValue(null);
        repository.updateCityByLocation(
                lat, lon,
                () -> loading.postValue(false),
                () -> {
                    loading.postValue(false);
                    error.postValue("定位获取天气失败");
                }
        );
    }

    public void setCurrentCity(CityEntity city) {
        repository.setCurrentCity(city);
    }

    public LiveData<List<CityEntity>> getRecentCityList() {
        return repository.getRecentCities();
    }

}
