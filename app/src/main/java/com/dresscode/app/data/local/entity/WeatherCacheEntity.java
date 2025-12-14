package com.dresscode.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather_cache")
public class WeatherCacheEntity {

    @PrimaryKey
    public int cityId;      // 对应 CityEntity.id

    public String cityName;
    public double temperature;      // 当前温度
    public String conditionText;    // 晴、多云、小雨等
    public long updateTime;         // 更新时间：毫秒

    // ✅ 新增：用于更饱满的 UI
    public double feelsLike;        // 体感温度
    public int humidity;            // 湿度 %
    public double windSpeed;        // 风速 m/s
    public int pressure;            // 气压 hPa

    // 兼容旧构造器（你现有代码不需要立刻全改）
    @Ignore
    public WeatherCacheEntity(int cityId, String cityName,
                              double temperature, String conditionText,
                              long updateTime) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.temperature = temperature;
        this.conditionText = conditionText;
        this.updateTime = updateTime;
    }

    // ✅ 可选：新构造器（方便一次性写全）
    public WeatherCacheEntity(int cityId, String cityName,
                              double temperature, double feelsLike,
                              int humidity, double windSpeed, int pressure,
                              String conditionText, long updateTime) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.conditionText = conditionText;
        this.updateTime = updateTime;
    }
}
