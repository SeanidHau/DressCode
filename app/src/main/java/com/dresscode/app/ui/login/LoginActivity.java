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
import com.dresscode.app.ui.main.MainActivity;
import com.dresscode.app.viewmodel.LoginViewModel;
import com.dresscode.app.viewmodel.LoginViewModelFactory;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoRegister;

    private LoginViewModel viewModel;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView tvGoRegister = findViewById(R.id.tvGoRegister);
        setupGoRegister(tvGoRegister);

        // ViewModel
        LoginViewModelFactory factory = new LoginViewModelFactory(getApplicationContext());
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        if (viewModel.isLoggedIn()) {
            goToMain();
            return;
        }

        initViews();
        setupGoRegister(tvGoRegister);
        initEvents();
        observeViewModel();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void initEvents() {
        btnLogin.setOnClickListener(v -> {
            hideKeyboard();
            clearError();

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // ✅ 轻量校验（不 toast）
            if (email.isEmpty()) {
                showError("请输入邮箱");
                etEmail.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                showError("请输入密码");
                etPassword.requestFocus();
                return;
            }

            viewModel.login(email, password);
        });
    }

    private void observeViewModel() {
        viewModel.loginState.observe(this, state -> {
            if (state == null) return;

            // 简单处理：toast + 导航
            if (state.loading) {
                btnLogin.setEnabled(false);
                btnLogin.setText("登录中…");
            } else {
                btnLogin.setEnabled(true);
                btnLogin.setText("登录");
            }

            if (state.success) {
                goToMain();
                return;
            }

            if (state.message != null && !state.message.trim().isEmpty()) {
                showError(state.message);
            }
        });
    }

    private void showError(String msg) {
        if (tvStatus == null) return;
        tvStatus.setText(msg);
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        if (tvStatus == null) return;
        tvStatus.setText("");
        tvStatus.setVisibility(View.GONE);
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
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

    private void setupGoRegister(TextView tv) {
        String fullText = "还没有账号？去注册";
        String actionText = "去注册";

        SpannableString span = new SpannableString(fullText);
        int start = fullText.indexOf(actionText);
        int end = start + actionText.length();

        // 黑色
        span.setSpan(
                new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.dc_text_primary)
                ),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // 下划线
        span.setSpan(
                new UnderlineSpan(),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // 点击事件
        span.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                        finish();
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        // 关键：去掉系统默认高亮
                        ds.setUnderlineText(true);
                        ds.setColor(ContextCompat.getColor(LoginActivity.this, R.color.dc_text_primary));
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
