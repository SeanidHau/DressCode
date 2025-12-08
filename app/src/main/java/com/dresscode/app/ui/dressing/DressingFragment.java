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

    private DressingViewModel viewModel;
    private FavoriteOutfitAdapter favoriteAdapter;

    private Uri selectedPhotoUri;
    private OutfitEntity selectedOutfit;

    private ActivityResultLauncher<Intent> pickLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private androidx.activity.result.ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri cameraImageUri;

    private static final int REQUEST_CAMERA_PERMISSION = 2001;

    public DressingFragment() {}

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
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(DressingViewModel.class);
    }

    private void initActivityResultLaunchers() {
        pickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedPhotoUri = result.getData().getData();
                        Glide.with(requireContext())
                                .load(selectedPhotoUri)
                                .into(ivUserPhoto);
                    }
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess != null && isSuccess) {
                        // 使用我们预先创建的 Uri
                        selectedPhotoUri = cameraImageUri;
                        if (selectedPhotoUri != null) {
                            Glide.with(requireContext())
                                    .load(selectedPhotoUri)
                                    .into(ivUserPhoto);
                        }
                    } else {
                        // 用户取消或者失败，不用弹「无法创建」那种误导性的提示
                    }
                });
    }

    private void setupFavoriteList() {
        rvFavorites.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(
                        getContext(),
                        RecyclerView.HORIZONTAL,
                        false
                )
        );
        favoriteAdapter = new FavoriteOutfitAdapter(outfit -> {
            selectedOutfit = outfit;
            favoriteAdapter.setSelectedId(outfit.id);
        });
        rvFavorites.setAdapter(favoriteAdapter);
    }

    private void setupListeners() {
        btnPick.setOnClickListener(v -> pickImageFromGallery());
        btnCamera.setOnClickListener(v -> takePhotoWithCamera());
        btnDressing.setOnClickListener(v -> {
            if (selectedPhotoUri == null) {
                Toast.makeText(getContext(), "请先选择或拍摄一张照片", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedOutfit == null) {
                Toast.makeText(getContext(), "请先选择一套收藏穿搭", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.startDressing(requireContext(), selectedPhotoUri, selectedOutfit.id);
        });
    }

    private void observeViewModel() {
        viewModel.getFavoriteList().observe(getViewLifecycleOwner(), this::onFavoriteListChanged);
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                tvLoading.setVisibility(View.VISIBLE);
                btnDressing.setEnabled(false);
            } else {
                tvLoading.setVisibility(View.GONE);
                btnDressing.setEnabled(true);
            }
        });
        viewModel.getDressingResult().observe(getViewLifecycleOwner(), url -> {
            if (url != null) {
                Intent intent = new Intent(getContext(), DressingResultActivity.class);
                intent.putExtra(DressingResultActivity.EXTRA_RESULT_URL, url);
                startActivity(intent);
            }
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onFavoriteListChanged(List<OutfitEntity> outfits) {
        favoriteAdapter.submitList(outfits);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickLauncher.launch(intent);
    }

    private void takePhotoWithCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        try {
            // 让 ViewModel 创建一个用于保存图片的 Uri
            cameraImageUri = viewModel.createCameraImageUri(requireContext());
            // 启动系统相机拍照，结果会写入这个 Uri
            takePictureLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "创建拍照文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == REQUEST_CAMERA_PERMISSION) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        launchCamera();
                    } else {
                        Toast.makeText(getContext(), "未授予相机权限", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getContext(), "未授予相机权限", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
