package com.dresscode.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "city",
        indices = {@Index(value = {"queryName"}, unique = true)})
public class CityEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    /** 中文显示名，比如 "上海"、"杭州"、"纽约" */
    @NonNull
    public String displayName;

    /** 用于 OpenWeather 查询的英文名，比如 "Shanghai"、"Hangzhou"、"New York" */
    @NonNull
    public String queryName;

    public double lat;
    public double lng;

    public boolean isCurrent;

    public long lastUsedTime;

    public CityEntity(@NonNull String displayName,
                      @NonNull String queryName,
                      double lat,
                      double lng,
                      boolean isCurrent) {
        this.displayName = displayName;
        this.queryName = queryName;
        this.lat = lat;
        this.lng = lng;
        this.isCurrent = isCurrent;
        this.lastUsedTime = 0L;
    }
}
