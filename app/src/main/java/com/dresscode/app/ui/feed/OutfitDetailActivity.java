package com.dresscode.app.ui.feed;

import android.app.Activity;
import androidx.activity.OnBackPressedCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.dresscode.app.R;
import com.dresscode.app.data.local.AppDatabase;
import com.dresscode.app.data.local.dao.FavoriteDao;
import com.dresscode.app.data.local.dao.OutfitDao;
import com.dresscode.app.data.local.dao.SearchHistoryDao;
import com.dresscode.app.data.local.entity.FavoriteEntity;
import com.dresscode.app.data.remote.api.OutfitApi;
import com.dresscode.app.data.repository.OutfitRepository;
import com.dresscode.app.viewmodel.FeedViewModel;
import com.dresscode.app.viewmodel.FeedViewModelFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OutfitDetailActivity extends AppCompatActivity {

    private static final String EXTRA_ID = "extra_outfit_id";

    public static Intent newIntent(Context ctx, int outfitId) {
        Intent i = new Intent(ctx, OutfitDetailActivity.class);
        i.putExtra(EXTRA_ID, outfitId);
        return i;
    }

    private FeedViewModel viewModel;
    private ImageView imageView;
    private ImageButton btnFavorite;
    private ImageButton btnBack;
    private TextView tvTitle, tvTags, tvDesc;

    private FavoriteDao favoriteDao;
    private int outfitId;
    private boolean isFavorite = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_detail);

        imageView = findViewById(R.id.outfitImage);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvTags = findViewById(R.id.tvTags);
        tvDesc = findViewById(R.id.tvDesc);

        outfitId = getIntent().getIntExtra(EXTRA_ID, -1);

        initViewModelAndDao();
        bindOutfitData();
        initFavoriteState();
        initClicks();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                );
            }
        });
    }

    private void initViewModelAndDao() {
        AppDatabase db = AppDatabase.getInstance(this);
        OutfitDao outfitDao = db.outfitDao();
        favoriteDao = db.favoriteDao();
        SearchHistoryDao searchHistoryDao = db.searchHistoryDao();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://example.com/api/") // TODO: 如果有后端再改
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        OutfitApi outfitApi = retrofit.create(OutfitApi.class);

        OutfitRepository repository =
                new OutfitRepository(outfitDao, favoriteDao, searchHistoryDao, outfitApi);

        FeedViewModelFactory factory = new FeedViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(FeedViewModel.class);
    }

    private void bindOutfitData() {
        viewModel.getOutfitDetail(outfitId).observe(this, outfit -> {
            if (outfit != null) {
                Glide.with(this)
                        .load(outfit.imageUrl)
                        .into(imageView);

                // 简单把 style/season/scene 拼一下
                String title = "推荐穿搭 #" + outfit.id;
                String tags = "";
                if (outfit.style != null) tags += outfit.style + " · ";
                if (outfit.season != null) tags += outfit.season + " · ";
                if (outfit.scene != null) tags += outfit.scene;
                if (tags.endsWith(" · ")) {
                    tags = tags.substring(0, tags.length() - 3);
                }

                tvTitle.setText(title);
                tvTags.setText(tags.isEmpty() ? "日常穿搭" : tags);
                tvDesc.setText(outfit.keyword != null ? outfit.keyword : "适合日常出行的舒适搭配。");
            }
        });
    }

    /** 初始化收藏状态（已收藏就亮星） */
    private void initFavoriteState() {
        executor.execute(() -> {
            FavoriteEntity entity = favoriteDao.getByOutfitIdSync(outfitId);
            isFavorite = (entity != null);
            runOnUiThread(this::updateFavoriteIcon);
        });
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(
                isFavorite
                        ? R.drawable.ic_star_filled
                        : R.drawable.ic_star_border
        );
    }

    private void initClicks() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnFavorite.setOnClickListener(v -> {
            // 1. 先本地切换 UI 状态
            isFavorite = !isFavorite;
            updateFavoriteIcon();
            // 2. 调用 ViewModel / Repository 真正改数据库
            viewModel.toggleFavorite(outfitId);
        });
    }
}
