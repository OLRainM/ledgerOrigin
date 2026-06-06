package com.ledger.origin.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ledger.origin.R;
import com.ledger.origin.ui.account.AccountManageActivity;
import com.ledger.origin.ui.category.CategoryManageActivity;
import com.ledger.origin.ui.login.LoginActivity;
import com.ledger.origin.util.PrefManager;

/** “我的”页：分类管理、账户管理、退出登录 */
public class MineFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView tvNick = view.findViewById(R.id.tv_nickname);
        tvNick.setText(PrefManager.getNickname());

        view.findViewById(R.id.item_category).setOnClickListener(v ->
                startActivity(new Intent(getContext(), CategoryManageActivity.class)));
        view.findViewById(R.id.item_account).setOnClickListener(v ->
                startActivity(new Intent(getContext(), AccountManageActivity.class)));
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> confirmLogout());
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("确定", (d, w) -> {
                    PrefManager.logout();
                    Intent i = new Intent(getContext(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    requireActivity().finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
