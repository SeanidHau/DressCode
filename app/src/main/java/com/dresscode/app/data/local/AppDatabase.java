package com.dresscode.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.dresscode.app.data.local.dao.CityDao;
import com.dresscode.app.data.local.dao.WeatherDao;
import com.dresscode.app.data.local.entity.CityEntity;
import com.dresscode.app.data.local.entity.WeatherCacheEntity;

@Database(
        entities = {
                CityEntity.class,
                WeatherCacheEntity.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract CityDao cityDao();
    public abstract WeatherDao weatherDao();

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
