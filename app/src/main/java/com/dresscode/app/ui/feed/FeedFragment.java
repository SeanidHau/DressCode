package com.dresscode.app.ui.feed;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.dresscode.app.R;
import com.dresscode.app.data.local.dao.FavoriteDao;
import com.dresscode.app.data.local.dao.OutfitDao;
import com.dresscode.app.data.local.dao.SearchHistoryDao;
import com.dresscode.app.data.local.AppDatabase;
import com.dresscode.app.data.local.entity.SearchHistoryEntity;
import com.dresscode.app.data.remote.api.OutfitApi;
import com.dresscode.app.data.repository.OutfitRepository;
import com.dresscode.app.model.FilterOption;
import com.dresscode.app.viewmodel.FeedViewModel;
import com.dresscode.app.viewmodel.FeedViewModelFactory;
import com.dresscode.app.utils.FakeOutfitData;
import com.dresscode.app.data.local.entity.FavoriteEntity;
import com.dresscode.app.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FeedFragment extends Fragment {
    private FeedViewModel viewModel;
    private FeedAdapter adapter;
    private SearchHistoryAdapter historyAdapter;
    private EditText searchBox;
    private RecyclerView rvHistory;
    private FilterOption currentFilter = new FilterOption();
    private List<SearchHistoryEntity> historyCache = new ArrayList<>();
    private ImageButton btnBackSearch;
    private Button btnClearSearch;
    private boolean isInSearchMode = false;

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
        rvHistory = v.findViewById(R.id.rvSearchHistory);
        btnBackSearch = v.findViewById(R.id.btnBackSearch);
        btnClearSearch = v.findViewById(R.id.btnClearSearch);
        Button btnFilter = v.findViewById(R.id.btnFilter);

        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        adapter = new FeedAdapter(outfitId -> viewModel.toggleFavorite(outfitId));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                super.onScrollStateChanged(rv, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    hideHistory();
                    searchBox.clearFocus();
                    hideKeyboard();
                }
            }
        });

        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyAdapter = new SearchHistoryAdapter(keyword -> {
            enterSearchMode();
            searchBox.setText(keyword);
            searchBox.setSelection(keyword.length());
            performSearch(keyword);
            hideHistory();
        });

        rvHistory.setAdapter(historyAdapter);

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

        btnBackSearch.setOnClickListener(v1 -> exitSearchMode());

        btnClearSearch.setOnClickListener(view12 -> {
            searchBox.setText("");
            if (historyAdapter.getItemCount() > 0 && searchBox.hasFocus()) {
                showHistory();
            } else {
                hideHistory();
            }
        });

        initViewModel();

        applyDefaultFilterFromSettings();

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
            String keyword = searchBox.getText().toString().trim();
            if (!keyword.isEmpty()) {
                enterSearchMode();
                performSearch(keyword);
                hideHistory();
            }
            return true;
        });


        searchBox.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                enterSearchMode();
                if (searchBox.getText().length() == 0
                        && historyAdapter.getItemCount() > 0) {
                    showHistory();
                }
            } else {
                hideHistory();
            }
        });

        // 文本变化：有内容就隐藏历史；清空内容就显示历史
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0 && searchBox.hasFocus()
                        && historyAdapter.getItemCount() > 0) {
                    showHistory();
                    btnClearSearch.setVisibility(View.GONE);
                } else if (s.length() > 0) {
                    hideHistory();
                    btnClearSearch.setVisibility(View.VISIBLE);
                }
            }
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

        viewModel.searchHistoryList.observe(getViewLifecycleOwner(), history -> {
            historyAdapter.submitList(history);
            // 如果当前输入框有焦点 & 没文字，就显示
            if (searchBox.hasFocus() && (searchBox.getText().length() == 0)
                    && history != null && !history.isEmpty()) {
                showHistory();
            } else {
                hideHistory();
            }
        });

    }

    private void performSearch(String keyword) {
        viewModel.search(keyword).observe(getViewLifecycleOwner(), result -> {
            adapter.submitList(result);
        });

        btnClearSearch.setVisibility(View.VISIBLE);

        hideHistory();
        hideKeyboard();
    }

    private void showHistory() {
        rvHistory.setVisibility(View.VISIBLE);
    }

    private void hideHistory() {
        rvHistory.setVisibility(View.GONE);
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view == null) {
            view = getView();
        }
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void enterSearchMode() {
        isInSearchMode = true;
        btnBackSearch.setVisibility(View.VISIBLE);   // 显示返回
    }

    private void exitSearchMode() {
        isInSearchMode = false;

        searchBox.setText("");        // 清空搜索
        searchBox.clearFocus();       // 取消焦点
        btnBackSearch.setVisibility(View.GONE);  // 隐藏返回
        btnClearSearch.setVisibility(View.GONE); // 隐藏清除

        hideHistory();                // 隐藏历史列表
        hideKeyboard();               // 收键盘

        // 回到主列表（保留当前筛选条件）
        viewModel.updateFilter(currentFilter);
    }

    /**
     * 从设置页读取用户保存的性别 / 风格 / 季节，并作为 Feed 的默认筛选选项
     */
    private void applyDefaultFilterFromSettings() {
        Context ctx = requireContext();

        int gender = PreferenceUtils.getInt(ctx, PreferenceUtils.KEY_GENDER, 0);
        String style = PreferenceUtils.getString(ctx, PreferenceUtils.KEY_DEFAULT_STYLE, "不过滤");
        String season = PreferenceUtils.getString(ctx, PreferenceUtils.KEY_DEFAULT_SEASON, "不过滤");

        FilterOption option = new FilterOption();

        option.gender = gender;

        if ("不过滤".equals(style)) {
            option.style = null;
        } else {
            option.style = style;
        }

        if ("不过滤".equals(season)) {
            option.season = null;
        } else {
            option.season = season;
        }

        option.scene = null;
        option.weather = null;

        currentFilter = option;

        viewModel.updateFilter(option);
    }

}
