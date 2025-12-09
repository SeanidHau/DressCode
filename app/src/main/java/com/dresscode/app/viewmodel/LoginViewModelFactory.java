package com.dresscode.app.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dresscode.app.data.repository.AuthRepository;
import com.dresscode.app.viewmodel.LoginViewModel;

public class LoginViewModelFactory implements ViewModelProvider.Factory {

    private final AuthRepository authRepository;

    public LoginViewModelFactory(Context context) {
        this.authRepository = AuthRepository.getInstance(context);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(authRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
