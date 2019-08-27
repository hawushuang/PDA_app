package com.microtechmd.pda_app.activity;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.adapter.BaseViewHolder;
import com.microtechmd.pda_app.adapter.CommonAdapter;
import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.BaseMsgEntity;
import com.microtechmd.pda_app.entity.Friend;
import com.microtechmd.pda_app.entity.FriendSearch;
import com.microtechmd.pda_app.network.HttpHeader;
import com.microtechmd.pda_app.network.NetPostUtil;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.util.JSONUtils;
import com.microtechmd.pda_app.util.MD5Utils;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;


public class FriendsSearchActivity extends ActivityPDA {
    private String accessId;
    private String signKey;

    private List<FriendSearch.ContentBean.UserlistBean> mDatas;
    private CommonAdapter<FriendSearch.ContentBean.UserlistBean> mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendsearch);
        accessId = (String) SPUtils.get(this, StringConstant.ACCESSID, "");
        signKey = (String) SPUtils.get(this, StringConstant.SIGNKEY, "");

        EditText et_keyword = findViewById(R.id.et_keyword);
        mRecyclerView = findViewById(R.id.list);

        initFriends();

        et_keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String key = editable.toString().trim();
                if (TextUtils.isEmpty(key)) {
                    mDatas.clear();
                    mAdapter.notifyDataSetChanged();
                } else {
                    search(key);
                }
            }
        });
    }

    private void initFriends() {
        mDatas = new ArrayList<>();
        mAdapter = new CommonAdapter<FriendSearch.ContentBean.UserlistBean>(this,
                R.layout.item_friend_search, mDatas) {
            @Override
            public void convert(BaseViewHolder holder, int position) {
                if ("2".equals(mDatas.get(position).getSex())) {
                    holder.setImageResource(R.id.sex_icon, R.drawable.ic_female);
                } else {
                    holder.setImageResource(R.id.sex_icon, R.drawable.ic_male);
                }

                holder.setText(R.id.item_userid, mDatas.get(position).getUserid());

                final String userNo = mDatas.get(position).getUserno();
                holder.getView(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showFriendAddDialog(userNo);
                    }
                });
            }
        };
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        //添加Android自带的分割线
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void showFriendAddDialog(final String userNo) {
        View friendAddView = LayoutInflater.from(this).inflate(R.layout.friend_add_content, null);
        final EditText et_remark = friendAddView.findViewById(R.id.et_remark);
        final EditText et_verinfo = friendAddView.findViewById(R.id.et_verinfo);
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
                String remark = et_remark.getText().toString().trim();
                String verInfo = et_verinfo.getText().toString().trim();
                sendFriendAddMessge(userNo, remark, verInfo);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void sendFriendAddMessge(String userNo, String remark, String verInfo) {
        Map<String, Object> reqcontent = new LinkedHashMap<>();
        reqcontent.put("userno", userNo);
        reqcontent.put("remark", remark);
        reqcontent.put("verinfo", verInfo);
        reqcontent.put("imsendflag", "1");

        NetPostUtil.doAction(accessId, signKey, reqcontent, "userfriendRequest",
                new MyStringCallback(FriendsSearchActivity.this) {
                    @Override
                    public void onResponse(String response, int id) {
                        super.onResponse(response, id);
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }
                        Friend authorModel = new Gson().fromJson(response, Friend.class);
                        if (authorModel == null) {
                            return;
                        }
                        if (authorModel.getInfo().getCode() == 100000) {
                            showToast(R.string.send_success);
                        } else {
                            showToast(authorModel.getInfo().getMsg());
                        }
                    }
                }
//                new ACallback<BaseMsgEntity>() {
//                    @Override
//                    public void onSuccess(BaseMsgEntity authorModel) {
//                        if (authorModel == null) {
//                            return;
//                        }
//                        if (authorModel.getInfo().getCode() == 100000) {
//                            showToast(R.string.send_success);
//                        } else {
//                            showToast(authorModel.getInfo().getMsg());
//                        }
//                    }
//
//                    @Override
//                    public void onFail(int errCode, String errMsg) {
//                        showToast(R.string.toast_network_connection_failed);
//                    }
//                }
        );
    }

    private void search(String keyword) {
        Map<String, Object> reqcontent = new LinkedHashMap<>();
        reqcontent.put("userid", keyword);
        reqcontent.put("useridflag", "");
        reqcontent.put("pagesize", "");
        reqcontent.put("currentpage", "");

        NetPostUtil.doAction(accessId, signKey, reqcontent, "userfriendunQuery",
                new MyStringCallback(FriendsSearchActivity.this) {
                    @Override
                    public void onResponse(String response, int id) {
                        super.onResponse(response, id);
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }
                        FriendSearch authorModel = new Gson().fromJson(response, FriendSearch.class);
                        if (authorModel == null) {
                            return;
                        }
                        if (authorModel.getInfo().getCode() != 100000) {
                            mDatas.clear();
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mDatas.clear();
                            mDatas.addAll(authorModel.getContent().getUserlist());
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                    }

                    @Override
                    public void onAfter(int id) {
                    }
                }
//        new ACallback<FriendSearch>() {
//            @Override
//            public void onSuccess(FriendSearch authorModel) {
//                if (authorModel == null) {
//                    return;
//                }
//                if (authorModel.getInfo().getCode() != 100000) {
//                    mDatas.clear();
//                    mAdapter.notifyDataSetChanged();
//                } else {
//                    mDatas.clear();
//                    mDatas.addAll(authorModel.getContent().getUserlist());
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onFail(int errCode, String errMsg) {
//                showToast(R.string.toast_network_connection_failed);
//            }
//        }
        );
    }

    @Override
    protected void onClickView(View v) {
        super.onClickView(v);
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            default:
                break;
        }
    }

    //处理后退键的情况
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
