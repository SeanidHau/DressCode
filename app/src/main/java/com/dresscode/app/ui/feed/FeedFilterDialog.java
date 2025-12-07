package com.dresscode.app.ui.feed;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.dresscode.app.R;
import com.dresscode.app.model.FilterOption;

import java.util.Arrays;

public class FeedFilterDialog {

    public interface OnFilterSelectedListener {
        void onFilterSelected(FilterOption option);
    }

    public static void show(Context context,
                            FilterOption currentOption,
                            OnFilterSelectedListener listener) {

        FilterOption working = currentOption != null ? currentOption.copy() : new FilterOption();

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_feed_filter, null);

        RadioGroup rgGender = view.findViewById(R.id.rgGender);
        RadioButton rbAll = view.findViewById(R.id.rbGenderAll);
        RadioButton rbMale = view.findViewById(R.id.rbGenderMale);
        RadioButton rbFemale = view.findViewById(R.id.rbGenderFemale);

        Spinner spStyle = view.findViewById(R.id.spStyle);
        Spinner spSeason = view.findViewById(R.id.spSeason);
        Spinner spScene = view.findViewById(R.id.spScene);
        Spinner spWeather = view.findViewById(R.id.spWeather);

        // 几个下拉选项（第一项都是“全部”）
        String[] styles = new String[]{"全部", "休闲", "通勤", "运动", "街头", "约会", "正式"};
        String[] seasons = new String[]{"全部", "春", "夏", "秋", "冬"};
        String[] scenes = new String[]{"全部", "校园", "上班", "约会", "聚会", "旅行", "面试"};
        String[] weathers = new String[]{"全部", "炎热", "适中", "寒冷", "下雨"};

        setupSpinner(context, spStyle, styles, working.style);
        setupSpinner(context, spSeason, seasons, working.season);
        setupSpinner(context, spScene, scenes, working.scene);
        setupSpinner(context, spWeather, weathers, working.weather);

        // 初始性别选中
        switch (working.gender) {
            case 1:
                rbMale.setChecked(true);
                break;
            case 2:
                rbFemale.setChecked(true);
                break;
            default:
                rbAll.setChecked(true);
                break;
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("筛选穿搭")
                .setView(view)
                .setPositiveButton("确定", (d, which) -> {
                    // 读取性别
                    int checkedId = rgGender.getCheckedRadioButtonId();
                    if (checkedId == R.id.rbGenderMale) {
                        working.gender = 1;
                    } else if (checkedId == R.id.rbGenderFemale) {
                        working.gender = 2;
                    } else {
                        working.gender = 0;
                    }

                    // 读取下拉（“全部” → null）
                    working.style = valueOrNull(styles[spStyle.getSelectedItemPosition()]);
                    working.season = valueOrNull(seasons[spSeason.getSelectedItemPosition()]);
                    working.scene = valueOrNull(scenes[spScene.getSelectedItemPosition()]);
                    working.weather = valueOrNull(weathers[spWeather.getSelectedItemPosition()]);

                    if (listener != null) {
                        listener.onFilterSelected(working);
                    }
                })
                .setNegativeButton("重置", (d, which) -> {
                    FilterOption reset = new FilterOption();
                    if (listener != null) {
                        listener.onFilterSelected(reset);
                    }
                })
                .create();

        dialog.show();
    }

    private static void setupSpinner(Context context, Spinner spinner,
                                     String[] options, String currentValue) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int index = 0; // 默认“全部”
        if (currentValue != null) {
            int found = Arrays.asList(options).indexOf(currentValue);
            if (found >= 0) {
                index = found;
            }
        }
        spinner.setSelection(index);
    }

    private static String valueOrNull(String s) {
        if ("全部".equals(s)) return null;
        return s;
    }
}
