package com.dresscode.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dresscode.app.data.repository.AuthRepository;
import com.dresscode.app.ui.login.AuthUiState;

public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<AuthUiState> _loginState = new MutableLiveData<>(AuthUiState.idle());
    public LiveData<AuthUiState> loginState = _loginState;

    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void login(String email, String password) {
        _loginState.setValue(AuthUiState.loading());

        String error = authRepository.login(email, password);
        if (error == null) {
            _loginState.setValue(AuthUiState.success("登录成功"));
        } else {
            _loginState.setValue(AuthUiState.error(error));
        }
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }
}
