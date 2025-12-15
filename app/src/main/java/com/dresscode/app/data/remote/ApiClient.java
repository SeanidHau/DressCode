package com.dresscode.app.data.remote;

import com.dresscode.app.data.remote.api.DressingApi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static volatile ApiClient instance;
    private final Retrofit retrofit;

    private ApiClient() {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .client(client)
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

    /** 调试期强制重建，防止旧 client 复用 */
    public static void resetForDebug() {
        instance = null;
    }

    public DressingApi getDressingApi() {
        return retrofit.create(DressingApi.class);
    }
}