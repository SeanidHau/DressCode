package com.dresscode.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dresscode.app.data.local.entity.WeatherCacheEntity;

@Dao
public interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(WeatherCacheEntity entity);

    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId LIMIT 1")
    LiveData<WeatherCacheEntity> getWeatherForCity(int cityId);
}
