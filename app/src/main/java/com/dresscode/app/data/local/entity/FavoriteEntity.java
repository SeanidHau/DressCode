package com.dresscode.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite")
public class FavoriteEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int outfitId;

    public long favoriteTime; // 时间戳
}
