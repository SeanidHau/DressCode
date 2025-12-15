package com.dresscode.app.ui.dressing;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dresscode.app.R;

import java.io.OutputStream;

public class DressingResultActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT_URL = "result_url";

    private ImageView ivResult;
    private Button btnSave, btnShare;

    // ✅ 保存接口返回的图片 URL（不要再从 ImageView Drawable 强转 Bitmap）
    private String resultUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dressing_result);

        ivResult = findViewById(R.id.ivResult);
        btnSave = findViewById(R.id.btnSave);
        btnShare = findViewById(R.id.btnShare);

        resultUrl = getIntent().getStringExtra(EXTRA_RESULT_URL);

        if (resultUrl != null && !resultUrl.trim().isEmpty()) {
            Glide.with(this).load(resultUrl).into(ivResult);
        } else {
            Toast.makeText(this, "结果图片地址为空", Toast.LENGTH_SHORT).show();
        }

        btnSave.setOnClickListener(v -> saveImageToGallery());
        btnShare.setOnClickListener(v -> shareImage());
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveImageToGallery() {
        if (resultUrl == null || resultUrl.trim().isEmpty()) {
            Toast.makeText(this, "图片还未准备好", Toast.LENGTH_SHORT).show();
            return;
        }

        Glide.with(this)
                .asBitmap()
                .load(resultUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                        try {
                            String savedUrl = insertImage(bitmap);
                            Toast.makeText(
                                    DressingResultActivity.this,
                                    savedUrl != null ? "保存成功" : "保存失败",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } catch (Exception e) {
                            Toast.makeText(
                                    DressingResultActivity.this,
                                    "保存失败: " + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                        // no-op
                    }

                    @Override
                    public void onLoadFailed(@Nullable android.graphics.drawable.Drawable errorDrawable) {
                        Toast.makeText(DressingResultActivity.this, "图片下载失败，无法保存", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void shareImage() {
        if (resultUrl == null || resultUrl.trim().isEmpty()) {
            Toast.makeText(this, "图片还未准备好", Toast.LENGTH_SHORT).show();
            return;
        }

        Glide.with(this)
                .asBitmap()
                .load(resultUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                        try {
                            String url = insertImage(bitmap);
                            if (url == null) {
                                Toast.makeText(DressingResultActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Uri uri = Uri.parse(url);

                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

                            // ✅ 关键：给接收方读取权限（Android 10+ 很重要）
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            startActivity(Intent.createChooser(shareIntent, "分享换装结果"));
                        } catch (Exception e) {
                            Toast.makeText(
                                    DressingResultActivity.this,
                                    "分享失败: " + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                        // no-op
                    }

                    @Override
                    public void onLoadFailed(@Nullable android.graphics.drawable.Drawable errorDrawable) {
                        Toast.makeText(DressingResultActivity.this, "图片下载失败，无法分享", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * ✅ 保存 Bitmap 到系统相册（MediaStore）
     * Android 10+ 不需要 WRITE_EXTERNAL_STORAGE
     */
    private String insertImage(Bitmap bitmap) throws Exception {
        String imageName = "dresscode_" + System.currentTimeMillis() + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        // ✅ Android 10+：保存到 Pictures/DressCode 目录，方便在相册里找
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DressCode");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = getContentResolver().insert(
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    values
            );
        } else {
            uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
            );
        }

        if (uri == null) return null;

        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            if (out == null) return null;
            boolean ok = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            if (!ok) return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);
        }

        return uri.toString();
    }
}
