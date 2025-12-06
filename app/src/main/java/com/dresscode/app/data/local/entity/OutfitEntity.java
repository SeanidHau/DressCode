package com.dresscode.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "outfits")
public class OutfitEntity {

    @PrimaryKey
    public int id;

    @NonNull
    public String imageUrl = "";

    // 0 = 通用 / 不限；1 = 男；2 = 女
    public int gender;

    public String style;   // 休闲、通勤...
    public String season;  // 春夏秋冬
    public String scene;   // 约会、校园...
    public String weather; // 热、冷、雨天...
    public String keyword; // 用于搜索（例如：蓬蓬裙、针织衫）

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OutfitEntity)) return false;
        OutfitEntity other = (OutfitEntity) obj;
        return id == other.id
                && gender == other.gender
                && safeEquals(imageUrl, other.imageUrl)
                && safeEquals(style, other.style)
                && safeEquals(season, other.season)
                && safeEquals(scene, other.scene)
                && safeEquals(weather, other.weather)
                && safeEquals(keyword, other.keyword);
    }

    private boolean safeEquals(Object a, Object b) {
        if (a == null) return b == null;
        return a.equals(b);
    }
}
