package com.dresscode.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dresscode.app.data.local.entity.CityEntity;

import java.util.List;

@Dao
public interface CityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCity(CityEntity city);

    @Update
    void updateCity(CityEntity city);

    @Query("SELECT * FROM city ORDER BY isCurrent DESC, name ASC")
    LiveData<List<CityEntity>> getAllCities();

    @Query("SELECT * FROM city WHERE isCurrent = 1 LIMIT 1")
    LiveData<CityEntity> getCurrentCity();

    @Query("UPDATE city SET isCurrent = 0")
    void clearCurrentFlags();

    @Query("UPDATE city SET isCurrent = 1 WHERE id = :cityId")
    void setCurrentCity(int cityId);
}
