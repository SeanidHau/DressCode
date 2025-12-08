package com.dresscode.app.model;

public class UserSettings {

    public static final int GENDER_UNKNOWN = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    private int gender;
    private String defaultStyle;
    private String defaultSeason;

    public UserSettings() {
    }

    public UserSettings(int gender, String defaultStyle, String defaultSeason) {
        this.gender = gender;
        this.defaultStyle = defaultStyle;
        this.defaultSeason = defaultSeason;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public String getDefaultSeason() {
        return defaultSeason;
    }

    public void setDefaultSeason(String defaultSeason) {
        this.defaultSeason = defaultSeason;
    }
}
