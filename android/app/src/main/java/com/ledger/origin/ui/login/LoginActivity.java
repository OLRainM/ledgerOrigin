package com.ledger.origin.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ledger.origin.R;
import com.ledger.origin.model.ApiResponse;
import com.ledger.origin.model.Dtos;
import com.ledger.origin.net.ApiClient;
import com.ledger.origin.ui.main.MainActivity;
import com.ledger.origin.util.PrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 登录页面 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        TextView tvRegister = findViewById(R.id.tv_to_register);

        btnLogin.setOnClickListener(v -> doLogin());
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        ApiClient.get().login(body).enqueue(new Callback<ApiResponse<Dtos.LoginData>>() {
            @Override
            public void onResponse(Call<ApiResponse<Dtos.LoginData>> call,
                                   Response<ApiResponse<Dtos.LoginData>> response) {
                btnLogin.setEnabled(true);
                if (response.body() != null && response.body().isSuccess()) {
                    Dtos.LoginData d = response.body().data;
                    PrefManager.saveLogin(d.token, d.user_id, d.nickname);
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().msg : "登录失败";
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Dtos.LoginData>> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
