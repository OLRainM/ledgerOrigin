package com.ledger.origin.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

/** 注册页面 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etNickname, etPassword, etConfirm;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.et_username);
        etNickname = findViewById(R.id.et_nickname);
        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);
        btnRegister = findViewById(R.id.btn_register);

        findViewById(R.id.tv_to_login).setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String username = etUsername.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(nickname)
                || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() < 3) {
            Toast.makeText(this, "用户名至少3位", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("nickname", nickname);
        body.put("password", password);

        ApiClient.get().register(body).enqueue(new Callback<ApiResponse<Dtos.LoginData>>() {
            @Override
            public void onResponse(Call<ApiResponse<Dtos.LoginData>> call,
                                   Response<ApiResponse<Dtos.LoginData>> response) {
                btnRegister.setEnabled(true);
                if (response.body() != null && response.body().isSuccess()) {
                    Dtos.LoginData d = response.body().data;
                    PrefManager.saveLogin(d.token, d.user_id, d.nickname);
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().msg : "注册失败";
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Dtos.LoginData>> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
