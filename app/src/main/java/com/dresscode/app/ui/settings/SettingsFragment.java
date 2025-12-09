package com.dresscode.app.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dresscode.app.R;
import com.dresscode.app.data.local.AppDatabase;
import com.dresscode.app.data.local.dao.UserProfileDao;
import com.dresscode.app.data.local.entity.UserProfileEntity;
import com.dresscode.app.model.UserSettings;
import com.dresscode.app.ui.login.LoginActivity;
import com.dresscode.app.utils.PreferenceUtils;
import com.dresscode.app.viewmodel.SettingsViewModel;

import java.io.File;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    private EditText etNickname;

    private RadioGroup rgGender;
    private RadioButton rbGenderUnknown;
    private RadioButton rbGenderMale;
    private RadioButton rbGenderFemale;
    private Spinner spinnerDefaultStyle;
    private Spinner spinnerDefaultSeason;
    private Button btnSaveSettings;

    private SwitchCompat switchDarkMode;
    private Button btnClearCache;
    private Button btnLogout;

    private ArrayAdapter<CharSequence> styleAdapter;
    private ArrayAdapter<CharSequence> seasonAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        // 昵称
        etNickname = v.findViewById(R.id.etNickname);

        // 性别
        rgGender = v.findViewById(R.id.rgGender);
        rbGenderUnknown = v.findViewById(R.id.rbGenderUnknown);
        rbGenderMale = v.findViewById(R.id.rbGenderMale);
        rbGenderFemale = v.findViewById(R.id.rbGenderFemale);

        // 默认筛选
        spinnerDefaultStyle = v.findViewById(R.id.spinnerDefaultStyle);
        spinnerDefaultSeason = v.findViewById(R.id.spinnerDefaultSeason);

        // 外观与缓存
        switchDarkMode = v.findViewById(R.id.switchDarkMode);
        btnClearCache = v.findViewById(R.id.btnClearCache);

        // 保存按钮
        btnSaveSettings = v.findViewById(R.id.btnSaveSettings);

        // 退出登录按钮
        btnLogout = v.findViewById(R.id.btnLogout);

        // Spinner 适配器
        styleAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.style_options,
                android.R.layout.simple_spinner_item
        );
        styleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDefaultStyle.setAdapter(styleAdapter);

        seasonAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.season_options,
                android.R.layout.simple_spinner_item
        );
        seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDefaultSeason.setAdapter(seasonAdapter);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // 订阅数据变化（性别 / 默认风格 / 默认季节）
        viewModel.getUserSettingsLiveData().observe(getViewLifecycleOwner(), this::applySettingsToUi);

        // 从 SharedPreferences 读取昵称 / 深色模式
        loadProfileFromPreferences();

        // 读取本地已保存设置（性别、默认筛选）
        viewModel.loadSettings(requireContext());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 写入 SharedPreferences
            PreferenceUtils.putBoolean(
                    requireContext(),
                    PreferenceUtils.KEY_DARK_MODE,
                    isChecked
            );

            // 设置 NightMode
            AppCompatDelegate.setDefaultNightMode(
                    isChecked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );

            // 重建 Activity 让主题立即刷新
            requireActivity().recreate();
        });

        // 性别选择监听 -> 更新到 ViewModel
        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbGenderMale) {
                viewModel.updateGender(UserSettings.GENDER_MALE);
            } else if (checkedId == R.id.rbGenderFemale) {
                viewModel.updateGender(UserSettings.GENDER_FEMALE);
            } else {
                viewModel.updateGender(UserSettings.GENDER_UNKNOWN);
            }
        });

        // 风格选择监听
        spinnerDefaultStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                String style = (String) parent.getItemAtPosition(position);
                viewModel.updateDefaultStyle(style);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // ignore
            }
        });

        // 季节选择监听
        spinnerDefaultSeason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view12, int position, long id) {
                String season = (String) parent.getItemAtPosition(position);
                viewModel.updateDefaultSeason(season);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // ignore
            }
        });

        // 清除缓存
        btnClearCache.setOnClickListener(v1 -> {
            clearAppCache(requireContext());
            Toast.makeText(requireContext(), "缓存已清除", Toast.LENGTH_SHORT).show();
        });

        // 保存按钮：保存 ViewModel 设置 + 昵称 + 深色模式 + 写入 Room
        btnSaveSettings.setOnClickListener(v -> {
            saveAllSettings();
            Toast.makeText(requireContext(), "设置已保存", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            // 1. 调用 ViewModel 退出登录（清除本地登录状态）
            viewModel.logout(requireContext());

            // 2. 跳转回登录页面
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            // 清除返回栈，防止按返回键又回到已登录页面
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // 3. 结束当前 Activity
            requireActivity().finish();
        });
    }

    /**
     * 把 ViewModel 中的设置（性别 / 默认风格 / 默认季节）应用到 UI 上
     */
    private void applySettingsToUi(UserSettings settings) {
        if (settings == null) return;

        // 性别
        switch (settings.getGender()) {
            case UserSettings.GENDER_MALE:
                rbGenderMale.setChecked(true);
                break;
            case UserSettings.GENDER_FEMALE:
                rbGenderFemale.setChecked(true);
                break;
            case UserSettings.GENDER_UNKNOWN:
            default:
                rbGenderUnknown.setChecked(true);
                break;
        }

        // 默认风格 Spinner
        if (settings.getDefaultStyle() != null) {
            int index = styleAdapter.getPosition(settings.getDefaultStyle());
            if (index >= 0) {
                spinnerDefaultStyle.setSelection(index);
            }
        }

        // 默认季节 Spinner
        if (settings.getDefaultSeason() != null) {
            int index = seasonAdapter.getPosition(settings.getDefaultSeason());
            if (index >= 0) {
                spinnerDefaultSeason.setSelection(index);
            }
        }
    }

    /**
     * 从 SharedPreferences 读取昵称 / 深色模式到 UI
     */
    private void loadProfileFromPreferences() {
        Context ctx = requireContext();
        String nickname = PreferenceUtils.getString(ctx, PreferenceUtils.KEY_NICKNAME, "");
        etNickname.setText(nickname);

        boolean darkMode = PreferenceUtils.getBoolean(ctx, PreferenceUtils.KEY_DARK_MODE, false);
        switchDarkMode.setChecked(darkMode);
    }

    /**
     * 保存所有设置：
     * - 性别 / 默认筛选 -> SettingsViewModel + PreferenceUtils（你原来就有）
     * - 昵称 / 深色模式 -> PreferenceUtils
     * - 昵称 / 性别 -> Room 的 UserProfile 表
     */
    private void saveAllSettings() {
        Context ctx = requireContext();

        // 1. 让 ViewModel 把 UserSettings 写入 SharedPreferences
        viewModel.saveSettings(ctx);

        // 2. 额外存：昵称 / 深色模式
        String nickname = etNickname.getText().toString().trim();

        PreferenceUtils.putString(ctx, PreferenceUtils.KEY_NICKNAME, nickname);

        // 3. 写入 Room：UserProfile（昵称 + 性别）
        UserSettings settings = viewModel.getUserSettingsLiveData().getValue();
        final int gender = (settings != null) ? settings.getGender() : UserSettings.GENDER_UNKNOWN;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(ctx);
            UserProfileDao dao = db.userProfileDao();

            UserProfileEntity profile = new UserProfileEntity(nickname, gender);
            profile.id = 1; // 本地只有一个用户，写死 1
            dao.upsert(profile);
        }).start();
    }

    /**
     * 删除内部 / 外部缓存目录
     */
    private void clearAppCache(Context ctx) {
        deleteDir(ctx.getCacheDir());
        if (ctx.getExternalCacheDir() != null) {
            deleteDir(ctx.getExternalCacheDir());
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) return false;
                }
            }
        }
        return dir != null && dir.delete();
    }
}
