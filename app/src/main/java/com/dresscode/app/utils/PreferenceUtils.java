package com.dresscode.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dresscode.app.model.UserSettings;

public class PreferenceUtils {
    private static final String PREF_NAME = "dresscode_pref";
    public static final String KEY_GENDER = "user_gender";
    public static final String KEY_DEFAULT_STYLE = "user_default_style";
    public static final String KEY_DEFAULT_SEASON = "user_default_season";
    public static final String KEY_NICKNAME = "user_nickname";
    public static final String KEY_DARK_MODE = "dark_mode_enabled";  // true=深色


    public static void putString(Context ctx, String key, String value) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public static String getString(Context ctx, String key, String def) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, def);
    }

    public static void putInt(Context ctx, String key, int value) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).apply();
    }

    public static int getInt(Context ctx, String key, int def) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getInt(key, def);
    }

    public static UserSettings loadUserSettings(Context ctx) {
        int gender = getInt(ctx, KEY_GENDER, 0);
        String style = getString(ctx, KEY_DEFAULT_STYLE, "不过滤");
        String season = getString(ctx, KEY_DEFAULT_SEASON, "不过滤");
        return new UserSettings(gender, style, season);
    }

    public static void saveUserSettings(Context ctx, UserSettings s) {
        putInt(ctx, KEY_GENDER, s.getGender());
        putString(ctx, KEY_DEFAULT_STYLE, s.getDefaultStyle());
        putString(ctx, KEY_DEFAULT_SEASON, s.getDefaultSeason());
    }

    public static void putBoolean(Context ctx, String key, boolean value) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context ctx, String key, boolean def) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, def);
    }
}
