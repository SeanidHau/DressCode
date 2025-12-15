package com.dresscode.app.data.repository;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.dresscode.app.data.local.AppDatabase;
import com.dresscode.app.data.local.dao.FavoriteDao;
import com.dresscode.app.data.local.entity.OutfitEntity;
import com.dresscode.app.data.remote.ApiClient;
import com.dresscode.app.data.remote.api.DressingApi;
import com.dresscode.app.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DressingRepository {

    public interface FavoriteLoadCallback {
        void onLoaded(List<OutfitEntity> list);
        void onError(String msg);
    }

    public interface Callback {
        void onSuccess(String url);
        void onError(String msg);
    }

    private final AppDatabase db;
    private final FavoriteDao favoriteDao;
    private final DressingApi api;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DressingRepository(Context context) {
        Context appCtx = context.getApplicationContext();
        db = AppDatabase.getInstance(appCtx);
        favoriteDao = db.favoriteDao();
        api = ApiClient.getInstance().getDressingApi();
    }

    public void loadFavoriteOutfits(FavoriteLoadCallback callback) {
        executor.execute(() -> {
            try {
                List<OutfitEntity> list = favoriteDao.getFavoriteOutfitsWithDetail();
                callback.onLoaded(list);
            } catch (Exception e) {
                callback.onError("加载收藏失败: " + e.getMessage());
            }
        });
    }

    public void uploadAndGenerate(Context context, Uri photoUri, int outfitId, Callback callback) {
        File file;
        try {
            file = FileUtils.uriToFile(context, photoUri);
            android.util.Log.d("DressingRepo", "upload file=" + file.getAbsolutePath() + " size=" + file.length());
        } catch (IOException e) {
            callback.onError("无法读取图片: " + e.getMessage());
            return;
        }

        RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part photoPart =
                MultipartBody.Part.createFormData("photo", file.getName(), imageBody);

        RequestBody outfitPart =
                RequestBody.create(String.valueOf(outfitId), MediaType.parse("text/plain"));

        api.generateDressing(photoPart, outfitPart)
                .enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String bodyStr = response.body().string();

                                android.util.Log.d(
                                        "DressingRepo",
                                        "HTTP " + response.code() + " body head=" +
                                                (bodyStr.length() > 120
                                                        ? bodyStr.substring(0, 120)
                                                        : bodyStr)
                                );

                                callback.onSuccess(bodyStr);

                            } catch (IOException e) {
                                android.util.Log.e("DressingRepo", "read body failed", e);
                                callback.onError("解析返回结果失败");
                            }
                        } else {
                            callback.onError("换装失败，错误码：" + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        android.util.Log.e("DressingRepo", "onFailure", t);
                        callback.onError("网络请求失败: " + t.getClass().getSimpleName() + " / " + t.getMessage());
                    }
                });
    }

    public void unfavoriteOutfits(List<Integer> outfitIds, Runnable onDone, java.util.function.Consumer<String> onError) {
        executor.execute(() -> {
            try {
                if (outfitIds == null || outfitIds.isEmpty()) {
                    if (onDone != null) onDone.run();
                    return;
                }
                favoriteDao.deleteByOutfitIds(outfitIds);
                if (onDone != null) onDone.run();
            } catch (Exception e) {
                if (onError != null) onError.accept("取消收藏失败: " + e.getMessage());
            }
        });
    }
}
