package com.ledger.origin.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ledger.origin.R;
import com.ledger.origin.model.Category;
import com.ledger.origin.util.IconUtil;

import java.util.List;

/** 分类网格适配器 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface OnClick {
        void onClick(Category c);
    }

    private final List<Category> data;
    private final OnClick listener;

    public CategoryAdapter(List<Category> data, OnClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Category c = data.get(position);
        h.tvIcon.setText(IconUtil.emoji(c.icon));
        h.tvName.setText(c.name);
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName;

        VH(@NonNull View v) {
            super(v);
            tvIcon = v.findViewById(R.id.tv_icon);
            tvName = v.findViewById(R.id.tv_name);
        }
    }
}
