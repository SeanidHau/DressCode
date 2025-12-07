package com.dresscode.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.dresscode.app.data.local.entity.OutfitEntity;
import com.dresscode.app.data.local.entity.FavoriteEntity;
import com.dresscode.app.data.local.entity.SearchHistoryEntity;
import com.dresscode.app.data.repository.OutfitRepository;
import com.dresscode.app.model.FilterOption;

import java.util.List;

public class FeedViewModel extends ViewModel {

    private final OutfitRepository repository;

    private final MutableLiveData<FilterOption> filterLiveData = new MutableLiveData<>();
    public final LiveData<List<OutfitEntity>> outfitList;
    public final LiveData<List<FavoriteEntity>> favoriteList;
    public final LiveData<List<SearchHistoryEntity>> searchHistoryList;

    public FeedViewModel(OutfitRepository repository) {
        this.repository = repository;

        // 默认筛选
        FilterOption defaultOption = new FilterOption();
        filterLiveData.setValue(defaultOption);

        outfitList = Transformations.switchMap(filterLiveData, repository::getFilteredOutfits);

        favoriteList = repository.getAllFavorites();

        searchHistoryList = repository.getSearchHistory();
    }

    public void updateFilter(FilterOption option) {
        filterLiveData.setValue(option);
    }

    public void toggleFavorite(int outfitId) {
        repository.toggleFavorite(outfitId);
    }

    public LiveData<List<OutfitEntity>> search(String keyword) {
        return repository.search(keyword);
    }

    public void refreshFromRemote() {
        repository.syncAllFromRemote();
    }

    public LiveData<OutfitEntity> getOutfitDetail(int id) {
        return repository.getOutfitDetail(id);
    }

    public void clearSearchHistory() {
        repository.clearSearchHistory();
    }
}
