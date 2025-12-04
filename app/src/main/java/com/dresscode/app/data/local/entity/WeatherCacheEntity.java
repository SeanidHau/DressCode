package com.dresscode.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather_cache")
public class WeatherCacheEntity {

    @PrimaryKey
    public int cityId;      // 对应 CityEntity.id

    public String cityName;
    public double temperature;      // 当前温度
    public String conditionText;    // 晴、多云、小雨等
    public long updateTime;         // 更新时间：System.currentTimeMillis()

    public WeatherCacheEntity(int cityId, String cityName,
                              double temperature, String conditionText,
                              long updateTime) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.temperature = temperature;
        this.conditionText = conditionText;
        this.updateTime = updateTime;
    }
}
