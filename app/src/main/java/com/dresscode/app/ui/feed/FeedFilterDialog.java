package com.dresscode.app.ui.feed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.dresscode.app.R;
import com.dresscode.app.model.FilterOption;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class FeedFilterDialog {

    public interface OnFilterSelectedListener {
        void onFilterSelected(FilterOption option);
    }

    public static void show(Context context,
                            FilterOption currentOption,
                            OnFilterSelectedListener listener) {

        FilterOption working = currentOption != null ? currentOption.copy() : new FilterOption();

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_feed_filter, null);

        // 性别
        MaterialButtonToggleGroup groupGender = view.findViewById(R.id.groupGender);
        MaterialButton btnGenderAll = view.findViewById(R.id.btnGenderAll);
        MaterialButton btnGenderMale = view.findViewById(R.id.btnGenderMale);
        MaterialButton btnGenderFemale = view.findViewById(R.id.btnGenderFemale);

        // 筛选维度
        ChipGroup groupStyle = view.findViewById(R.id.groupStyle);
        ChipGroup groupSeason = view.findViewById(R.id.groupSeason);
        ChipGroup groupScene = view.findViewById(R.id.groupScene);
        ChipGroup groupWeather = view.findViewById(R.id.groupWeather);

        MaterialButton btnReset = view.findViewById(R.id.btnReset);
        MaterialButton btnApply = view.findViewById(R.id.btnApply);

        // 初始化性别
        switch (working.gender) {
            case 1:
                groupGender.check(btnGenderMale.getId());
                break;
            case 2:
                groupGender.check(btnGenderFemale.getId());
                break;
            default:
                groupGender.check(btnGenderAll.getId());
                break;
        }

        // 初始化各个 ChipGroup（根据当前值选中）
        preselectChip(groupStyle, working.style);
        preselectChip(groupSeason, working.season);
        preselectChip(groupScene, working.scene);
        preselectChip(groupWeather, working.weather);

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);

        // 应用筛选
        btnApply.setOnClickListener(v -> {
            // 性别
            int checkedGenderId = groupGender.getCheckedButtonId();
            if (checkedGenderId == btnGenderMale.getId()) {
                working.gender = 1;
            } else if (checkedGenderId == btnGenderFemale.getId()) {
                working.gender = 2;
            } else {
                working.gender = 0;
            }

            // 其他维度（"全部" → null）
            working.style = getChipValue(groupStyle);
            working.season = getChipValue(groupSeason);
            working.scene = getChipValue(groupScene);
            working.weather = getChipValue(groupWeather);

            if (listener != null) {
                listener.onFilterSelected(working);
            }
            dialog.dismiss();
        });

        // 重置
        btnReset.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFilterSelected(new FilterOption());
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private static void preselectChip(ChipGroup group, String value) {
        if (value == null) {
            // 默认选中“全部”（第一个 chip）
            if (group.getChildCount() > 0 && group.getChildAt(0) instanceof Chip) {
                ((Chip) group.getChildAt(0)).setChecked(true);
            }
            return;
        }

        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (value.equals(chip.getText().toString())) {
                    chip.setChecked(true);
                    return;
                }
            }
        }
    }

    private static String getChipValue(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id == View.NO_ID) return null;
        Chip chip = group.findViewById(id);
        if (chip == null) return null;
        String text = chip.getText().toString();
        if ("全部".equals(text)) return null;
        return text;
    }
}
