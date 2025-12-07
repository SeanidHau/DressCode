package com.dresscode.app.data.repository;

import androidx.lifecycle.LiveData;

import com.dresscode.app.data.local.dao.FavoriteDao;
import com.dresscode.app.data.local.dao.OutfitDao;
import com.dresscode.app.data.local.dao.SearchHistoryDao;
import com.dresscode.app.data.local.entity.FavoriteEntity;
import com.dresscode.app.data.local.entity.OutfitEntity;
import com.dresscode.app.data.local.entity.SearchHistoryEntity;
import com.dresscode.app.data.remote.api.OutfitApi;
import com.dresscode.app.model.FilterOption;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class OutfitRepository {

    private final OutfitDao outfitDao;
    private final FavoriteDao favoriteDao;
    private final SearchHistoryDao searchHistoryDao;
    private final OutfitApi outfitApi;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public OutfitRepository(OutfitDao outfitDao,
                            FavoriteDao favoriteDao,
                            SearchHistoryDao searchHistoryDao,
                            OutfitApi outfitApi) {
        this.outfitDao = outfitDao;
        this.favoriteDao = favoriteDao;
        this.searchHistoryDao = searchHistoryDao;
        this.outfitApi = outfitApi;

    }

    // 从本地（Room）获取过滤后的列表
    public LiveData<List<OutfitEntity>> getFilteredOutfits(FilterOption option) {
        int gender = option.gender;
        String style = emptyToNull(option.style);
        String season = emptyToNull(option.season);
        String scene = emptyToNull(option.scene);
        String weather = emptyToNull(option.weather);
        return outfitDao.queryFiltered(gender, style, season, scene, weather);
    }

    // 搜索（本地 Room + 可选同步后端）
    public LiveData<List<OutfitEntity>> search(String keyword) {
        // 记录搜索历史
        executor.execute(() -> {
            SearchHistoryEntity history = new SearchHistoryEntity();
            history.keyword = keyword;
            history.time = System.currentTimeMillis();
            searchHistoryDao.insert(history);

            // 如果你有后端，可以在这里同步一下最新搜索结果：
            // syncFromRemote(keyword);
        });

        String pattern = "%" + keyword + "%";
        return outfitDao.search(pattern);
    }

    // 收藏 / 取消收藏
    public void toggleFavorite(int outfitId) {
        executor.execute(() -> {
            FavoriteEntity exist = favoriteDao.getByOutfitIdSync(outfitId);
            if (exist == null) {
                FavoriteEntity f = new FavoriteEntity();
                f.outfitId = outfitId;
                f.favoriteTime = System.currentTimeMillis();
                favoriteDao.insert(f);
            } else {
                favoriteDao.delete(exist);
            }
        });
    }

    public LiveData<List<OutfitEntity>> getAllOutfits() {
        return outfitDao.getAll();
    }

    public LiveData<OutfitEntity> getOutfitDetail(int id) {
        return outfitDao.getById(id);
    }

    // 可选：从服务器同步（不需要的话可以删掉）
    public void syncAllFromRemote() {
        executor.execute(() -> {
            try {
                Response<List<OutfitEntity>> resp = outfitApi.getAllOutfits().execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    outfitDao.insertAll(resp.body());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String emptyToNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        return s;
    }

    public LiveData<List<FavoriteEntity>> getAllFavorites() {
        return favoriteDao.getAll();
    }

    public void clearSearchHistory() {
        executor.execute(() -> searchHistoryDao.clearAll());
    }

    public LiveData<List<SearchHistoryEntity>> getSearchHistory() {
        return searchHistoryDao.getRecent();
    }
}
