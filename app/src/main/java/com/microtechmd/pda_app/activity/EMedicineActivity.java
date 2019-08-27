package com.microtechmd.pda_app.activity;


import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.adapter.BaseViewHolder;
import com.microtechmd.pda_app.adapter.CommonAdapter;
import com.microtechmd.pda_app.adapter.MultiItemCommonAdapter;
import com.microtechmd.pda_app.adapter.MultiItemTypeSupport;
import com.microtechmd.pda_app.entity.MedicineEntity;
import com.microtechmd.pda_app.fragment.FragmentDialog;
import com.microtechmd.pda_app.fragment.FragmentInput;
import com.triggertrap.seekarc.SeekArc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EMedicineActivity extends ActivityPDA {
    private ImageButton back;
    private TextView tv_save;
    private TextView tv_medicine_name;
    private TextView tv_medicine_count;
    private RecyclerView mFlowLayout;
    private int TYPE_COMMON = 0;
    private int TYPE_ADD = 1;
    private List<MedicineEntity> mData;
    private MultiItemCommonAdapter<MedicineEntity> adapter;
    private int selectedIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emedicine);

        mData = new ArrayList<>();
        MedicineEntity medicineEntity;
        medicineEntity = new MedicineEntity();
        medicineEntity.setName("Metformin");
        medicineEntity.setCount("1000");
        mData.add(medicineEntity);

        medicineEntity = new MedicineEntity();
        medicineEntity.setName("Metformi");
        medicineEntity.setCount("500");
        mData.add(medicineEntity);

        medicineEntity = new MedicineEntity();
        medicineEntity.setName("Acarbose");
        medicineEntity.setCount("500");
        mData.add(medicineEntity);

        medicineEntity = new MedicineEntity();
        medicineEntity.setName("Avandamet");
        medicineEntity.setCount("800");
        mData.add(medicineEntity);

        mData.add(new MedicineEntity());

        back = findViewById(R.id.ibt_back);
        tv_save = findViewById(R.id.tv_save);
        tv_medicine_name = findViewById(R.id.tv_medicine_name);
        tv_medicine_count = findViewById(R.id.tv_medicine_count);
        mFlowLayout = findViewById(R.id.content_layout);


        MultiItemTypeSupport<MedicineEntity> support = new MultiItemTypeSupport<MedicineEntity>() {
            @Override
            public int getLayoutId(int itemType) {
                if (itemType == TYPE_COMMON) {
                    return R.layout.auto_item2;
                } else {
                    return R.layout.auto_item_add;
                }

            }

            @Override
            public int getItemViewType(int position, MedicineEntity s) {
                if (position != mData.size() - 1) {
                    return TYPE_COMMON;
                } else {
                    return TYPE_ADD;
                }
            }
        };
        adapter = new MultiItemCommonAdapter<MedicineEntity>(this, mData, support) {
            @Override
            public void convert(BaseViewHolder holder, int position) {
                if (position == selectedIndex) {
                    holder.getItemView().setBackgroundResource(R.drawable.auto_tag_selected);
                } else {
                    holder.getItemView().setBackgroundResource(R.drawable.auto_tag_normal);
                }
                if (position != mData.size() - 1) {
                    holder.setText(R.id.auto_text1, mData.get(position).getName());
                    holder.setText(R.id.auto_text2, mData.get(position).getCount() + "mg");
                }
            }
        };
        RecyclerView.LayoutManager manager = new GridLayoutManager(this, 3);
        //添加默认分割线
//        mFlowLayout.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//        mFlowLayout.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        mFlowLayout.setLayoutManager(manager);
        mFlowLayout.setAdapter(adapter);
        adapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (position == selectedIndex) {
                    return;
                }
                if (position != mData.size() - 1) {
                    BaseViewHolder viewHolder = (BaseViewHolder) mFlowLayout.findViewHolderForLayoutPosition(selectedIndex);
                    if (viewHolder != null) {
                        viewHolder.getItemView().setBackgroundResource(R.drawable.auto_tag_normal);
                    } else {
                        adapter.notifyItemChanged(selectedIndex);
                    }
                    selectedIndex = position;
                    ((BaseViewHolder) holder).getItemView().setBackgroundResource(R.drawable.auto_tag_selected);

                    tv_medicine_name.setText(mData.get(selectedIndex).getName());
                    tv_medicine_count.setText(mData.get(selectedIndex).getCount());

                } else {
                    showAddDialog();
                }
            }
        });
        adapter.setOnItemLongClickListener(new CommonAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (position != mData.size() - 1) {
                    showDeleteDialog(position);
                }
                return false;
            }
        });
        initClick();
    }

    private void showAddDialog() {
        final FragmentInput fragmentInput = new FragmentInput();
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER,
                "");
        fragmentInput.setInputWidth(FragmentInput.POSITION_CENTER, 450);
        showDialogConfirm(getString(R.string.add), "",
                "", fragmentInput, true, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                String add_name = fragmentInput.getInputText(
                                        FragmentInput.POSITION_CENTER);
                                if (TextUtils.isEmpty(add_name.trim())) {
                                    showToast(R.string.input_empty);
                                    return false;
                                } else {
//                                    mData.set(mData.size() - 1, add_name.trim());
//                                    mData.add("add");
//                                    selectedIndex = mData.size() - 2;
//                                    adapter.notifyDataSetChanged();
                                }

                                break;
                            default:
                                break;
                        }

                        return true;
                    }
                });
    }

    private void showDeleteDialog(final int position) {
        final String name = mData.get(position).getName();
        View friendAddView = LayoutInflater.from(this).inflate(R.layout.delete_friend_content, null);
        TextView textView_delete_name = friendAddView.findViewById(R.id.delete_name);
        textView_delete_name.setText(name + "？");
        Button ok = friendAddView.findViewById(R.id.btn_ok);
        Button cancel = friendAddView.findViewById(R.id.btn_cancel);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(true).create();
        dialog.show();
        dialog.getWindow().setContentView(friendAddView);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mData.remove(position);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
                if (mData.size() == 1) {
                    selectedIndex = -1;
                } else {
                    selectedIndex = 0;
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void initClick() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
