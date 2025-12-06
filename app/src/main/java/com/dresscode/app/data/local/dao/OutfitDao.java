package com.dresscode.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dresscode.app.data.local.entity.OutfitEntity;

import java.util.List;

@Dao
public interface OutfitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OutfitEntity> outfits);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OutfitEntity outfit);

    @Query("SELECT * FROM outfits")
    LiveData<List<OutfitEntity>> getAll();

    @Query("SELECT * FROM outfits WHERE id = :id LIMIT 1")
    LiveData<OutfitEntity> getById(int id);

    @Query("SELECT COUNT(*) FROM outfits")
    int getCount();   // 👈 新增

    @Query("SELECT * FROM outfits " +
            "WHERE (:gender = 0 OR gender = :gender OR gender = 0) " +
            "AND (:style IS NULL OR style = :style) " +
            "AND (:season IS NULL OR season = :season) " +
            "AND (:scene IS NULL OR scene = :scene) " +
            "AND (:weather IS NULL OR weather = :weather)")
    LiveData<List<OutfitEntity>> queryFiltered(int gender,
                                               String style,
                                               String season,
                                               String scene,
                                               String weather);

    @Query("SELECT * FROM outfits " +
            "WHERE keyword LIKE :keyword " +
            "OR style LIKE :keyword " +
            "OR scene LIKE :keyword")
    LiveData<List<OutfitEntity>> search(String keyword);
}
