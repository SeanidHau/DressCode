package com.dresscode.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dresscode.app.data.local.entity.SearchHistoryEntity;

import java.util.List;

@Dao
public interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY time DESC LIMIT 10")
    LiveData<List<SearchHistoryEntity>> getRecent();

    @Query("SELECT * FROM search_history ORDER BY time DESC LIMIT 20")
    LiveData<List<SearchHistoryEntity>> getRecent20();

    // ✅ 同关键词去重：先删掉旧的
    @Query("DELETE FROM search_history WHERE keyword = :keyword")
    void deleteByKeyword(String keyword);

    @Insert
    void insert(SearchHistoryEntity entity);

    @Query("DELETE FROM search_history")
    void clearAll();

    @Query("DELETE FROM search_history WHERE id NOT IN (SELECT id FROM search_history ORDER BY time DESC LIMIT :keep)")
    void trimTo(int keep);
}

