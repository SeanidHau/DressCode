package com.dresscode.app.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.dresscode.app.R;
import com.dresscode.app.data.remote.ApiClient;
import com.dresscode.app.ui.dressing.DressingFragment;
import com.dresscode.app.ui.feed.FeedFragment;
import com.dresscode.app.ui.settings.SettingsFragment;
import com.dresscode.app.ui.weather.WeatherFragment;
import com.dresscode.app.utils.PreferenceUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.dresscode.app.data.local.AppDatabase;
import com.dresscode.app.utils.OutfitDataSeeder;

public class MainActivity extends AppCompatActivity
        implements WeatherFragment.Navigator {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean darkMode = PreferenceUtils.getBoolean(
                this,
                PreferenceUtils.KEY_DARK_MODE,
                false
        );

        AppCompatDelegate.setDefaultNightMode(
                darkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDatabase db = AppDatabase.getInstance(this);
        OutfitDataSeeder.seedIfEmpty(this, db);

        bottomNav = findViewById(R.id.bottomNav);

        // 首次进入默认显示 FeedFragment
        if (savedInstanceState == null) {
            switchFragment(new FeedFragment());
            bottomNav.setSelectedItemId(R.id.nav_feed);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_feed) {
                fragment = new FeedFragment();
            } else if (itemId == R.id.nav_weather) {
                fragment = new WeatherFragment();
            } else if (itemId == R.id.nav_dressing) {
                fragment = new DressingFragment();
            } else if (itemId == R.id.nav_settings) {
                fragment = new SettingsFragment();
            }

            if (fragment != null) {
                switchFragment(fragment);
                return true;
            }
            return false;
        });

        ApiClient.resetForDebug();
    }

    private void switchFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void navigateToFeed() {
        if (bottomNav != null) {
            // 直接选中 Feed tab，会自动触发 setOnItemSelectedListener
            bottomNav.setSelectedItemId(R.id.nav_feed);
        }
    }

}
