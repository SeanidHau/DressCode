package com.dresscode.app.model;

public class FilterOption {

    // 0 = 不限制 / 全部；1 = 男；2 = 女
    public int gender = 0;

    public String style;   // 休闲 / 通勤 / 运动 ...
    public String season;  // 春 / 夏 / 秋 / 冬
    public String scene;   // 校园 / 约会 / 职场 ...
    public String weather; // 热 / 冷 / 雨天 ...

    public FilterOption() {
    }

    public FilterOption(int gender, String style, String season, String scene, String weather) {
        this.gender = gender;
        this.style = style;
        this.season = season;
        this.scene = scene;
        this.weather = weather;
    }

    public FilterOption copy() {
        FilterOption f = new FilterOption();
        f.gender = this.gender;
        f.style = this.style;
        f.season = this.season;
        f.scene = this.scene;
        f.weather = this.weather;
        return f;
    }
}
