package com.ledger.origin.ui.record;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ledger.origin.R;
import com.ledger.origin.model.ApiResponse;
import com.ledger.origin.model.Dtos;
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

/** 明细列表页（含月度概览） */
public class RecordListFragment extends Fragment {

    private TextView tvMonth, tvBalance, tvIncome, tvExpense;
    private SwipeRefreshLayout swipe;
    private final List<Transaction> data = new ArrayList<>();
    private TransactionAdapter adapter;
    private String month;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c,
                             @Nullable Bundle b) {
        return inflater.inflate(R.layout.fragment_record_list, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle b) {
        month = DateUtil.currentMonth();
        tvMonth = view.findViewById(R.id.tv_month);
        tvBalance = view.findViewById(R.id.tv_balance);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);
        swipe = view.findViewById(R.id.swipe);

        RecyclerView rv = view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(data, new TransactionAdapter.OnItemClick() {
            @Override
            public void onClick(Transaction t) {
                Intent i = new Intent(getContext(), AddRecordActivity.class);
                i.putExtra("transaction", t);
                startActivity(i);
            }

            @Override
            public void onLongClick(Transaction t) {
                confirmDelete(t);
            }
        });
        rv.setAdapter(adapter);

        swipe.setOnRefreshListener(this::loadData);
        view.findViewById(R.id.tv_pick_month).setOnClickListener(v -> pickMonth());
        tvMonth.setText(month);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void pickMonth() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (vw, y, m, d) -> {
            month = String.format("%04d-%02d", y, m + 1);
            tvMonth.setText(month);
            loadData();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
        dialog.show();
    }

    private void loadData() {
        loadOverview();
        Map<String, String> q = new HashMap<>();
        q.put("date_from", DateUtil.monthStart(month));
        q.put("date_to", DateUtil.monthEnd(month));
        q.put("page_size", "200");
        ApiClient.get().getTransactions(q).enqueue(new Callback<ApiResponse<Dtos.PageData<Transaction>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Dtos.PageData<Transaction>>> call,
                                   Response<ApiResponse<Dtos.PageData<Transaction>>> r) {
                swipe.setRefreshing(false);
                if (r.body() != null && r.body().isSuccess() && r.body().data != null) {
                    data.clear();
                    if (r.body().data.list != null) data.addAll(r.body().data.list);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Dtos.PageData<Transaction>>> call, Throwable t) {
                swipe.setRefreshing(false);
            }
        });
    }

    private void loadOverview() {
        Map<String, String> q = new HashMap<>();
        q.put("month", month);
        ApiClient.get().getOverview(q).enqueue(new Callback<ApiResponse<Dtos.Overview>>() {
            @Override
            public void onResponse(Call<ApiResponse<Dtos.Overview>> call,
                                   Response<ApiResponse<Dtos.Overview>> r) {
                if (r.body() != null && r.body().data != null) {
                    Dtos.Overview o = r.body().data;
                    tvBalance.setText(String.format("%.2f", o.balance));
                    tvIncome.setText(String.format("收入 %.2f", o.income));
                    tvExpense.setText(String.format("支出 %.2f", o.expense));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Dtos.Overview>> call, Throwable t) {
            }
        });
    }

    private void confirmDelete(Transaction t) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除记录")
                .setMessage("确定删除这条记录吗？")
                .setPositiveButton("删除", (d, w) -> doDelete(t))
                .setNegativeButton("取消", null)
                .show();
    }

    private void doDelete(Transaction t) {
        ApiClient.get().deleteTransaction(t.id).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> r) {
                Toast.makeText(getContext(), "已删除", Toast.LENGTH_SHORT).show();
                loadData();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
