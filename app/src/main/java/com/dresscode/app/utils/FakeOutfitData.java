package com.dresscode.app.utils;

import com.dresscode.app.data.local.AppDatabase;
import com.dresscode.app.data.local.entity.OutfitEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 首次启动时往本地 Room 里塞一些穿搭假数据，
 * 没有后台也能看到瀑布流效果。
 */
public class FakeOutfitData {

    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static void seedIfEmpty(AppDatabase db) {
        executor.execute(() -> {
            int count = db.outfitDao().getCount();
            if (count > 0) {
                // 已经有数据了，就不再重复插入
                return;
            }

            List<OutfitEntity> list = new ArrayList<>();

            // 这里随便写了一些示例，图片是网络图，Glide 直接加载
            // id 一定不要重复
            OutfitEntity o1 = new OutfitEntity();
            o1.id = 1;
            o1.imageUrl = "https://images.pexels.com/photos/3755706/pexels-photo-3755706.jpeg";
            o1.gender = 2; // 女
            o1.style = "通勤";
            o1.season = "春";
            o1.scene = "上班";
            o1.weather = "适中";
            o1.keyword = "风衣 半裙 通勤 春季";
            list.add(o1);

            OutfitEntity o2 = new OutfitEntity();
            o2.id = 2;
            o2.imageUrl = "https://images.pexels.com/photos/7671166/pexels-photo-7671166.jpeg";
            o2.gender = 1; // 男
            o2.style = "休闲";
            o2.season = "秋";
            o2.scene = "校园";
            o2.weather = "适中";
            o2.keyword = "连帽衫 牛仔裤 运动鞋";
            list.add(o2);

            OutfitEntity o3 = new OutfitEntity();
            o3.id = 3;
            o3.imageUrl = "https://images.pexels.com/photos/7691088/pexels-photo-7691088.jpeg";
            o3.gender = 2;
            o3.style = "约会";
            o3.season = "夏";
            o3.scene = "约会";
            o3.weather = "炎热";
            o3.keyword = "蓬蓬裙 连衣裙 小高跟";
            list.add(o3);

            OutfitEntity o4 = new OutfitEntity();
            o4.id = 4;
            o4.imageUrl = "https://images.pexels.com/photos/913134/pexels-photo-913134.jpeg";
            o4.gender = 1;
            o4.style = "街头";
            o4.season = "夏";
            o4.scene = "逛街";
            o4.weather = "炎热";
            o4.keyword = "T恤 破洞牛仔裤 街头";
            list.add(o4);

            OutfitEntity o5 = new OutfitEntity();
            o5.id = 5;
            o5.imageUrl = "https://images.pexels.com/photos/7076000/pexels-photo-7076000.jpeg";
            o5.gender = 2;
            o5.style = "通勤";
            o5.season = "冬";
            o5.scene = "办公室";
            o5.weather = "寒冷";
            o5.keyword = "大衣 高领毛衣 长靴";
            list.add(o5);

            OutfitEntity o6 = new OutfitEntity();
            o6.id = 6;
            o6.imageUrl = "https://images.pexels.com/photos/6311577/pexels-photo-6311577.jpeg";
            o6.gender = 1;
            o6.style = "正式";
            o6.season = "冬";
            o6.scene = "面试";
            o6.weather = "寒冷";
            o6.keyword = "西装 大衣 正装裤";
            list.add(o6);

            OutfitEntity o7 = new OutfitEntity();
            o7.id = 7;
            o7.imageUrl = "https://images.pexels.com/photos/6311606/pexels-photo-6311606.jpeg";
            o7.gender = 2;
            o7.style = "运动";
            o7.season = "夏";
            o7.scene = "健身房";
            o7.weather = "炎热";
            o7.keyword = "运动内衣 紧身裤 跑鞋";
            list.add(o7);

            OutfitEntity o8 = new OutfitEntity();
            o8.id = 8;
            o8.imageUrl = "https://images.pexels.com/photos/7671179/pexels-photo-7671179.jpeg";
            o8.gender = 1;
            o8.style = "休闲";
            o8.season = "春";
            o8.scene = "旅行";
            o8.weather = "适中";
            o8.keyword = "风衣 卫衣 旅行 休闲";
            list.add(o8);

            db.outfitDao().insertAll(list);
        });
    }
}
