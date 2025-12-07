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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SearchHistoryEntity entity);

    @Query("SELECT * FROM search_history ORDER BY time DESC LIMIT 10")
    LiveData<List<SearchHistoryEntity>> getRecent();

    @Query("DELETE FROM search_history")
    void clearAll();

}
