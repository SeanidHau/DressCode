package com.dresscode.app.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dresscode.app.model.UserSettings;
import com.dresscode.app.utils.PreferenceUtils;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<UserSettings> userSettingsLiveData = new MutableLiveData<>();

    public LiveData<UserSettings> getUserSettingsLiveData() {
        return userSettingsLiveData;
    }

    // Fragment 在 onViewCreated 中调用
    public void loadSettings(Context context) {
        UserSettings settings = PreferenceUtils.loadUserSettings(context);
        userSettingsLiveData.setValue(settings);
    }

    public void updateGender(int gender) {
        UserSettings current = userSettingsLiveData.getValue();
        if (current == null) {
            current = new UserSettings();
        }
        current.setGender(gender);
        userSettingsLiveData.setValue(current);
    }

    public void updateDefaultStyle(String style) {
        UserSettings current = userSettingsLiveData.getValue();
        if (current == null) {
            current = new UserSettings();
        }
        current.setDefaultStyle(style);
        userSettingsLiveData.setValue(current);
    }

    public void updateDefaultSeason(String season) {
        UserSettings current = userSettingsLiveData.getValue();
        if (current == null) {
            current = new UserSettings();
        }
        current.setDefaultSeason(season);
        userSettingsLiveData.setValue(current);
    }

    public void saveSettings(Context context) {
        UserSettings current = userSettingsLiveData.getValue();
        PreferenceUtils.saveUserSettings(context, current);
    }
}
