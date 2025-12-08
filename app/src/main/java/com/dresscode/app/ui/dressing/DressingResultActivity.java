package com.dresscode.app.ui.dressing;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dresscode.app.R;

import java.io.OutputStream;

public class DressingResultActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT_URL = "result_url";

    private ImageView ivResult;
    private Button btnSave, btnShare;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dressing_result);

        ivResult = findViewById(R.id.ivResult);
        btnSave = findViewById(R.id.btnSave);
        btnShare = findViewById(R.id.btnShare);

        String url = getIntent().getStringExtra(EXTRA_RESULT_URL);
        if (url != null) {
            Glide.with(this).load(url).into(ivResult);
        }

        btnSave.setOnClickListener(v -> saveImageToGallery());
        btnShare.setOnClickListener(v -> shareImage());
    }

    private void saveImageToGallery() {
        if (!(ivResult.getDrawable() instanceof BitmapDrawable)) {
            Toast.makeText(this, "图片还未加载完成", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = ((BitmapDrawable) ivResult.getDrawable()).getBitmap();
        try {
            String savedUrl = insertImage(bitmap);
            if (savedUrl != null) {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String insertImage(Bitmap bitmap) throws Exception {
        String imageName = "dresscode_" + System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            uri = getContentResolver().insert(
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    values);
        } else {
            uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values);
        }

        if (uri == null) return null;

        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);
        }

        return uri.toString();
    }

    private void shareImage() {
        if (!(ivResult.getDrawable() instanceof BitmapDrawable)) {
            Toast.makeText(this, "图片还未加载完成", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = ((BitmapDrawable) ivResult.getDrawable()).getBitmap();
        try {
            String url = insertImage(bitmap);
            if (url == null) {
                Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = Uri.parse(url);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, "分享换装结果"));
        } catch (Exception e) {
            Toast.makeText(this, "分享失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
