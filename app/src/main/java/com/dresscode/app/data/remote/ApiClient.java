package com.dresscode.app.data.remote;

import com.dresscode.app.data.remote.api.DressingApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static volatile ApiClient instance;
    private final Retrofit retrofit;

    private ApiClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://your-server-domain.com/") // TODO 修改为你的后端地址
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = new ApiClient();
                }
            }
        }
        return instance;
    }

    public DressingApi getDressingApi() {
        return retrofit.create(DressingApi.class);
    }
}
