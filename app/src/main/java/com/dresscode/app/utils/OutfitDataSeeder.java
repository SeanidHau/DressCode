package com.dresscode.app.utils;

import android.content.Context;

import com.dresscode.app.data.local.AppDatabase;
import com.dresscode.app.data.local.entity.OutfitEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OutfitDataSeeder {

    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static void seedIfEmpty(Context context, AppDatabase db) {
        executor.execute(() -> {
            try {
                db.outfitDao().clearAllOutfits();

                String json = readAsset(context, "outfits.json");
                Type type = new TypeToken<List<OutfitEntity>>() {}.getType();
                List<OutfitEntity> list = new Gson().fromJson(json, type);

                // 兜底，避免 Room / equals 空指针
                for (OutfitEntity o : list) {
                    if (o.imageUrl == null) o.imageUrl = "";
                    if (o.style == null) o.style = "";
                    if (o.season == null) o.season = "";
                    if (o.scene == null) o.scene = "";
                    if (o.weather == null) o.weather = "";
                    if (o.keyword == null) o.keyword = "";
                }

                db.outfitDao().insertAll(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String readAsset(Context context, String name) throws Exception {
        InputStream is = context.getAssets().open(name);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append('\n');
        br.close();
        return sb.toString();
    }
}
