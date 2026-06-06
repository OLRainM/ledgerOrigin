package com.ledger.origin.ui.record;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ledger.origin.R;
import com.ledger.origin.model.Transaction;
import com.ledger.origin.util.IconUtil;

import java.util.List;

/** 交易记录列表适配器 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {

    public interface OnItemClick {
        void onClick(Transaction t);
        void onLongClick(Transaction t);
    }

    private final List<Transaction> data;
    private final OnItemClick listener;

    public TransactionAdapter(List<Transaction> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Transaction t = data.get(position);
        h.tvIcon.setText(IconUtil.emoji(t.category_icon));
        h.tvName.setText(t.category_name != null ? t.category_name : "未分类");
        h.tvNote.setText(t.note == null || t.note.isEmpty() ? t.account_name : t.note);

        if (t.isExpense()) {
            h.tvAmount.setText(String.format("-%.2f", t.amount));
            h.tvAmount.setTextColor(0xFFEF5350);
        } else {
            h.tvAmount.setText(String.format("+%.2f", t.amount));
            h.tvAmount.setTextColor(0xFF66BB6A);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(t));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(t);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName, tvNote, tvAmount;

        VH(@NonNull View v) {
            super(v);
            tvIcon = v.findViewById(R.id.tv_icon);
            tvName = v.findViewById(R.id.tv_name);
            tvNote = v.findViewById(R.id.tv_note);
            tvAmount = v.findViewById(R.id.tv_amount);
        }
    }
}
