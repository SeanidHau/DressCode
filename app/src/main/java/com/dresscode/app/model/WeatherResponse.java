package com.dresscode.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("name")
    public String cityName;

    @SerializedName("dt")
    public long dt; // 秒

    @SerializedName("main")
    public MainInfo main;

    @SerializedName("weather")
    public List<WeatherInfo> weather;

    @SerializedName("wind")
    public WindInfo wind;

    public static class MainInfo {
        @SerializedName("temp") public double temp;
        @SerializedName("feels_like") public double feelsLike;
        @SerializedName("humidity") public int humidity;
        @SerializedName("pressure") public int pressure;
    }

    public static class WeatherInfo {
        @SerializedName("description") public String description;
        @SerializedName("icon") public String icon;
    }

    public static class WindInfo {
        @SerializedName("speed") public double speed;
        @SerializedName("deg") public int deg;
    }
}
