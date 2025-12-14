package com.dresscode.app.ui.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.dresscode.app.R;
import com.dresscode.app.viewmodel.RegisterViewModel;
import com.dresscode.app.viewmodel.RegisterViewModelFactory;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNickname, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoLogin;

    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        RegisterViewModelFactory factory = new RegisterViewModelFactory(getApplicationContext());
        viewModel = new ViewModelProvider(this, factory).get(RegisterViewModel.class);

        TextView tvGoLogin = findViewById(R.id.tvGoLogin);
        setupGoLogin(tvGoLogin);

        initViews();
        initEvents();
        observeViewModel();
    }

    private void initViews() {
        etNickname = findViewById(R.id.etNickname);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);
    }

    private void initEvents() {
        btnRegister.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            viewModel.register(nickname, email, password, confirm);
        });

        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        viewModel.registerState.observe(this, state -> {
            if (state == null) return;

            if (state.loading) {
                btnRegister.setEnabled(false);
            } else {
                btnRegister.setEnabled(true);
            }

            if (state.message != null) {
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show();
            }

            if (state.success) {
                // 注册成功，返回登录页
                finish();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int[] outLocation = new int[2];
                v.getLocationOnScreen(outLocation);

                float x = ev.getRawX() + v.getLeft() - outLocation[0];
                float y = ev.getRawY() + v.getTop() - outLocation[1];

                // 点击位置超出 EditText 区域 = 失焦 + 收起键盘
                if (x < v.getLeft() || x > v.getRight()
                        || y < v.getTop() || y > v.getBottom()) {
                    hideKeyboard();
                    v.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupGoLogin(TextView tv) {
        String fullText = "已有账号？返回登录";
        String actionText = "返回登录";

        SpannableString span = new SpannableString(fullText);
        int start = fullText.indexOf(actionText);
        int end = start + actionText.length();

        span.setSpan(
                new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.dc_text_primary)
                ),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        span.setSpan(
                new UnderlineSpan(),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        span.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(true);
                        ds.setColor(ContextCompat.getColor(RegisterActivity.this, R.color.dc_text_primary));
                    }
                },
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tv.setText(span);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setHighlightColor(Color.TRANSPARENT);
    }
}
