package com.dresscode.app.ui.feed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.dresscode.app.R;
import com.dresscode.app.data.local.dao.FavoriteDao;
import com.dresscode.app.data.local.dao.OutfitDao;
import com.dresscode.app.data.local.dao.SearchHistoryDao;
import com.dresscode.app.data.local.AppDatabase;
import com.dresscode.app.data.remote.api.OutfitApi;
import com.dresscode.app.data.repository.OutfitRepository;
import com.dresscode.app.viewmodel.FeedViewModel;
import com.dresscode.app.viewmodel.FeedViewModelFactory;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_detail);

        imageView = findViewById(R.id.outfitImage);

        int outfitId = getIntent().getIntExtra(EXTRA_ID, -1);

        initViewModel();

        viewModel.getOutfitDetail(outfitId).observe(this, outfit -> {
            if (outfit != null) {
                Glide.with(this)
                        .load(outfit.imageUrl)
                        .into(imageView);
                // 你可以在这里更新风格、季节等文本
            }
        });
    }

    private void initViewModel() {
        AppDatabase db = AppDatabase.getInstance(this);
        OutfitDao outfitDao = db.outfitDao();
        FavoriteDao favoriteDao = db.favoriteDao();
        SearchHistoryDao searchHistoryDao = db.searchHistoryDao();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://example.com/api/") // TODO 替换
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        OutfitApi outfitApi = retrofit.create(OutfitApi.class);

        OutfitRepository repository =
                new OutfitRepository(outfitDao, favoriteDao, searchHistoryDao, outfitApi);

        FeedViewModelFactory factory = new FeedViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(FeedViewModel.class);
    }
}
