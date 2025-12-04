package com.dresscode.app.data.remote.api;

import com.dresscode.app.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    // GET https://api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}&units=metric
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units   // 传 "metric"
    );
}
