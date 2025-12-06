package com.dresscode.app.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dresscode.app.data.repository.OutfitRepository;

public class FeedViewModelFactory implements ViewModelProvider.Factory {

    private final OutfitRepository repository;

    public FeedViewModelFactory(OutfitRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FeedViewModel.class)) {
            return (T) new FeedViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
