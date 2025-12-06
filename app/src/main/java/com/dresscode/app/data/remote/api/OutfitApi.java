package com.dresscode.app.data.remote.api;

import com.dresscode.app.data.local.entity.OutfitEntity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// 你可以自己改成真实后端接口；
// 这里先写一个简单的示例
public interface OutfitApi {

    @GET("outfits/list")
    Call<List<OutfitEntity>> getAllOutfits();

    @GET("outfits/search")
    Call<List<OutfitEntity>> searchOutfits(@Query("q") String keyword);
}
