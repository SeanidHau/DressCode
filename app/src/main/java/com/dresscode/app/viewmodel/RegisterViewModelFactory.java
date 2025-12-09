package com.dresscode.app.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dresscode.app.data.repository.AuthRepository;

public class RegisterViewModelFactory implements ViewModelProvider.Factory {

    private final AuthRepository authRepository;

    public RegisterViewModelFactory(Context context) {
        this.authRepository = AuthRepository.getInstance(context);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RegisterViewModel.class)) {
            return (T) new RegisterViewModel(authRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
