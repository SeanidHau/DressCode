package com.dresscode.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dresscode.app.data.repository.AuthRepository;
import com.dresscode.app.ui.login.AuthUiState;

public class RegisterViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<AuthUiState> _registerState = new MutableLiveData<>(AuthUiState.idle());
    public LiveData<AuthUiState> registerState = _registerState;

    public RegisterViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void register(String nickname, String email, String password, String confirmPassword) {
        _registerState.setValue(AuthUiState.loading());

        String error = authRepository.register(nickname, email, password, confirmPassword);
        if (error == null) {
            _registerState.setValue(AuthUiState.success("注册成功，请使用该账号登录"));
        } else {
            _registerState.setValue(AuthUiState.error(error));
        }
    }
}
