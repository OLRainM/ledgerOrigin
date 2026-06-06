package com.ledger.origin.ui.budget;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ledger.origin.R;
import com.ledger.origin.model.ApiResponse;
import com.ledger.origin.model.Budget;
import com.ledger.origin.model.Category;
import com.ledger.origin.net.ApiClient;
import com.ledger.origin.util.DateUtil;
import com.ledger.origin.util.NotificationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 预算页：进度条展示 + 超支通知 */
public class BudgetFragment extends Fragment {

    private LinearLayout container;
    private TextView tvEmpty;
    private String month;
    private final List<Category> expenseCats = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inflater.inflate(R.layout.fragment_budget, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle b) {
        month = DateUtil.currentMonth();
        container = view.findViewById(R.id.budget_container);
        tvEmpty = view.findViewById(R.id.tv_empty);
        ((TextView) view.findViewById(R.id.tv_title)).setText(month + " 预算");
        view.findViewById(R.id.fab_add).setOnClickListener(v -> showAddDialog());
        loadCategories();
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void loadCategories() {
        Map<String, String> q = new HashMap<>();
        q.put("type", "1");
        ApiClient.get().getCategories(q).enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call,
                                   Response<ApiResponse<List<Category>>> r) {
                expenseCats.clear();
                if (r.body() != null && r.body().data != null) expenseCats.addAll(r.body().data);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
            }
        });
    }

    private void load() {
        Map<String, String> q = new HashMap<>();
        q.put("month", month);
        ApiClient.get().getBudgets(q).enqueue(new Callback<ApiResponse<List<Budget>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Budget>>> call,
                                   Response<ApiResponse<List<Budget>>> r) {
                if (r.body() != null && r.body().data != null) render(r.body().data);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Budget>>> call, Throwable t) {
            }
        });
    }

    private void render(List<Budget> budgets) {
        container.removeAllViews();
        tvEmpty.setVisibility(budgets.isEmpty() ? View.VISIBLE : View.GONE);
        for (Budget bud : budgets) {
            View v = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_budget, container, false);
            ((TextView) v.findViewById(R.id.tv_name)).setText(bud.category_name);
            ((TextView) v.findViewById(R.id.tv_detail))
                    .setText(String.format("已用 %.0f / %.0f", bud.spent, bud.amount));
            ProgressBar pb = v.findViewById(R.id.progress);
            pb.setProgress((int) Math.min(100, bud.percentage));

            TextView tvPct = v.findViewById(R.id.tv_percent);
            tvPct.setText(String.format("%.0f%%", bud.percentage));
            if (bud.percentage >= 100) {
                tvPct.setTextColor(0xFFEF5350);
                NotificationUtil.notifyBudget(requireContext(), "预算超支提醒",
                        bud.category_name + " 已超出预算！");
            } else if (bud.percentage >= 80) {
                tvPct.setTextColor(0xFFFFB300);
            }
            v.setOnClickListener(view -> confirmDelete(bud));
            container.addView(v);
        }
    }

    private void showAddDialog() {
        if (expenseCats.isEmpty()) {
            Toast.makeText(getContext(), "请先添加支出分类", Toast.LENGTH_SHORT).show();
            return;
        }
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_budget, null);
        Spinner sp = v.findViewById(R.id.spinner_category);
        EditText et = v.findViewById(R.id.et_amount);
        sp.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, expenseCats));

        new AlertDialog.Builder(requireContext())
                .setTitle("设置预算")
                .setView(v)
                .setPositiveButton("保存", (d, w) -> {
                    String amt = et.getText().toString().trim();
                    if (TextUtils.isEmpty(amt)) return;
                    Map<String, Object> body = new HashMap<>();
                    body.put("category_id", ((Category) sp.getSelectedItem()).id);
                    body.put("amount", Double.parseDouble(amt));
                    body.put("month", month);
                    ApiClient.get().createBudget(body).enqueue(refreshCb());
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void confirmDelete(Budget bud) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除预算")
                .setMessage("删除 " + bud.category_name + " 的预算？")
                .setPositiveButton("删除", (d, w) ->
                        ApiClient.get().deleteBudget(bud.id).enqueue(refreshCb()))
                .setNegativeButton("取消", null)
                .show();
    }

    private Callback<ApiResponse<Object>> refreshCb() {
        return new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> r) {
                String msg = r.body() != null ? r.body().msg : "完成";
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                load();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
