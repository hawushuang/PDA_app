package com.microtechmd.pda_app.activity;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.BaseMsgEntity;
import com.microtechmd.pda_app.entity.FriendRequest;
import com.microtechmd.pda_app.network.HttpHeader;
import com.microtechmd.pda_app.network.okhttp_util.OkHttpUtils;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.util.JSONUtils;
import com.microtechmd.pda_app.util.MD5Utils;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;
import com.wyh.slideAdapter.ItemBind;
import com.wyh.slideAdapter.ItemView;
import com.wyh.slideAdapter.SlideAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;

import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;


public class FriendsRequestActivity extends ActivityPDA {
    private String accessId;
    private String signKey;

    private List<FriendRequest.ContentBean.ReqlistBean> mReqList;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout refresh;
    private TextView empty_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendrequest);
        accessId = (String) SPUtils.get(this, StringConstant.ACCESSID, "");
        signKey = (String) SPUtils.get(this, StringConstant.SIGNKEY, "");

        mReqList = new ArrayList<>();

        mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        refresh = findViewById(R.id.refresh);
        empty_view = findViewById(R.id.empty_view);

        initFriendRequest();
        loadData();

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
    }


    private void initFriendRequest() {
        ItemBind<FriendRequest.ContentBean.ReqlistBean> itemBind = new ItemBind<FriendRequest.ContentBean.ReqlistBean>() {
            @Override
            public void onBind(final ItemView itemView, final FriendRequest.ContentBean.ReqlistBean reqlistBean, int i) {
                try {
                    if ("2".equals(reqlistBean.getSex())) {
                        itemView.setImageResource(R.id.sex_icon, R.drawable.ic_female);
                    } else {
                        itemView.setImageResource(R.id.sex_icon, R.drawable.ic_male);
                    }

                    itemView.setText(R.id.item_userid, reqlistBean.getUserid());

                    String verinfo = reqlistBean.getVerinfo();
                    if (!TextUtils.isEmpty(verinfo)) {
                        itemView.setText(R.id.item_verinfo, verinfo);
                    } else {
                        itemView.setText(R.id.item_verinfo, getResources().getString(R.string.add_friend));
                    }

                    String t = reqlistBean.getReqtime();
                    if (t.length() >= 14) {
                        String time = t.substring(0, 4) + "-" + t.substring(4, 6) + "-" + t.substring(6, 8) + " " +
                                t.substring(8, 10) + ":" + t.substring(10, 12);
                        itemView.setText(R.id.item_time, time);
                    }
                    switch (Integer.valueOf(reqlistBean.getStatus())) {
                        case 1:
                            itemView.setText(R.id.item_accept, getResources().getString(R.string.accept));
                            itemView.getView(R.id.item_accept).setBackgroundResource(R.drawable.button_small);
                            itemView.getView(R.id.item_accept).setEnabled(true);
                            itemView.getView(R.id.reject).setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            setRejectViewGone(itemView, getResources().getString(R.string.accepted));
                            break;
                        case 3:
                            setRejectViewGone(itemView, getResources().getString(R.string.rejected));
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    mLog.Error(getClass(), e.getMessage());
                }

                itemView.setOnClickListener(R.id.item_accept, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        doAction(reqlistBean.getRecordno(), "userfriendreqAccept");
                        itemView.closeMenu();
                    }
                }).setOnClickListener(R.id.reject, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        doAction(reqlistBean.getRecordno(), "userfriendreqReject");
                        itemView.closeMenu();
                    }
                }).setOnClickListener(R.id.delete, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<String> strings = new ArrayList<>();
                        strings.add(reqlistBean.getRecordno());
                        deleteMsg(strings);
                        itemView.closeMenu();
                    }
                });
            }
        };
        SlideAdapter.load(mReqList)
                .item(R.layout.item_friendrequest, 0, 0,
                        R.layout.menu_friendrequest, 0.3f)
                .padding(1)
                .bind(itemBind)
                .into(mRecyclerView);
    }

    private void setRejectViewGone(ItemView itemView, String string) {
        itemView.setText(R.id.item_accept,
                string);
        itemView.getView(R.id.item_accept).setBackgroundResource(R.color.transparent);
        itemView.getView(R.id.item_accept).setEnabled(false);
        itemView.getView(R.id.reject).setVisibility(View.GONE);
    }

    private void doAction(String recordno, String action) {
        try {
            Map<String, Object> reqcontent = new LinkedHashMap<>();
            String imsendflag = "1";

            reqcontent.put("recordno", recordno);
            reqcontent.put("imsendflag", imsendflag);

            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim());
            String headerData = HttpHeader.getHeaderData(accessId, action, reqMd5);
            String headerSign = HttpHeader.getHeaderSign(headerData, signKey);
            String json = JSONUtils.toJSON(reqcontent).trim();

            OkHttpUtils
                    .postString()
                    .url(HOST + APISERVICE_VERSION + "/" + Constant.APISERVICE)
                    .addHeader("x-api-data", headerData)
                    .addHeader("x-api-sign", headerSign)
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .content(json)
                    .build()
                    .execute(new MyStringCallback(FriendsRequestActivity.this) {
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
                            if (authorModel.getInfo().getCode() == 100000) {
                                loadData();
                            } else {
                                showToast(authorModel.getInfo().getMsg());
                            }
                        }
                    });

//            ViseHttp.POST(Constant.APISERVICE)
//                    .addHeader("x-api-data", headerData)
//                    .addHeader("x-api-sign", headerSign)
//                    .setJson(JSONUtils.toJSON(reqcontent).trim())
//                    .request(new ACallback<BaseMsgEntity>() {
//                        @Override
//                        public void onSuccess(BaseMsgEntity authorModel) {
//                            if (authorModel == null) {
//                                return;
//                            }
//                            if (authorModel.getInfo().getCode() == 100000) {
//                                loadData();
//                            } else {
//                                showToast(authorModel.getInfo().getMsg());
//                            }
//                        }
//
//                        @Override
//                        public void onFail(int errCode, String errMsg) {
//                            showToast(R.string.toast_network_connection_failed);
//                        }
//                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteMsg(List<String> recordnos) {
        try {
            if (recordnos.size() == 0) {
                return;
            }
            Map<String, Object> reqcontent = new LinkedHashMap<>();
            List<Map<String, Object>> reqlist = new ArrayList<>();

            for (String recordno : recordnos) {
                Map<String, Object> reqinfo = new HashMap<>();
                reqinfo.put("recordno", recordno);
                reqlist.add(reqinfo);
            }
            reqcontent.put("reqlist", reqlist);

            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim());
            String headerData = HttpHeader.getHeaderData(accessId, "userfriendreqDelete", reqMd5);
            String headerSign = HttpHeader.getHeaderSign(headerData, signKey);
            String json = JSONUtils.toJSON(reqcontent).trim();

            OkHttpUtils
                    .postString()
                    .url(HOST + APISERVICE_VERSION + "/" + Constant.APISERVICE)
                    .addHeader("x-api-data", headerData)
                    .addHeader("x-api-sign", headerSign)
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .content(json)
                    .build()
                    .execute(new MyStringCallback(FriendsRequestActivity.this) {
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
                            if (authorModel.getInfo().getCode() == 100000) {
                                loadData();
                            } else {
                                showToast(authorModel.getInfo().getMsg());
                            }
                        }
                    });
//            ViseHttp.POST(Constant.APISERVICE)
//                    .addHeader("x-api-data", headerData)
//                    .addHeader("x-api-sign", headerSign)
//                    .setJson(JSONUtils.toJSON(reqcontent).trim())
//                    .request(new ACallback<BaseMsgEntity>() {
//                        @Override
//                        public void onSuccess(BaseMsgEntity authorModel) {
//                            if (authorModel == null) {
//                                return;
//                            }
//                            if (authorModel.getInfo().getCode() == 100000) {
//                                loadData();
//                            } else {
//                                showToast(authorModel.getInfo().getMsg());
//                            }
//                        }
//
//                        @Override
//                        public void onFail(int errCode, String errMsg) {
//                            showToast(R.string.toast_network_connection_failed);
//                        }
//                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            Map<String, Object> reqcontent = new LinkedHashMap<>();
            String reqtimeb = "";
            String reqtimee = "";
            String status = "";
            String ordersort = "DESC";
            String pagesize = "";
            String currentpage = "";

            reqcontent.put("reqtimeb", reqtimeb);
            reqcontent.put("reqtimee", reqtimee);
            reqcontent.put("status", status);
            reqcontent.put("ordersort", ordersort);
            reqcontent.put("pagesize", pagesize);
            reqcontent.put("currentpage", currentpage);

            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim());
            String headerData = HttpHeader.getHeaderData(accessId, "userfriendreqQuery", reqMd5);
            String headerSign = HttpHeader.getHeaderSign(headerData, signKey);
            String json = JSONUtils.toJSON(reqcontent).trim();

            OkHttpUtils
                    .postString()
                    .url(HOST + APISERVICE_VERSION + "/" + Constant.APISERVICE)
                    .addHeader("x-api-data", headerData)
                    .addHeader("x-api-sign", headerSign)
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .content(json)
                    .build()
                    .execute(new MyStringCallback(FriendsRequestActivity.this) {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            super.onError(call, e, id);
                            refresh.setRefreshing(false);
                        }

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
                            refresh.setRefreshing(false);
                            mReqList.clear();
                            if (authorModel.getInfo().getCode() == 100000) {
                                mReqList.addAll(authorModel.getContent().getReqlist());
                            }
                            mRecyclerView.getAdapter().notifyDataSetChanged();
                            mRecyclerView.setVisibility(View.VISIBLE);
                            empty_view.setVisibility(View.GONE);
                            if (mReqList.size() == 0) {
                                mRecyclerView.setVisibility(View.GONE);
                                empty_view.setVisibility(View.VISIBLE);
                            }
                        }
                    });
//            ViseHttp.POST(Constant.APISERVICE)
//                    .addHeader("x-api-data", headerData)
//                    .addHeader("x-api-sign", headerSign)
//                    .setJson(JSONUtils.toJSON(reqcontent).trim())
//                    .request(new ACallback<FriendRequest>() {
//                        @Override
//                        public void onSuccess(FriendRequest authorModel) {
//                            if (authorModel == null) {
//                                return;
//                            }
//                            refresh.setRefreshing(false);
//                            mReqList.clear();
//                            if (authorModel.getInfo().getCode() == 100000) {
//                                mReqList.addAll(authorModel.getContent().getReqlist());
//                            }
//                            mRecyclerView.getAdapter().notifyDataSetChanged();
//                            mRecyclerView.setVisibility(View.VISIBLE);
//                            empty_view.setVisibility(View.GONE);
//                            if (mReqList.size() == 0) {
//                                mRecyclerView.setVisibility(View.GONE);
//                                empty_view.setVisibility(View.VISIBLE);
//                            }
//                        }
//
//                        @Override
//                        public void onFail(int errCode, String errMsg) {
//                            refresh.setRefreshing(false);
//                            showToast(R.string.toast_network_connection_failed);
//                        }
//                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onClickView(View v) {
        super.onClickView(v);
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_clear:
                List<String> records = new ArrayList<>();
                for (FriendRequest.ContentBean.ReqlistBean reqlistBean : mReqList) {
                    records.add(reqlistBean.getRecordno());
                }
                deleteMsg(records);
                break;
            default:
                break;
        }
    }

}
