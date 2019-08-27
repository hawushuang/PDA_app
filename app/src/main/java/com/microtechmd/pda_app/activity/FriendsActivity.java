package com.microtechmd.pda_app.activity;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.adapter.BaseViewHolder;
import com.microtechmd.pda_app.adapter.CommonAdapter;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.BaseMsgEntity;
import com.microtechmd.pda_app.entity.Friend;
import com.microtechmd.pda_app.entity.FriendRequest;
import com.microtechmd.pda_app.network.NetPostUtil;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.widget.badgeview.BadgeFactory;
import com.microtechmd.pda_app.widget.badgeview.BadgeView;
import com.microtechmd.pda_app.widget.myswitchbutton.LukeSwitchButton;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.microtechmd.pda_app.constant.StringConstant.FRIUSERNO;


public class FriendsActivity extends ActivityPDA {
    private String accessId;
    private String signKey;

    private List<Friend.ContentBean.FriendlistBean> mDatas;
    private TextView tv_title;
    private BadgeView badgeView;
    private RecyclerView mRecyclerView;
    private CommonAdapter<Friend.ContentBean.FriendlistBean> adapter;
    private SmartRefreshLayout refresh;

    private TextView empty_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        accessId = (String) SPUtils.get(this, StringConstant.ACCESSID, "");
        signKey = (String) SPUtils.get(this, StringConstant.SIGNKEY, "");

        mDatas = new ArrayList<>();

        tv_title = findViewById(R.id.text_view_title_bar);
        mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        refresh = findViewById(R.id.refresh);
        refresh.setRefreshHeader(new ClassicsHeader(this));
        refresh.setHeaderHeight(60);
        empty_view = findViewById(R.id.empty_view);

        badgeView = BadgeFactory.createCircle(this);
        badgeView.setBadgeGravity(Gravity.END | Gravity.TOP)
                .setSpace(12, 6);
        loadMsg();
        initFriends();
        loadData();

        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                badgeView.unbind();
                startactivity(FriendsRequestActivity.class);
            }
        });

        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                loadData();
                loadMsg();
            }
        });
    }

    private void loadMsg() {
        Map<String, Object> reqcontent = new LinkedHashMap<>();

        reqcontent.put("reqtimeb", "");
        reqcontent.put("reqtimee", "");
        reqcontent.put("status", "1");
        reqcontent.put("ordersort", "DESC");
        reqcontent.put("pagesize", "");
        reqcontent.put("currentpage", "");

        NetPostUtil.doAction(accessId, signKey, reqcontent, "userfriendreqQuery",
                new MyStringCallback(FriendsActivity.this) {
                    @Override
                    public void onResponse(String response, int id) {
                        super.onResponse(response, id);
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }
                        FriendRequest authorModel = new Gson().fromJson(response, FriendRequest.class);
                        if (authorModel == null) {
                            return;
                        }
                        if (authorModel.getInfo().getCode() == 100000) {
                            int count = authorModel.getContent().getPageinfo().getTotalcount();
                            if (count > 0) {
                                badgeView.setBadgeCount(count).bind(tv_title);
                            }
                        } else {
                            badgeView.unbind();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        super.onError(call, e, id);
                        refresh.finishRefresh();
                    }
                }
//        new ACallback<FriendRequest>() {
//            @Override
//            public void onSuccess(FriendRequest authorModel) {
//                refresh.finishRefresh();
//                if (authorModel == null) {
//                    return;
//                }
//                if (authorModel.getInfo().getCode() == 100000) {
//                    int count = authorModel.getContent().getPageinfo().getTotalcount();
//                    if (count > 0) {
//                        badgeView.setBadgeCount(count).bind(tv_title);
//                    }
//                } else {
//                    badgeView.unbind();
//                }
//            }
//
//            @Override
//            public void onFail(int errCode, String errMsg) {
//                refresh.finishRefresh();
//                showToast(R.string.toast_network_connection_failed);
//            }
//        }
        );
    }

    private void initFriends() {
//        ItemBind<Friend.ContentBean.FriendlistBean> itemBind = new ItemBind<Friend.ContentBean.FriendlistBean>() {
//            @Override
//            public void onBind(final ItemView itemView, final Friend.ContentBean.FriendlistBean friendlistBean, int i) {
//                if ("2".equals(friendlistBean.getSex())) {
//                    itemView.setImageResource(R.id.sex_icon, R.drawable.ic_female);
//                } else {
//                    itemView.setImageResource(R.id.sex_icon, R.drawable.ic_male);
//                }
//
//                String remark = friendlistBean.getRemark();
//                if (TextUtils.isEmpty(remark)) {
//                    itemView.setText(R.id.item_userid, friendlistBean.getUserid());
//                } else {
//                    itemView.setText(R.id.item_userid, remark + " (" + friendlistBean.getUserid() + ")");
//                }
//
//                if ("1".equals(friendlistBean.getBgflag1())) {
//                    itemView.getView(R.id.bgflag).setVisibility(View.VISIBLE);
//                    itemView.setText(R.id.authorize, getResources().getString(R.string.authorize_cancel));
//                } else {
//                    itemView.getView(R.id.bgflag).setVisibility(View.GONE);
//                    itemView.setText(R.id.authorize, getResources().getString(R.string.authorize));
//                }
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if ("1".equals(friendlistBean.getBgflag2())) {
//                            Intent intent = new Intent(FriendsActivity.this, FriendsDetailActivity.class);
//                            intent.putExtra(FRIUSERNO, friendlistBean.getFriuserno());
//                            startActivity(intent);
//                        } else {
//                            showToast(R.string.not_allowed_interview);
//                        }
//                    }
//                }).setOnClickListener(R.id.modify, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String friuserno = friendlistBean.getFriuserno();
//                        shwoModifyRemarkDialog(friuserno);
//                        itemView.closeMenu();
//                    }
//                }).setOnClickListener(R.id.authorize, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String friuserno = friendlistBean.getFriuserno();
//                        if ("1".equals(friendlistBean.getBgflag1())) {
//                            authorizeOrCancel(friuserno, "2");
//                        } else {
//                            authorizeOrCancel(friuserno, "1");
//                        }
//                        itemView.closeMenu();
//                    }
//                }).setOnClickListener(R.id.delete, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String friuserno = friendlistBean.getFriuserno();
//                        shwoDeleteFriendDialog(friuserno);
//                        itemView.closeMenu();
//                    }
//                });
//            }
//        };
//        SlideAdapter.load(mDatas)
//                .item(R.layout.item_friend, 0, 0, R.layout.menu_friend, 0.5f)
//                .padding(1)
//                .bind(itemBind)
//                .into(mRecyclerView);

        adapter = new CommonAdapter<Friend.ContentBean.FriendlistBean>(this, R.layout.item_friend_new, mDatas) {
            @Override
            public void convert(BaseViewHolder holder, int position) {
                final Friend.ContentBean.FriendlistBean friendListBean = mDatas.get(position);
                String remark = friendListBean.getRemark();
                if (TextUtils.isEmpty(remark)) {
                    holder.setText(R.id.item_userid, friendListBean.getUserid());
                } else {
                    holder.setText(R.id.item_userid, remark + " (" + friendListBean.getUserid() + ")");
                }

                final LukeSwitchButton lukeSwitchButton = holder.getItemView().findViewById(R.id.authorize_switch);
                if ("1".equals(friendListBean.getBgflag1())) {
                    lukeSwitchButton.setToggleOn();
                } else {
                    lukeSwitchButton.setToggleOff();
                }
                lukeSwitchButton.setOnToggleChanged(new LukeSwitchButton.OnToggleChanged() {
                    @Override
                    public void onToggle(boolean on) {
                        String friuserno = friendListBean.getFriuserno();
                        if (on) {
                            authorizeOrCancel(friuserno, "1", lukeSwitchButton);
                        } else {
                            authorizeOrCancel(friuserno, "2", lukeSwitchButton);
                        }
                    }
                });
            }
        };
        adapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                Friend.ContentBean.FriendlistBean friendlistBean = mDatas.get(position);
                if ("1".equals(friendlistBean.getBgflag2())) {
                    Intent intent = new Intent(FriendsActivity.this, FriendsDetailActivity.class);
                    intent.putExtra(FRIUSERNO, friendlistBean.getFriuserno());
                    startActivity(intent);
                } else {
                    showToast(R.string.not_allowed_interview);
                }
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(adapter);

        adapter.setOnItemLongClickListener(new CommonAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                showDeleteFriendDialog(mDatas.get(position));
                return false;
            }
        });
    }

    private void authorizeOrCancel(String friuserno, final String flag, final LukeSwitchButton lukeSwitchButton) {
        Map<String, Object> reqcontent = new LinkedHashMap<>();
        reqcontent.put("friuserno", friuserno);
        reqcontent.put("bgflag", flag);
        NetPostUtil.doAction(accessId, signKey, reqcontent, "userfriendBgflag",
                new MyStringCallback(FriendsActivity.this) {
                    @Override
                    public void onResponse(String response, int id) {
                        super.onResponse(response, id);
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }
                        BaseMsgEntity authorModel = new Gson().fromJson(response, BaseMsgEntity.class);
                        if (authorModel == null) {
                            return;
                        }
                        if (authorModel.getInfo().getCode() != 100000) {
                            showToast(authorModel.getInfo().getMsg());
                            if ("1".equals(flag)) {
                                lukeSwitchButton.setToggleOff();
                            } else {
                                lukeSwitchButton.setToggleOn();
                            }
                        } else {
                            loadData();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        super.onError(call, e, id);
                        if ("1".equals(flag)) {
                            lukeSwitchButton.setToggleOff();
                        } else {
                            lukeSwitchButton.setToggleOn();
                        }
                    }
                }
//        new ACallback<BaseMsgEntity>() {
//            @Override
//            public void onSuccess(BaseMsgEntity authorModel) {
//                if (authorModel == null) {
//                    return;
//                }
//                if (authorModel.getInfo().getCode() != 100000) {
//                    showToast(authorModel.getInfo().getMsg());
//                } else {
//                    loadData();
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

    private void shwoModifyRemarkDialog(final String friuserno) {
        View friendAddView = LayoutInflater.from(this).inflate(R.layout.remark_modify_content, null);
        final EditText et_remark = friendAddView.findViewById(R.id.et_remark);
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
                modifyRemark(friuserno, remark);
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

    private void modifyRemark(String friuserno, String remark) {
        Map<String, Object> reqcontent = new LinkedHashMap<>();
        reqcontent.put("friuserno", friuserno);
        reqcontent.put("remark", remark);

        NetPostUtil.doAction(accessId, signKey, reqcontent, "userfriendRemark",
                new MyStringCallback(FriendsActivity.this) {
                    @Override
                    public void onResponse(String response, int id) {
                        super.onResponse(response, id);
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }
                        BaseMsgEntity authorModel = new Gson().fromJson(response, BaseMsgEntity.class);
                        if (authorModel == null) {
                            return;
                        }
                        if (authorModel.getInfo().getCode() != 100000) {
                            showToast(authorModel.getInfo().getMsg());
                        } else {
                            loadData();
                        }
                    }
                }
//                new ACallback<BaseMsgEntity>() {
//                    @Override
//                    public void onSuccess(BaseMsgEntity authorModel) {
//                        if (authorModel == null) {
//                            return;
//                        }
//                        if (authorModel.getInfo().getCode() != 100000) {
//                            showToast(authorModel.getInfo().getMsg());
//                        } else {
//                            loadData();
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

    private void showDeleteFriendDialog(Friend.ContentBean.FriendlistBean friendlistBean) {
        final String friuserno = friendlistBean.getFriuserno();
        View friendAddView = LayoutInflater.from(this).inflate(R.layout.delete_friend_content, null);
        TextView textView_delete_name = friendAddView.findViewById(R.id.delete_name);
        textView_delete_name.setText(friendlistBean.getUserid() + "？");
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
                deleteFriend(friuserno);
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

    private void deleteFriend(String friuserno) {
        Map<String, Object> reqcontent = new LinkedHashMap<>();
        reqcontent.put("friuserno", friuserno);
        reqcontent.put("imsendflag", "1");
        NetPostUtil.doAction(accessId, signKey, reqcontent, "userfriendDelete",
                new MyStringCallback(FriendsActivity.this) {
                    @Override
                    public void onResponse(String response, int id) {
                        super.onResponse(response, id);
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }
                        BaseMsgEntity authorModel = new Gson().fromJson(response, BaseMsgEntity.class);
                        if (authorModel == null) {
                            return;
                        }
                        if (authorModel.getInfo().getCode() != 100000) {
                            showToast(authorModel.getInfo().getMsg());
                        } else {
                            loadData();
                        }
                    }
                }
//                new ACallback<BaseMsgEntity>() {
//                    @Override
//                    public void onSuccess(BaseMsgEntity authorModel) {
//                        if (authorModel == null) {
//                            return;
//                        }
//                        if (authorModel.getInfo().getCode() != 100000) {
//                            showToast(authorModel.getInfo().getMsg());
//                        } else {
//                            loadData();
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

    private void loadData() {
        Map<String, Object> reqcontent = new LinkedHashMap<>();
        reqcontent.put("userid", "");
        reqcontent.put("useridflag", "");
        reqcontent.put("bgflag1", "");
        reqcontent.put("ipflag1", "");
        reqcontent.put("bgflag2", "");
        reqcontent.put("ipflag2", "");
        reqcontent.put("remark", "");
        reqcontent.put("pagesize", "");
        reqcontent.put("currentpage", "");

        NetPostUtil.doAction(accessId, signKey, reqcontent, "userfriendQuery",
                new MyStringCallback(FriendsActivity.this) {
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
                        refresh.finishRefresh();
                        mDatas.clear();
                        if (authorModel.getInfo().getCode() == 100000) {
                            mDatas.addAll(authorModel.getContent().getFriendlist());
                        }
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        mRecyclerView.setVisibility(View.VISIBLE);
                        empty_view.setVisibility(View.GONE);
                        if (mDatas.size() == 0) {
                            mRecyclerView.setVisibility(View.GONE);
                            empty_view.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        super.onError(call, e, id);
                        refresh.finishRefresh();
                    }
                }
//                new ACallback<Friend>() {
//                    @Override
//                    public void onSuccess(Friend authorModel) {
//                        if (authorModel == null) {
//                            return;
//                        }
//                        refresh.finishRefresh();
//                        mDatas.clear();
//                        if (authorModel.getInfo().getCode() == 100000) {
//                            mDatas.addAll(authorModel.getContent().getFriendlist());
//                        }
//                        mRecyclerView.getAdapter().notifyDataSetChanged();
//                        mRecyclerView.setVisibility(View.VISIBLE);
//                        empty_view.setVisibility(View.GONE);
//                        if (mDatas.size() == 0) {
//                            mRecyclerView.setVisibility(View.GONE);
//                            empty_view.setVisibility(View.VISIBLE);
//                        }
//                    }
//
//                    @Override
//                    public void onFail(int errCode, String errMsg) {
//                        refresh.finishRefresh();
//                        showToast(R.string.toast_network_connection_failed);
//                    }
//                }
        );
    }

    @Override
    protected void onClickView(View v) {
        super.onClickView(v);
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.ibt_add:
                startactivity(FriendsSearchActivity.class);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            default:
                break;
        }
    }

}
