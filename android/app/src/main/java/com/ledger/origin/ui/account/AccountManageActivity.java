package com.ledger.origin.ui.account;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ledger.origin.R;
import com.ledger.origin.model.Account;
import com.ledger.origin.model.ApiResponse;
import com.ledger.origin.net.ApiClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 账户管理：使用 ListView 展示，支持增删改 */
public class AccountManageActivity extends AppCompatActivity {

    private final List<String> TYPE_KEYS = Arrays.asList(
            "cash", "alipay", "wechat", "bank", "credit", "other");

    private final List<Account> data = new ArrayList<>();
    private AccountListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_account_manage);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        ListView lv = findViewById(R.id.list_view);
        adapter = new AccountListAdapter(this, data);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((p, view, pos, id) -> showEditDialog(data.get(pos)));

        findViewById(R.id.fab_add).setOnClickListener(v -> showEditDialog(null));
        load();
    }

    private void load() {
        ApiClient.get().getAccounts().enqueue(new Callback<ApiResponse<List<Account>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Account>>> call,
                                   Response<ApiResponse<List<Account>>> r) {
                data.clear();
                if (r.body() != null && r.body().data != null) data.addAll(r.body().data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Account>>> call, Throwable t) {
            }
        });
    }

    private void showEditDialog(@Nullable Account acc) {
        View v = getLayoutInflater().inflate(R.layout.dialog_account, null);
        EditText etName = v.findViewById(R.id.et_name);
        EditText etBalance = v.findViewById(R.id.et_balance);
        Spinner spType = v.findViewById(R.id.spinner_type);
        spType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, TYPE_KEYS));

        if (acc != null) {
            etName.setText(acc.name);
            etBalance.setText(String.valueOf(acc.balance));
            int idx = TYPE_KEYS.indexOf(acc.type);
            if (idx >= 0) spType.setSelection(idx);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(acc == null ? "新增账户" : "编辑账户")
                .setView(v)
                .setPositiveButton("保存", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "请输入账户名", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String balStr = etBalance.getText().toString().trim();
                    double bal = TextUtils.isEmpty(balStr) ? 0 : Double.parseDouble(balStr);
                    String typeKey = spType.getSelectedItem().toString();

                    Map<String, Object> body = new HashMap<>();
                    body.put("name", name);
                    body.put("type", typeKey);
                    body.put("balance", bal);
                    body.put("icon", typeKey);
                    if (acc == null) {
                        ApiClient.get().createAccount(body).enqueue(refreshCb());
                    } else {
                        ApiClient.get().updateAccount(acc.id, body).enqueue(refreshCb());
                    }
                })
                .setNegativeButton("取消", null);
        if (acc != null) {
            builder.setNeutralButton("删除", (d, w) ->
                    ApiClient.get().deleteAccount(acc.id).enqueue(refreshCb()));
        }
        builder.show();
    }

    private Callback<ApiResponse<Object>> refreshCb() {
        return new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> r) {
                String msg = r.body() != null ? r.body().msg : "操作完成";
                Toast.makeText(AccountManageActivity.this, msg, Toast.LENGTH_SHORT).show();
                load();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(AccountManageActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
