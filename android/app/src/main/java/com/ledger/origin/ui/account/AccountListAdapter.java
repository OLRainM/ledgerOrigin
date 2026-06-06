package com.ledger.origin.ui.account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ledger.origin.R;
import com.ledger.origin.model.Account;
import com.ledger.origin.util.IconUtil;

import java.util.List;

/** ListView 适配器，展示账户名称与余额 */
public class AccountListAdapter extends BaseAdapter {

    private final Context context;
    private final List<Account> data;

    public AccountListAdapter(Context context, List<Account> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_account, parent, false);
        }
        Account a = data.get(position);
        TextView tvIcon = convertView.findViewById(R.id.tv_icon);
        TextView tvName = convertView.findViewById(R.id.tv_name);
        TextView tvBalance = convertView.findViewById(R.id.tv_balance);

        tvIcon.setText(IconUtil.emoji(a.icon));
        tvName.setText(a.name);
        tvBalance.setText(String.format("¥ %.2f", a.balance));
        return convertView;
    }
}
