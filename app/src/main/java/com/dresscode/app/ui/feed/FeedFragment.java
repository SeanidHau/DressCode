package com.dresscode.app.ui.feed;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.dresscode.app.R;
import com.dresscode.app.data.local.dao.FavoriteDao;
import com.dresscode.app.data.local.dao.OutfitDao;
import com.dresscode.app.data.local.dao.SearchHistoryDao;
import com.dresscode.app.data.local.AppDatabase;
import com.dresscode.app.data.remote.api.OutfitApi;
import com.dresscode.app.data.repository.OutfitRepository;
import com.dresscode.app.model.FilterOption;
import com.dresscode.app.viewmodel.FeedViewModel;
import com.dresscode.app.viewmodel.FeedViewModelFactory;
import com.dresscode.app.utils.FakeOutfitData;
import com.dresscode.app.data.local.entity.FavoriteEntity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FeedFragment extends Fragment {

    private FeedViewModel viewModel;
    private FeedAdapter adapter;

    private EditText searchBox;

    private FilterOption currentFilter = new FilterOption();

    public FeedFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feed, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
        searchBox = v.findViewById(R.id.searchBox);

        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        adapter = new FeedAdapter(outfitId -> viewModel.toggleFavorite(outfitId));
        recyclerView.setAdapter(adapter);

        v.findViewById(R.id.btnFilter).setOnClickListener(view -> {
            FeedFilterDialog.show(
                    requireContext(),
                    currentFilter,
                    option -> {
                        currentFilter = option;
                        viewModel.updateFilter(option); // 触发 LiveData 刷新
                    }
            );
        });

        initViewModel();

        // 观察数据
        viewModel.outfitList.observe(getViewLifecycleOwner(), outfits -> {
            adapter.submitList(outfits);
        });

        observeData();

        setupSearch();

        return v;
    }

    private void initViewModel() {
        // 1. 拿到 Room 数据库
        AppDatabase db = AppDatabase.getInstance(requireContext());
        OutfitDao outfitDao = db.outfitDao();
        FavoriteDao favoriteDao = db.favoriteDao();
        SearchHistoryDao searchHistoryDao = db.searchHistoryDao();

        // 2. ⚠️ 先塞假数据（只在空表时塞一次）
        FakeOutfitData.seedIfEmpty(db);

        // 3. Retrofit（如果暂时不用后端，这段就先留着，不影响）
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://example.com/api/") // TODO: 以后有后端再改
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        OutfitApi outfitApi = retrofit.create(OutfitApi.class);

        OutfitRepository repository =
                new OutfitRepository(outfitDao, favoriteDao, searchHistoryDao, outfitApi);

        FeedViewModelFactory factory = new FeedViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(FeedViewModel.class);

        viewModel.favoriteList.observe(getViewLifecycleOwner(), favorites -> {
            if (favorites == null) return;
            List<Integer> ids = new ArrayList<>();
            for (FavoriteEntity f : favorites) {
                ids.add(f.outfitId);
            }
            adapter.setFavoriteIds(ids);
        });
    }

    private void setupSearch() {
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            boolean isActionSearch = actionId == EditorInfo.IME_ACTION_SEARCH;
            boolean isEnterKey = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;

            if (isActionSearch || isEnterKey) {
                String keyword = searchBox.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    viewModel.search(keyword).observe(getViewLifecycleOwner(), outfits -> {
                        adapter.submitList(outfits);
                    });
                }
                return true;
            }
            return false;
        });
    }

    private void observeData() {
        // 列表数据
        viewModel.outfitList.observe(getViewLifecycleOwner(), outfits -> {
            adapter.submitList(outfits);
        });

        // 收藏状态（如果你之前已经写了 favoriteList 观察，这里保留）
        viewModel.favoriteList.observe(getViewLifecycleOwner(), favorites -> {
            if (favorites == null) return;
            List<Integer> ids = new ArrayList<>();
            for (FavoriteEntity f : favorites) {
                ids.add(f.outfitId);
            }
            adapter.setFavoriteIds(ids);
        });
    }
}
