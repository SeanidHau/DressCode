package com.dresscode.app.ui.dressing;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dresscode.app.R;
import com.dresscode.app.data.local.entity.OutfitEntity;
import com.dresscode.app.viewmodel.DressingViewModel;

import java.io.IOException;
import java.util.List;

public class DressingFragment extends Fragment {

    private ImageView ivUserPhoto;
    private RecyclerView rvFavorites;
    private Button btnPick, btnCamera, btnDressing;
    private TextView tvLoading;
    private TextView tvFavoritesEmpty;

    private DressingViewModel viewModel;
    private FavoriteOutfitAdapter favoriteAdapter;

    private Uri selectedPhotoUri;
    private OutfitEntity selectedOutfit;

    private ActivityResultLauncher<Intent> pickLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri cameraImageUri;
    private TextView btnManageFavorites;
    private View manageBar;
    private TextView tvManageCount;
    private Button btnUnfavorite, btnDoneManage;

    private static final int REQUEST_CAMERA_PERMISSION = 2001;
    private boolean navigating = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dressing, container, false);
        initViews(v);
        initViewModel();
        initActivityResultLaunchers();
        setupFavoriteList();
        setupListeners();
        observeViewModel();
        return v;
    }

    private void initViews(View v) {
        ivUserPhoto = v.findViewById(R.id.ivUserPhoto);
        rvFavorites = v.findViewById(R.id.rvFavorites);
        btnPick = v.findViewById(R.id.btnPickPhoto);
        btnCamera = v.findViewById(R.id.btnTakePhoto);
        btnDressing = v.findViewById(R.id.btnDressing);
        tvLoading = v.findViewById(R.id.tvLoading);
        tvFavoritesEmpty = v.findViewById(R.id.tvFavoritesEmpty);
        btnManageFavorites = v.findViewById(R.id.btnManageFavorites);
        manageBar = v.findViewById(R.id.manageBar);
        tvManageCount = v.findViewById(R.id.tvManageCount);
        btnUnfavorite = v.findViewById(R.id.btnUnfavorite);
        btnDoneManage = v.findViewById(R.id.btnDoneManage);

    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(DressingViewModel.class);
    }

    private void initActivityResultLaunchers() {
        pickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedPhotoUri = result.getData().getData();
                        Glide.with(requireContext())
                                .load(selectedPhotoUri)
                                .into(ivUserPhoto);
                        clearStatus();
                    }
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (Boolean.TRUE.equals(success) && cameraImageUri != null) {
                        selectedPhotoUri = cameraImageUri;
                        Glide.with(requireContext())
                                .load(selectedPhotoUri)
                                .into(ivUserPhoto);
                        clearStatus();
                    }
                });
    }

    private void setupFavoriteList() {
        rvFavorites.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(
                        requireContext(), RecyclerView.HORIZONTAL, false
                )
        );

        // ① 先初始化 adapter（别漏）
        favoriteAdapter = new FavoriteOutfitAdapter(outfit -> {
            // 普通点击：选中一套穿搭（用于换装）
            selectedOutfit = outfit;
            favoriteAdapter.setSelectedId(outfit.id);
        });

        // ② 再绑定“管理模式”的勾选计数回调
        favoriteAdapter.setOnCheckedChangeListener(count -> {
            tvManageCount.setText("已选 " + count);
            btnUnfavorite.setEnabled(count > 0);
        });

        // ③ 最后 setAdapter
        rvFavorites.setAdapter(favoriteAdapter);
    }


    private void setupListeners() {
        btnPick.setOnClickListener(v -> pickImageFromGallery());
        btnCamera.setOnClickListener(v -> takePhotoWithCamera());

        btnDressing.setOnClickListener(v -> {
            navigating = false; // ✅ 每次生成前重置

            if (selectedPhotoUri == null) {
                showStatus("请先选择或拍摄一张照片");
                return;
            }
            if (selectedOutfit == null) {
                showStatus("请选择一套收藏穿搭");
                return;
            }
            viewModel.startDressing(requireContext(), selectedPhotoUri, selectedOutfit.id);
        });


        btnManageFavorites.setOnClickListener(v -> {
            boolean toManage = !favoriteAdapter.isManageMode();
            favoriteAdapter.setManageMode(toManage);
            manageBar.setVisibility(toManage ? View.VISIBLE : View.GONE);
            btnManageFavorites.setText(toManage ? "取消" : "管理");

            // 进入管理模式时，不再影响“用于换装”的 selectedOutfit
        });

        btnDoneManage.setOnClickListener(v -> {
            favoriteAdapter.setManageMode(false);
            manageBar.setVisibility(View.GONE);
            btnManageFavorites.setText("管理");
        });

        btnUnfavorite.setOnClickListener(v -> {
            List<Integer> ids = favoriteAdapter.getCheckedIds();
            if (ids == null || ids.isEmpty()) return;

            viewModel.unfavoriteOutfits(ids);

            // UI 退出管理态（删除完成后列表会通过 LiveData 刷新）
            favoriteAdapter.clearChecked();
            favoriteAdapter.setManageMode(false);
            manageBar.setVisibility(View.GONE);
            btnManageFavorites.setText("管理");
        });
    }

    private void observeViewModel() {
        viewModel.getFavoriteList().observe(
                getViewLifecycleOwner(),
                outfits -> {
                    favoriteAdapter.submitList(outfits);
                    tvFavoritesEmpty.setVisibility(
                            outfits == null || outfits.isEmpty()
                                    ? View.VISIBLE
                                    : View.GONE
                    );
                }
        );

        viewModel.getLoading().observe(
                getViewLifecycleOwner(),
                loading -> {
                    if (Boolean.TRUE.equals(loading)) {
                        showStatus("正在生成换装效果…");
                        btnDressing.setEnabled(false);
                    } else {
                        clearStatus();
                        btnDressing.setEnabled(true);
                    }
                }
        );

        viewModel.getDressingResult().observe(
                getViewLifecycleOwner(),
                url -> {
                    if (url == null) return;

                    url = url.trim();
                    if (url.isEmpty()) return;

                    // ✅ 防止 LiveData 重放 / 多次回调导致重复 startActivity
                    if (navigating) return;
                    navigating = true;

                    // ✅ 先消费掉，避免页面重建/返回再次触发
                    viewModel.consumeDressingResult();

                    // ✅ Fragment 不在有效状态就不跳
                    if (!isAdded() || getActivity() == null || getActivity().isFinishing()) return;

                    Intent intent = new Intent(requireContext(), DressingResultActivity.class);
                    intent.putExtra(DressingResultActivity.EXTRA_RESULT_URL, url);
                    startActivity(intent);
                }
        );


        viewModel.getErrorMessage().observe(
                getViewLifecycleOwner(),
                msg -> {
                    if (msg != null && !msg.trim().isEmpty()) {
                        showStatus(msg);
                        btnDressing.setEnabled(true);
                    }
                }
        );
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickLauncher.launch(intent);
    }

    private void takePhotoWithCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        try {
            cameraImageUri =
                    viewModel.createCameraImageUri(requireContext());
            takePictureLauncher.launch(cameraImageUri);
        } catch (Exception e) {
            showStatus("无法启动相机，请稍后再试");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                showStatus("未授予相机权限");
            }
        }
    }

    /* ---------------- 状态 UI ---------------- */

    private void showStatus(String msg) {
        tvLoading.setText(msg);
        tvLoading.setVisibility(View.VISIBLE);
    }

    private void clearStatus() {
        tvLoading.setVisibility(View.GONE);
    }
}
