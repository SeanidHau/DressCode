package com.dresscode.app.utils;

import java.util.HashMap;
import java.util.Map;

public class WeatherTextMapper {

    private static final Map<String, String> map = new HashMap<>();

    static {
        // ------ 晴 / 云 ------
        map.put("clear sky", "晴");
        map.put("few clouds", "少云");
        map.put("scattered clouds", "分散多云");
        map.put("broken clouds", "多云");
        map.put("overcast clouds", "阴");

        // ------ 雨 ------
        map.put("light rain", "小雨");
        map.put("moderate rain", "中雨");
        map.put("heavy intensity rain", "大雨");
        map.put("very heavy rain", "暴雨");
        map.put("extreme rain", "特大暴雨");

        map.put("freezing rain", "冻雨");

        // ------ 毛毛雨 ------
        map.put("drizzle", "毛毛雨");
        map.put("light intensity drizzle", "小毛毛雨");
        map.put("heavy intensity drizzle", "大毛毛雨");

        // ------ 雷暴 ------
        map.put("thunderstorm", "雷暴");
        map.put("light thunderstorm", "弱雷暴");
        map.put("heavy thunderstorm", "强雷暴");
        map.put("ragged thunderstorm", "不规则雷暴");

        // ------ 雪 ------
        map.put("light snow", "小雪");
        map.put("snow", "中雪");
        map.put("heavy snow", "大雪");

        map.put("sleet", "雨夹雪");
        map.put("light shower sleet", "小雨夹雪");
        map.put("shower sleet", "阵雨夹雪");

        map.put("light shower snow", "小阵雪");
        map.put("shower snow", "阵雪");
        map.put("heavy shower snow", "强阵雪");

        // ------ 雾 / 空气 ------
        map.put("mist", "薄雾");
        map.put("smoke", "烟");
        map.put("haze", "霾");
        map.put("sand/dust whirls", "沙尘旋风");
        map.put("fog", "雾");
        map.put("sand", "沙尘");
        map.put("dust", "扬尘");
        map.put("volcanic ash", "火山灰");
        map.put("squalls", "飑");
        map.put("tornado", "龙卷风");
    }

    /** 映射英文 → 中文（没有匹配则返回英文或“未知”） */
    public static String toChinese(String desc) {
        if (desc == null) return "未知天气";

        desc = desc.toLowerCase().trim();

        if (map.containsKey(desc)) {
            return map.get(desc);
        }

        return desc; // fallback：直接显示英文，不会报错
    }
}
