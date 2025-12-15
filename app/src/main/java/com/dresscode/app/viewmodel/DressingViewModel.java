package com.dresscode.app.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dresscode.app.data.local.entity.OutfitEntity;
import com.dresscode.app.data.repository.DressingRepository;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DressingViewModel extends AndroidViewModel {

    private MutableLiveData<List<OutfitEntity>> favoriteList = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private MutableLiveData<String> dressingResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private DressingRepository repository;

    private Uri currentCameraPhotoUri;

    public DressingViewModel(@NonNull Application application) {
        super(application);
        repository = new DressingRepository(application);
        loadFavorites();
    }

    public LiveData<List<OutfitEntity>> getFavoriteList() {
        return favoriteList;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getDressingResult() {
        return dressingResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public Uri getCurrentCameraPhotoUri() {
        return currentCameraPhotoUri;
    }

    private void loadFavorites() {
        repository.loadFavoriteOutfits(new DressingRepository.FavoriteLoadCallback() {
            @Override
            public void onLoaded(List<OutfitEntity> list) {
                favoriteList.postValue(list);
            }

            @Override
            public void onError(String msg) {
                errorMessage.postValue(msg);
            }
        });
    }

    public void startDressing(Context context, Uri photoUri, int outfitId) {
        loading.setValue(true);
        repository.uploadAndGenerate(context, photoUri, outfitId, new DressingRepository.Callback() {
            @Override
            public void onSuccess(String url) {
                loading.postValue(false);
                dressingResult.postValue(url);
            }

            @Override
            public void onError(String msg) {
                loading.postValue(false);
                errorMessage.postValue(msg);
            }
        });
    }

    private File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    public Uri createCameraImageUri(Context context) throws IOException {
        File photoFile = createImageFile(context);
        Uri uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                photoFile
        );
        currentCameraPhotoUri = uri;
        return uri;
    }

    public void unfavoriteOutfits(List<Integer> outfitIds) {
        repository.unfavoriteOutfits(
                outfitIds,
                () -> {
                    // 删除后重新加载收藏列表，让页面立刻更新
                    loadFavorites();
                },
                msg -> errorMessage.postValue(msg)
        );
    }

    public void consumeDressingResult() {
        dressingResult.postValue(null);
    }
}
