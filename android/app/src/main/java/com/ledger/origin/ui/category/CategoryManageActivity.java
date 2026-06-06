package com.ledger.origin.ui.category;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ledger.origin.R;
import com.ledger.origin.model.ApiResponse;
import com.ledger.origin.model.Category;
import com.ledger.origin.net.ApiClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 分类管理：支出/收入切换 + 增删改 */
public class CategoryManageActivity extends AppCompatActivity {

    private final List<String> ICON_KEYS = Arrays.asList(
            "food", "transport", "shopping", "entertainment", "housing",
            "medical", "education", "communication", "salary", "bonus",
            "investment", "parttime", "other");

    private TextView tabExpense, tabIncome;
    private int type = 1;
    private final List<Category> data = new ArrayList<>();
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_category_manage);

        tabExpense = findViewById(R.id.tab_expense);
        tabIncome = findViewById(R.id.tab_income);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new CategoryAdapter(data, this::showEditDialog);
        rv.setAdapter(adapter);

        tabExpense.setOnClickListener(v -> switchType(1));
        tabIncome.setOnClickListener(v -> switchType(2));
        findViewById(R.id.fab_add).setOnClickListener(v -> showEditDialog(null));

        switchType(1);
    }

    private void switchType(int t) {
        type = t;
        tabExpense.setBackgroundResource(t == 1 ? R.drawable.bg_btn_primary : R.drawable.bg_input);
        tabExpense.setTextColor(t == 1 ? 0xFFFFFFFF : 0xFF8D6E63);
        tabIncome.setBackgroundResource(t == 2 ? R.drawable.bg_btn_primary : R.drawable.bg_input);
        tabIncome.setTextColor(t == 2 ? 0xFFFFFFFF : 0xFF8D6E63);
        load();
    }

    private void load() {
        Map<String, String> q = new HashMap<>();
        q.put("type", String.valueOf(type));
        ApiClient.get().getCategories(q).enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call,
                                   Response<ApiResponse<List<Category>>> r) {
                data.clear();
                if (r.body() != null && r.body().data != null) data.addAll(r.body().data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
            }
        });
    }

    private void showEditDialog(@Nullable Category cat) {
        View v = getLayoutInflater().inflate(R.layout.dialog_category, null);
        EditText etName = v.findViewById(R.id.et_name);
        Spinner spIcon = v.findViewById(R.id.spinner_icon);
        ArrayAdapter<String> iconAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ICON_KEYS);
        spIcon.setAdapter(iconAdapter);

        if (cat != null) {
            etName.setText(cat.name);
            int idx = ICON_KEYS.indexOf(cat.icon);
            if (idx >= 0) spIcon.setSelection(idx);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(cat == null ? "新增分类" : "编辑分类")
                .setView(v)
                .setPositiveButton("保存", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "请输入名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("name", name);
                    body.put("icon", spIcon.getSelectedItem().toString());
                    body.put("type", type);
                    body.put("sort_order", 0);
                    if (cat == null) {
                        ApiClient.get().createCategory(body).enqueue(refreshCb());
                    } else {
                        ApiClient.get().updateCategory(cat.id, body).enqueue(refreshCb());
                    }
                })
                .setNegativeButton("取消", null);
        if (cat != null) {
            builder.setNeutralButton("删除", (d, w) ->
                    ApiClient.get().deleteCategory(cat.id).enqueue(refreshCb()));
        }
        builder.show();
    }

    private Callback<ApiResponse<Object>> refreshCb() {
        return new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> r) {
                String msg = r.body() != null ? r.body().msg : "操作完成";
                Toast.makeText(CategoryManageActivity.this, msg, Toast.LENGTH_SHORT).show();
                load();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CategoryManageActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
