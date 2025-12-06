package com.dresscode.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "search_history")
public class SearchHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String keyword;

    public long time; // 搜索时间戳
}
