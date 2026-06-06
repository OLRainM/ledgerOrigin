package com.ledger.origin.ui.stats;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.ledger.origin.R;
import com.ledger.origin.model.ApiResponse;
import com.ledger.origin.model.Dtos;
import com.ledger.origin.net.ApiClient;
import com.ledger.origin.util.DateUtil;
import com.ledger.origin.util.IconUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 统计页：饼图 + 分类排行 */
public class StatsFragment extends Fragment {

    private static final int[] WARM_COLORS = {
            0xFFFF7043, 0xFFFFB300, 0xFFFFA726, 0xFFEF5350,
            0xFFFF8A65, 0xFFFFCA28, 0xFFD4956A, 0xFFE57373};

    private PieChart pieChart;
    private TextView tvMonth, tvType, tvEmpty;
    private LinearLayout listContainer;
    private String month;
    private int type = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inflater.inflate(R.layout.fragment_stats, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle b) {
        month = DateUtil.currentMonth();
        pieChart = view.findViewById(R.id.pie_chart);
        tvMonth = view.findViewById(R.id.tv_month);
        tvType = view.findViewById(R.id.tv_type);
        tvEmpty = view.findViewById(R.id.tv_empty);
        listContainer = view.findViewById(R.id.list_container);

        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.getLegend().setEnabled(false);

        tvMonth.setText(month);
        view.findViewById(R.id.tv_pick_month).setOnClickListener(v -> pickMonth());
        tvType.setOnClickListener(v -> {
            type = type == 1 ? 2 : 1;
            tvType.setText(type == 1 ? "支出" : "收入");
            load();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void pickMonth() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (vw, y, m, d) -> {
            month = String.format("%04d-%02d", y, m + 1);
            tvMonth.setText(month);
            load();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1).show();
    }

    private void load() {
        Map<String, String> q = new HashMap<>();
        q.put("month", month);
        q.put("type", String.valueOf(type));
        ApiClient.get().getCategoryStat(q).enqueue(new Callback<ApiResponse<List<Dtos.CategoryStat>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Dtos.CategoryStat>>> call,
                                   Response<ApiResponse<List<Dtos.CategoryStat>>> r) {
                if (r.body() != null && r.body().data != null) {
                    render(r.body().data);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Dtos.CategoryStat>>> call, Throwable t) {
            }
        });
    }

    private void render(List<Dtos.CategoryStat> stats) {
        listContainer.removeAllViews();
        if (stats.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);

        double total = 0;
        for (Dtos.CategoryStat s : stats) total += s.amount;

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < stats.size(); i++) {
            Dtos.CategoryStat s = stats.get(i);
            entries.add(new PieEntry((float) s.amount, s.category_name));
            colors.add(WARM_COLORS[i % WARM_COLORS.length]);
            addRow(s, total, WARM_COLORS[i % WARM_COLORS.length]);
        }

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(colors);
        ds.setValueTextColor(Color.WHITE);
        ds.setValueTextSize(11f);
        pieChart.setData(new PieData(ds));
        pieChart.invalidate();
        pieChart.animateY(600);
    }

    private void addRow(Dtos.CategoryStat s, double total, int color) {
        View row = LayoutInflater.from(getContext())
                .inflate(R.layout.item_stat, listContainer, false);
        ((TextView) row.findViewById(R.id.tv_icon)).setText(IconUtil.emoji(s.category_icon));
        ((TextView) row.findViewById(R.id.tv_name)).setText(s.category_name);
        double pct = total > 0 ? s.amount / total * 100 : 0;
        ((TextView) row.findViewById(R.id.tv_percent)).setText(String.format("%.1f%%", pct));
        TextView amt = row.findViewById(R.id.tv_amount);
        amt.setText(String.format("%.2f", s.amount));
        amt.setTextColor(color);
        listContainer.addView(row);
    }
}
