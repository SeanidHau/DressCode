package com.dresscode.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dresscode.app.data.local.entity.FavoriteEntity;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteEntity favorite);

    @Delete
    void delete(FavoriteEntity favorite);

    // 根据 outfitId 找收藏记录
    @Query("SELECT * FROM favorite WHERE outfitId = :outfitId LIMIT 1")
    FavoriteEntity getByOutfitIdSync(int outfitId);

    @Query("SELECT * FROM favorite")
    LiveData<List<FavoriteEntity>> getAll();
}
