package com.dresscode.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.dresscode.app.data.local.dao.CityDao;
import com.dresscode.app.data.local.dao.WeatherDao;
import com.dresscode.app.data.local.dao.OutfitDao;
import com.dresscode.app.data.local.dao.FavoriteDao;
import com.dresscode.app.data.local.dao.SearchHistoryDao;
import com.dresscode.app.data.local.dao.UserProfileDao;

import com.dresscode.app.data.local.entity.CityEntity;
import com.dresscode.app.data.local.entity.WeatherCacheEntity;
import com.dresscode.app.data.local.entity.OutfitEntity;
import com.dresscode.app.data.local.entity.FavoriteEntity;
import com.dresscode.app.data.local.entity.SearchHistoryEntity;
import com.dresscode.app.data.local.entity.UserProfileEntity;

@Database(
        entities = {
                // 天气模块
                CityEntity.class,
                WeatherCacheEntity.class,

                // 穿搭模块
                OutfitEntity.class,
                FavoriteEntity.class,
                SearchHistoryEntity.class,

                // 智能换装模块


                // 设置模块
                UserProfileEntity.class
        },
        version = 4,   // ⚠️版本号必须更新
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // 天气模块 Dao
    public abstract CityDao cityDao();
    public abstract WeatherDao weatherDao();

    // 穿搭模块 Dao
    public abstract OutfitDao outfitDao();
    public abstract FavoriteDao favoriteDao();
    public abstract SearchHistoryDao searchHistoryDao();

    // 设置模块 Dao
    public abstract UserProfileDao userProfileDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "dresscode-db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
