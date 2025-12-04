package com.dresscode.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "city")
public class CityEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;

    public double lat;
    public double lng;

    public boolean isCurrent;

    public CityEntity(@NonNull String name, double lat, double lng, boolean isCurrent) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.isCurrent = isCurrent;
    }
}
