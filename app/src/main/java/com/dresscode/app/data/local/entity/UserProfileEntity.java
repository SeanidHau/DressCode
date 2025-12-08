package com.dresscode.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfileEntity {

    @PrimaryKey
    public int id = 1;   // app 只管一个本地用户，所以写死 1 就够用

    public String nickname;

    // 0=未设置 / 全部，1=男，2=女
    public int gender;

    public UserProfileEntity() {}

    @Ignore
    public UserProfileEntity(String nickname, int gender) {
        this.nickname = nickname;
        this.gender = gender;
    }
}
