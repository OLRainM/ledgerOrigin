package com.ledger.origin.ui.record;

import android.app.DatePickerDialog;
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

import com.ledger.origin.R;
import com.ledger.origin.model.Account;
import com.ledger.origin.model.ApiResponse;
import com.ledger.origin.model.Category;
import com.ledger.origin.model.Transaction;
import com.ledger.origin.net.ApiClient;
import com.ledger.origin.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 添加/编辑记账页 */
public class AddRecordActivity extends AppCompatActivity {

    private TextView tabExpense, tabIncome, tvDate;
    private EditText etAmount, etNote;
    private Spinner spCategory, spAccount;
    private int type = 1; // 1支出 2收入
    private String date;
    private final List<Category> categories = new ArrayList<>();
    private final List<Account> accounts = new ArrayList<>();
    private Transaction editing; // 编辑模式

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_record);

        tabExpense = findViewById(R.id.tab_expense);
        tabIncome = findViewById(R.id.tab_income);
        tvDate = findViewById(R.id.tv_date);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        spCategory = findViewById(R.id.spinner_category);
        spAccount = findViewById(R.id.spinner_account);
        Button btnSave = findViewById(R.id.btn_save);

        editing = (Transaction) getIntent().getSerializableExtra("transaction");
        date = DateUtil.today();

        if (editing != null) {
            type = editing.type;
            etAmount.setText(String.valueOf(editing.amount));
            etNote.setText(editing.note);
            date = editing.date;
        }
        tvDate.setText(date);

        tabExpense.setOnClickListener(v -> switchType(1));
        tabIncome.setOnClickListener(v -> switchType(2));
        tvDate.setOnClickListener(v -> pickDate());
        btnSave.setOnClickListener(v -> save());

        switchType(type);
        loadAccounts();
    }

    private void switchType(int t) {
        type = t;
        if (t == 1) {
            tabExpense.setBackgroundResource(R.drawable.bg_btn_primary);
            tabExpense.setTextColor(0xFFFFFFFF);
            tabIncome.setBackgroundResource(R.drawable.bg_input);
            tabIncome.setTextColor(0xFF8D6E63);
        } else {
            tabIncome.setBackgroundResource(R.drawable.bg_btn_primary);
            tabIncome.setTextColor(0xFFFFFFFF);
            tabExpense.setBackgroundResource(R.drawable.bg_input);
            tabExpense.setTextColor(0xFF8D6E63);
        }
        loadCategories();
    }

    private void loadCategories() {
        Map<String, String> q = new HashMap<>();
        q.put("type", String.valueOf(type));
        ApiClient.get().getCategories(q).enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call,
                                   Response<ApiResponse<List<Category>>> r) {
                categories.clear();
                if (r.body() != null && r.body().data != null) categories.addAll(r.body().data);
                ArrayAdapter<Category> a = new ArrayAdapter<>(AddRecordActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, categories);
                spCategory.setAdapter(a);
                if (editing != null) selectCategory();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
            }
        });
    }

    private void loadAccounts() {
        ApiClient.get().getAccounts().enqueue(new Callback<ApiResponse<List<Account>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Account>>> call,
                                   Response<ApiResponse<List<Account>>> r) {
                accounts.clear();
                if (r.body() != null && r.body().data != null) accounts.addAll(r.body().data);
                ArrayAdapter<Account> a = new ArrayAdapter<>(AddRecordActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, accounts);
                spAccount.setAdapter(a);
                if (editing != null) selectAccount();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Account>>> call, Throwable t) {
            }
        });
    }

    private void selectCategory() {
        for (int i = 0; i < categories.size(); i++)
            if (categories.get(i).id == editing.category_id) { spCategory.setSelection(i); break; }
    }

    private void selectAccount() {
        for (int i = 0; i < accounts.size(); i++)
            if (accounts.get(i).id == editing.account_id) { spAccount.setSelection(i); break; }
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> {
            date = String.format("%04d-%02d-%02d", y, m + 1, d);
            tvDate.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String amountStr = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr) || Double.parseDouble(amountStr) <= 0) {
            Toast.makeText(this, "请输入正确金额", Toast.LENGTH_SHORT).show();
            return;
        }
        if (categories.isEmpty() || accounts.isEmpty()) {
            Toast.makeText(this, "请先添加分类和账户", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("type", type);
        body.put("amount", Double.parseDouble(amountStr));
        body.put("category_id", ((Category) spCategory.getSelectedItem()).id);
        body.put("account_id", ((Account) spAccount.getSelectedItem()).id);
        body.put("note", etNote.getText().toString().trim());
        body.put("date", date);

        Callback<ApiResponse<Object>> cb = new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> r) {
                if (r.body() != null && r.body().isSuccess()) {
                    Toast.makeText(AddRecordActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddRecordActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(AddRecordActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        };

        if (editing != null) {
            ApiClient.get().updateTransaction(editing.id, body).enqueue(cb);
        } else {
            ApiClient.get().createTransaction(body).enqueue(cb);
        }
    }
}
