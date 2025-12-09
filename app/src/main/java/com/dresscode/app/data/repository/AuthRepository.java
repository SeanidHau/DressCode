package com.dresscode.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * 简单的本地认证仓库，用 SharedPreferences 模拟后端
 */
public class AuthRepository {

    private static final String PREF_NAME = "user_prefs";

    private static AuthRepository INSTANCE;

    private final SharedPreferences prefs;

    private AuthRepository(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AuthRepository(context);
        }
        return INSTANCE;
    }

    // ========== 登录状态 ==========

    public boolean isLoggedIn() {
        return prefs.getBoolean("logged_in", false);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean("logged_in", loggedIn).apply();
    }

    public void logout() {
        prefs.edit().putBoolean("logged_in", false).apply();
    }

    // ========== 注册 / 登录 逻辑 ==========

    /**
     * 注册：简单校验 + 本地保存
     * @return 错误信息；null 代表成功
     */
    public String register(String nickname, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(nickname) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirmPassword)) {
            return "请把信息填写完整";
        }

        if (!password.equals(confirmPassword)) {
            return "两次输入的密码不一致";
        }

        prefs.edit()
                .putString("nickname", nickname)
                .putString("email", email)
                .putString("password", password)
                .apply();

        return null; // null 代表成功
    }

    /**
     * 登录：检查本地是否有对应账号密码
     * @return 错误信息；null 代表成功
     */
    public String login(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            return "邮箱和密码不能为空";
        }

        String savedEmail = prefs.getString("email", null);
        String savedPassword = prefs.getString("password", null);

        if (email.equals(savedEmail) && password.equals(savedPassword)) {
            setLoggedIn(true);
            return null; // 成功
        } else {
            return "账号或密码错误，请先注册";
        }
    }
}
