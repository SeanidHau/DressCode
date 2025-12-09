package com.dresscode.app.ui.login;

/**
 * 简单的 UI 状态封装：是否成功、是否加载中、错误提示
 */
public class AuthUiState {
    public boolean success;
    public boolean loading;
    public String message; // 用于错误或提示信息

    public AuthUiState(boolean success, boolean loading, String message) {
        this.success = success;
        this.loading = loading;
        this.message = message;
    }

    public static AuthUiState idle() {
        return new AuthUiState(false, false, null);
    }

    public static AuthUiState loading() {
        return new AuthUiState(false, true, null);
    }

    public static AuthUiState success(String msg) {
        return new AuthUiState(true, false, msg);
    }

    public static AuthUiState error(String msg) {
        return new AuthUiState(false, false, msg);
    }
}
