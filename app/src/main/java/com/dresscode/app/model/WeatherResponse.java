package com.dresscode.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    @SerializedName("name")
    public String cityName;

    @SerializedName("main")
    public MainInfo main;

    @SerializedName("weather")
    public List<WeatherInfo> weather;

    public static class MainInfo {
        @SerializedName("temp")
        public double temp;
    }

    public static class WeatherInfo {
        @SerializedName("description")
        public String description;
    }
}
