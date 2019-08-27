package com.microtechmd.pda_app.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterMonitor;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda_app.ApplicationPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.activity.SettingActivity;
import com.microtechmd.pda_app.adapter.TimelineAdapter;
import com.microtechmd.pda_app.entity.DbHistory;
import com.microtechmd.pda_app.entity.DbHistory_;
import com.microtechmd.pda_app.widget.MainActionBar;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.query.QueryBuilder;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.microtechmd.pda_app.ActivityPDA.BLOOD_GLUCOSE;
import static com.microtechmd.pda_app.ActivityPDA.CALIBRATION;
import static com.microtechmd.pda_app.ActivityPDA.HYPER;
import static com.microtechmd.pda_app.ActivityPDA.HYPO;
import static com.microtechmd.pda_app.ActivityPDA.SENSOR_ERROR;
import static com.microtechmd.pda_app.ActivityPDA.SENSOR_EXPIRATION;


public class FragmentHistory extends FragmentBase implements EntityMessage.Listener {
    private ApplicationPDA applicationPDA;
    private List<DbHistory> mDatas;
    private TimelineAdapter mAdapter;
    private View mRootView;
    private StickyListHeadersListView stickyList;
    private SmartRefreshLayout refresh;
    private TextView empty_view;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
//                if (mDatas.size() == 0) {
//                    for (int i = 0; i < 50; i++) {
//                        DbHistory dbHistory = new DbHistory();
//                        dbHistory.setDate_time(20180822010000L + i * 1000 * 200);
//                        dbHistory.setEvent_type(5 + i % 2);
//                        mDatas.add(dbHistory);
//                    }
//                }
                mAdapter.setmDatas(mDatas);
                mAdapter.notifyDataSetChanged();
                stickyList.setVisibility(View.VISIBLE);
                empty_view.setVisibility(View.GONE);
                if (mDatas.size() == 0) {
                    stickyList.setVisibility(View.GONE);
                    empty_view.setVisibility(View.VISIBLE);
                }
                refresh.finishRefresh();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_history, container, false);
        applicationPDA = (ApplicationPDA) getActivity().getApplication();
        initActionBar();
        stickyList = mRootView.findViewById(R.id.list);
        refresh = mRootView.findViewById(R.id.refresh);
        //设置 Header
        refresh.setRefreshHeader(new ClassicsHeader(getActivity()));
        refresh.setHeaderHeight(60);
        empty_view = mRootView.findViewById(R.id.empty_view);

        initHistoryLog();
        loadData();

        if (mDatas.size() == 0) {
            stickyList.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);
        } else {
            stickyList.setVisibility(View.VISIBLE);
            empty_view.setVisibility(View.GONE);
        }
        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                loadData();
            }
        });
        return mRootView;
    }

    private void initActionBar() {
        MainActionBar mainActionBar = mRootView.findViewById(R.id.toolbar);
        mainActionBar.setTitleText(R.string.activity_main_tab_history);
        mainActionBar.setRightButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });
    }

    private void initHistoryLog() {
        mDatas = new ArrayList<>();
        mAdapter = new TimelineAdapter(getActivity(), mDatas);
        stickyList.setOnStickyHeaderChangedListener(new StickyListHeadersListView.OnStickyHeaderChangedListener() {
            @Override
            public void onStickyHeaderChanged(StickyListHeadersListView l, View header, int itemPosition, long headerId) {
                header.setAlpha(1);
            }
        });
        stickyList.setDrawingListUnderStickyHeader(true);
        stickyList.setAreHeadersSticky(true);
        stickyList.setAdapter(mAdapter);
        stickyList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                Toast.makeText(applicationPDA, getEventContent(mDatas.get(i).getEvent_type()), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                QueryBuilder<DbHistory> builder = applicationPDA.getBoxStore().boxFor(DbHistory.class).query();
                builder.equal(DbHistory_.event_type, SENSOR_ERROR)
                        .or()
                        .equal(DbHistory_.event_type, SENSOR_EXPIRATION)
                        .or()
                        .equal(DbHistory_.event_type, HYPO)
                        .or()
                        .equal(DbHistory_.event_type, HYPER)
                        .or()
                        .equal(DbHistory_.event_type, BLOOD_GLUCOSE)
                        .or()
                        .equal(DbHistory_.event_type, CALIBRATION)
                        .orderDesc(DbHistory_.date_time);
                List<DbHistory> dbList = builder.build().find();
                mDatas.clear();
                mDatas.addAll(dbList);
                handler.sendEmptyMessage(1);
            }
        }).start();
    }

    private String getEventContent(int event) {
        String content = "";

        switch (event) {
            case SENSOR_ERROR:
                content = getString(R.string.alarm_sensor_error);
                break;

            case SENSOR_EXPIRATION:
                content = getString(R.string.alarm_expiration);
                break;

            case HYPO:
                content = getString(R.string.alarm_hypo);
                break;

            case HYPER:
                content = getString(R.string.alarm_hyper);
                break;

            case BLOOD_GLUCOSE:
                content = getString(R.string.blood_glucose);
                break;

            case CALIBRATION:
                content = getString(R.string.calibrate_blood);
                break;
            default:
                break;
        }
        return content;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((ApplicationPDA) getActivity().getApplication())
                .registerMessageListener(ParameterGlobal.PORT_MONITOR, this);
    }


    @Override
    public void onDestroyView() {
        ((ApplicationPDA) getActivity().getApplication())
                .unregisterMessageListener(ParameterGlobal.PORT_MONITOR, this);
        super.onDestroyView();
    }

    @Override
    public void onReceive(EntityMessage message) {
        switch (message.getOperation()) {
            case EntityMessage.OPERATION_SET:
                handleSet(message);
                break;

            case EntityMessage.OPERATION_GET:
                break;

            case EntityMessage.OPERATION_EVENT:
                break;

            case EntityMessage.OPERATION_NOTIFY:
                break;

            case EntityMessage.OPERATION_ACKNOWLEDGE:
                break;

            default:
                break;
        }
    }

    private void handleSet(EntityMessage message) {
        if (message.getParameter() == ParameterMonitor.TIPSREFRESH) {
            loadData();
        }
    }
}