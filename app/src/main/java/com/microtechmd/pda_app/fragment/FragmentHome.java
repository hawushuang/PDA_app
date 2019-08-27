package com.microtechmd.pda_app.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterMonitor;
import com.microtechmd.pda.library.entity.monitor.DateTime;
import com.microtechmd.pda.library.entity.monitor.History;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.ApplicationPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.activity.EMedicineActivity;
import com.microtechmd.pda_app.activity.EatingActivity;
import com.microtechmd.pda_app.activity.ExerciseActivity;
import com.microtechmd.pda_app.activity.ExerciseStateActivity;
import com.microtechmd.pda_app.activity.MainActivity;
import com.microtechmd.pda_app.activity.SettingActivity;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.DbHistory;
import com.microtechmd.pda_app.util.FileUtil;
import com.microtechmd.pda_app.util.LimitQueue;
import com.microtechmd.pda_app.widget.MainActionBar;
import com.microtechmd.pda_app.widget.bottomdialog.BottomDialog;
import com.microtechmd.pda_app.widget.bottomdialog.BottomDialogNew;
import com.microtechmd.pda_app.widget.bottomdialog.Item;
import com.microtechmd.pda_app.widget.bottomdialog.OnItemClickListener;
import com.microtechmd.pda_app.widget.countdownview.CountdownView;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import io.objectbox.Box;

import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE_INVALID;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE_RECOMMEND_CAL;
import static com.microtechmd.pda_app.ActivityPDA.HIGHGLUCOSE_SAVED;
import static com.microtechmd.pda_app.ActivityPDA.HIGH_DEFAULT;
import static com.microtechmd.pda_app.ActivityPDA.HYPER;
import static com.microtechmd.pda_app.ActivityPDA.HYPO;
import static com.microtechmd.pda_app.ActivityPDA.IMPENDANCE;
import static com.microtechmd.pda_app.ActivityPDA.LOWGLUCOSE_SAVED;
import static com.microtechmd.pda_app.ActivityPDA.LOW_DEFAULT;
import static com.microtechmd.pda_app.ActivityPDA.RFSIGNAL;
import static com.microtechmd.pda_app.ActivityPDA.SENSOR_ERROR;
import static com.microtechmd.pda_app.ActivityPDA.SENSOR_EXPIRATION;
import static com.microtechmd.pda_app.fragment.FragmentSettings.REALTIMEFLAG;


public class FragmentHome extends FragmentBase
        implements EntityMessage.Listener {
    private static final String STRING_UNKNOWN = "-.-";
    private static final String TAG_GRAPH = "graph";
    public static final int TIME_DATA_6 = 6;
    public static final int TIME_DATA_12 = 12;
    public static final int TIME_DATA_24 = 24;

    public static final int NORMAL = 0;
    public static final int LOW = 1;
    public static final int HIGH = 2;
    private static final int GLUCOSE_DISPLAY_TIMEOUT = 15 * 60 * 1000;

    public static final int GLUCOSE_PER_TIME = 5;
    private static final int FAST_DOWN = -20;
    private static final int SLOW_DOWN = -10;
    private static final int SLOW_UP = 10;
    private static final int FAST_UP = 20;

    private ApplicationPDA app;
    private String userName;
    private View mRootView = null;
//    private FragmentGraph mFragmentGraph = null;

    private FragmentHelloChartsGraph fragmentNewGraph = null;
    private TextView textView_t;
    private TextView textView_g;
    private RelativeLayout rl_bg;
    private ImageView iv_glucose_bg;
    //    private ImageView iv_glucose_state;
    private TextView tv_glucose_state;
    private TextView tv_synchronize;
    private ImageButton iv_synchronize;
    private ImageView textView_g_err;
    private TextView textview_unit;
    private TextView text_view_countdown;
    private CountdownView countdownView;
    private RadioGroup radio_group;

    private LimitQueue<Integer> list_trends;
    private CountDownTimer countDownTimer;
//    private PendingIntent sender;
//    private AlarmManager manager;

//    private Box<DbHistory> dbHistoryBoxBox;


    private History mHistory;

    private int glucoseState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_home, container, false);
        app = (ApplicationPDA) getActivity().getApplication();
        userName = (String) SPUtils.get(getActivity(), StringConstant.USERNAME, "");
//        if (app != null) {
//            dbHistoryBoxBox = app.getBoxStore().boxFor(DbHistory.class);
//        }
        initActionBar();

        list_trends = new LimitQueue<>(2);

        textView_g = (TextView) mRootView.findViewById(R.id.text_view_glucose);
        rl_bg = mRootView.findViewById(R.id.rl_bg);
        iv_glucose_bg = mRootView.findViewById(R.id.iv_glucose_bg);
//        iv_glucose_state = mRootView.findViewById(R.id.iv_glucose_state);
        tv_glucose_state = mRootView.findViewById(R.id.tv_glucose_state);
        tv_synchronize = mRootView.findViewById(R.id.tv_synchronize);
        iv_synchronize = mRootView.findViewById(R.id.iv_synchronize);

        boolean realtimeFlag = (boolean) SPUtils.get(getActivity(), REALTIMEFLAG, true);
        if (realtimeFlag) {
            tv_synchronize.setVisibility(View.GONE);
            iv_synchronize.setVisibility(View.GONE);
        } else {
            tv_synchronize.setVisibility(View.VISIBLE);
            iv_synchronize.setVisibility(View.VISIBLE);
        }
        iv_synchronize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rfsignal = (int) SPUtils.get(getActivity(), RFSIGNAL, 0);
                if (rfsignal == 0) {
                    Toast.makeText(getActivity(),
                            getActivity().getResources().getString(R.string.connect_fail),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    synchronize_data();
                }
            }
        });

        textView_g_err = (ImageView) mRootView.findViewById(R.id.glucose_err);
        textView_t = (TextView) mRootView.findViewById(R.id.text_view_date_time);
        textview_unit = (TextView) mRootView.findViewById(R.id.text_view_unit);
        text_view_countdown = (TextView) mRootView.findViewById(R.id.text_view_countdown);
        countdownView = (CountdownView) mRootView.findViewById(R.id.countdown_view);

        if (fragmentNewGraph == null) {
            fragmentNewGraph = new FragmentHelloChartsGraph();
        }
        getChildFragmentManager().beginTransaction()
                .add(R.id.layout_graph, fragmentNewGraph, TAG_GRAPH).show(fragmentNewGraph).commit();
//        updateStatus(MainActivity.getStatus());
        radio_group = mRootView.findViewById(R.id.radio_group);
        radio_group.check(R.id.time_6);
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.time_6:
                        fragmentNewGraph.setTimeData(TIME_DATA_6);
                        break;
                    case R.id.time_12:
                        fragmentNewGraph.setTimeData(TIME_DATA_12);
                        break;
                    case R.id.time_24:
                        fragmentNewGraph.setTimeData(TIME_DATA_24);
                        break;

                    default:

                        break;
                }
            }
        });
        setCountDownVisible(false);
        countdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                setCountDownVisible(false);
            }
        });

        textView_g.setText(STRING_UNKNOWN);
        return mRootView;
    }

    private void setGlucoseLevel(int i) {
        rl_bg.setPivotX(rl_bg.getWidth() / 2);
        rl_bg.setPivotY(rl_bg.getHeight() / 2);//支点在图片中心显示广播包血糖值
        switch (i) {
            case 0:
//                iv_glucose_state.setImageResource(R.drawable.glucose_super_up);
                tv_glucose_state.setText("急速上升");
                rl_bg.setRotation(0);
                switch (glucoseState) {
                    case HIGH:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_high_up);
                        break;
                    case LOW:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_low_up);
                        break;
                    case NORMAL:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_normal_up);
                        break;
                    default:
                        break;
                }
                break;
            case 45:
//                iv_glucose_state.setImageResource(R.drawable.glucose_up);
                tv_glucose_state.setText("缓慢上升");
                rl_bg.setRotation(0);
                switch (glucoseState) {
                    case HIGH:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_high);
                        break;
                    case LOW:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_low);
                        break;
                    case NORMAL:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_normal);
                        break;
                    default:
                        break;
                }

                break;
            case 90:
//                iv_glucose_state.setImageResource(R.drawable.glucose_up);
                tv_glucose_state.setText("血糖平稳");
                rl_bg.setRotation(90);
                switch (glucoseState) {
                    case HIGH:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_high_up);
                        break;
                    case LOW:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_low_up);
                        break;
                    case NORMAL:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_normal_up);
                        break;
                    default:
                        break;
                }
                break;
            case 135:
//                iv_glucose_state.setImageResource(R.drawable.glucose_up);
                tv_glucose_state.setText("缓慢下降");
                rl_bg.setRotation(180);
                switch (glucoseState) {
                    case HIGH:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_high);
                        break;
                    case LOW:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_low);
                        break;
                    case NORMAL:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_normal);
                        break;
                    default:
                        break;
                }
                break;
            case 180:
//                iv_glucose_state.setImageResource(R.drawable.glucose_super_up);
                tv_glucose_state.setText("急速下降");
                rl_bg.setRotation(180);
                switch (glucoseState) {
                    case HIGH:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_high_up);
                        break;
                    case LOW:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_low_up);
                        break;
                    case NORMAL:
                        iv_glucose_bg.setImageResource(R.drawable.glucose_normal_up);
                        break;
                    default:
                        break;
                }
                break;
            default:

                break;
        }
    }

    private void initActionBar() {
        MainActionBar mainActionBar = mRootView.findViewById(R.id.toolbar);
        if (!TextUtils.isEmpty(userName)) {
            mainActionBar.setTitleText(userName);
        } else {
            mainActionBar.setTitleText(R.string.unlogin);
        }
        mainActionBar.setRightButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });
        FrameLayout edit_add = mRootView.findViewById(R.id.edit_add);
        edit_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<Item> itemList = new ArrayList<>();
                Drawable drawable1 = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.carb);
                Drawable drawable2 = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.medicine);
                Drawable drawable3 = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.insulin);
                Drawable drawable4 = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.exercise);
                Drawable drawable5 = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.health);
                Item item = new Item(1, "碳水化合物", drawable1);
                itemList.add(item);
                item = new Item(2, "用药", drawable2);
                itemList.add(item);
                item = new Item(3, "胰岛素注射", drawable3);
                itemList.add(item);
                item = new Item(4, "运动", drawable4);
                itemList.add(item);
                item = new Item(5, "健康状态", drawable5);
                itemList.add(item);
                new BottomDialogNew(getActivity())
                        .layout(BottomDialog.GRID)
                        .orientation(BottomDialog.VERTICAL)
                        .addItems(itemList, new OnItemClickListener() {
                            @Override
                            public void click(Item item) {
                                switch (item.getId()) {
                                    case 1:
                                        Intent intent1 = new Intent(getActivity(), EatingActivity.class);
                                        startActivity(intent1);
                                        break;
                                    case 2:
                                        Intent intent2 = new Intent(getActivity(), EMedicineActivity.class);
                                        startActivity(intent2);
                                        break;
                                    case 3:

                                        break;
                                    case 4:
                                        Intent intent4 = new Intent(getActivity(), ExerciseActivity.class);
                                        startActivity(intent4);
                                        break;
                                    case 5:
                                        Intent intent5 = new Intent(getActivity(), ExerciseStateActivity.class);
                                        startActivity(intent5);

                                        break;
                                    default:

                                        break;
                                }
                            }
                        })
                        .show();
            }
        });
        mainActionBar.setLeftButtonVisible(true);
        mainActionBar.setLeftButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<Item> itemList = new ArrayList<>();
                Drawable drawable1 = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.umeng_socialize_qq);
                Drawable drawable2 = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.umeng_socialize_qzone);
                Drawable drawable3 = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.umeng_socialize_wechat);
                Drawable drawable4 = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.umeng_socialize_wxcircle);
                Item item = new Item(1, "QQ", drawable1);
                itemList.add(item);
                item = new Item(2, "QQ空间", drawable2);
                itemList.add(item);
                item = new Item(3, "微信", drawable3);
                itemList.add(item);
                item = new Item(4, "微信朋友圈", drawable4);
                itemList.add(item);
                new BottomDialog(getActivity())
                        .title("分享到")
                        .layout(BottomDialog.GRID)
                        .orientation(BottomDialog.VERTICAL)
                        .size(4)
                        .addItems(itemList, new OnItemClickListener() {
                            @Override
                            public void click(Item item) {
                                switch (item.getId()) {
                                    case 1:
                                        checkStoragePermission(QQ.NAME);
                                        break;
                                    case 2:
                                        checkStoragePermission(QZone.NAME);
                                        break;
                                    case 3:
                                        checkStoragePermission(Wechat.NAME);
                                        break;
                                    case 4:
                                        checkStoragePermission(WechatMoments.NAME);
                                        break;
                                    default:

                                        break;
                                }
                            }
                        })
                        .show();
            }
        });
    }


    public Bitmap getFragmentBitmap(Fragment fragment) {
        View v = fragment.getView();
        assert v != null;
        Bitmap screenshot;
        screenshot = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(screenshot);
        c.translate(-v.getScrollX(), -v.getScrollY());
        v.draw(c);
        return screenshot;
    }

    private void checkStoragePermission(final String platformName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                PermissionManager.instance().with(getActivity()).request(new OnPermissionCallback() {
                    @Override
                    public void onRequestAllow(String permissionName) {
                        share(platformName);
                    }

                    @Override
                    public void onRequestRefuse(String permissionName) {
                        Toast.makeText(app, "分享失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestNoAsk(String permissionName) {
                        Toast.makeText(app, "分享失败", Toast.LENGTH_SHORT).show();
                    }
                }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                share(platformName);
            }
        }
    }

    private void share(String platformName) {

        Bitmap bitmap = getFragmentBitmap(this);
        if (bitmap != null) {
            FileUtil.saveBitmapToFile("test.png", bitmap);
        }

        Platform platform = ShareSDK.getPlatform(platformName);
        Platform.ShareParams shareParams = new Platform.ShareParams();
        if (platformName.equals(QQ.NAME)) {

        } else if (platformName.equals(QZone.NAME)) {
            shareParams.setText("分享测试");
        }

        shareParams.setImagePath(Environment.getExternalStorageDirectory().getPath() + "/test.png");
//        shareParams.setImageUrl();
//        shareParams.setImageData(bitmap);
//        shareParams.setImageArray();
        shareParams.setShareType(Platform.SHARE_IMAGE);
        platform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Toast.makeText(app, "分享完成", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Toast.makeText(app, "分享失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Toast.makeText(app, "取消分享", Toast.LENGTH_SHORT).show();
            }
        });
        platform.share(shareParams);
        Toast.makeText(app, platformName + "正在启动", Toast.LENGTH_SHORT).show();
    }

//    private void addHistory() {
//        List<DbHistory> dbHistoryList = new ArrayList<>();
//        DbHistory dbHistory;
//        for (int i = 0; i < 5; i++) {
//            dbHistory = new DbHistory();
//            dbHistory.setRf_address("TB0008");
//            dbHistory.setDate_time(201804200000L + i * 120);
//            dbHistory.setEvent_index(i + 1);
//            dbHistory.setEvent_type(7);
//            dbHistory.setValue(100);
//            dbHistoryList.add(dbHistory);
//        }
//        dbHistoryBoxBox.put(dbHistoryList);
//    }

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
    public void onResume() {
        super.onResume();
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
                handleNotification(message);
                break;

            case EntityMessage.OPERATION_ACKNOWLEDGE:
                break;

            default:
                break;
        }
    }

    private void handleSet(EntityMessage message) {
        if (message.getParameter() == ParameterComm.BROADCAST_SAVA) {
            if (message.getData()[0] == 0) {
                tv_synchronize.setVisibility(View.VISIBLE);
                iv_synchronize.setVisibility(View.VISIBLE);
            } else {
                tv_synchronize.setVisibility(View.GONE);
                iv_synchronize.setVisibility(View.GONE);
            }
        }

        if (message.getParameter() == ParameterMonitor.COUNTDOWNVIEW_VISIBLE) {
            if (message.getData() != null) {
                int value = message.getData()[0];
                setCountdownViewVisible(value);
            }
        }
        if (message.getParameter() == ParameterMonitor.UNPAIR_CLEAN) {
            setGlucoseLevel(90);
            mHistory = null;
            textView_t.setText("");
            textView_g.setText(STRING_UNKNOWN);
        }
        if (message.getParameter() == ParameterMonitor.GLUCOSE_DISPLAY) {
            if (mHistory != null) {
                long space = System.currentTimeMillis() - mHistory.getDateTime().getCalendar().getTimeInMillis();
                int minute = (int) (space / 60000);
                if (minute >= 15) {
                    textView_t.setText("");
                    textView_g.setText(STRING_UNKNOWN);
                } else {
                    textView_t.setText(String.valueOf(minute));
                }
            }
        }
        if (message.getParameter() == ParameterComm.RESET_DATA) {
            if (message.getData()[0] == 1) {
                radio_group.check(R.id.time_12);
                radio_group.check(R.id.time_6);
            }
        }
    }

    public void setCountdownViewVisible(int value) {
        if (value == 0) {
            setCountDownVisible(false);
        } else {
            if (value < 0) {
                return;
            }
            setCountDownVisible(true);
            if (((value * 60 - countdownView.getRemainTime() / 1000) < -30) ||
                    ((value * 60 - countdownView.getRemainTime() / 1000) > 90)) {
                countdownView.start((value * 60) * 1000);
                countdownView.restart();
            }
        }
    }

    private void handleNotification(final EntityMessage message) {
        if (message.getSourcePort() == ParameterGlobal.PORT_MONITOR) {
            if (message.getParameter() == ParameterMonitor.PARAM_STATUS) {
                if (message.getData() != null) {
                    History history = new History(message.getData());
                    updateStatus(history);
                    MainActivity.setStatus(history);
                }

            }
        }
    }


    @SuppressLint("SetTextI18n")
    private void updateStatus(History history) {
        mLog.Error(getClass(), "显示广播包血糖值");
        if (history != null) {
            DateTime nowTime = new DateTime(Calendar.getInstance());
            DateTime statusTime = history.getDateTime();
            long time_space = nowTime.getCalendar().getTimeInMillis() - statusTime.getCalendar().getTimeInMillis();
            boolean a = (history.getEvent().getEventFlag() & 0x40) == 0;
            mLog.Error(getClass(), "血糖：" + (history.getStatus().getShortValue1() & 0xFFFF) + "valueflag：" + a);
            if (time_space / 1000 >= -60) {
                if (Math.abs(time_space) < 15 * 60 * 1000) {
                    mHistory = history;
                    switch (history.getEvent().getEvent()) {
                        case SENSOR_EXPIRATION:
                            textView_g_err.setBackgroundResource(R.drawable.expirtion_err);
                            setGlucoseVisible(false);
                            return;
                        case SENSOR_ERROR:
                            break;
                        case GLUCOSE:
                        case GLUCOSE_RECOMMEND_CAL:
                        case GLUCOSE_INVALID:
                        case HYPO:
                        case HYPER:
                        case IMPENDANCE:
                            if ((history.getEvent().getEventFlag() & 0x40) == 0) {
                                setGlucoseVisible(true);
                                int value = history.getStatus().getShortValue1() & 0xFFFF;
                                switch (value) {
                                    case 0:
                                        value = 20;
                                        break;
                                    case 255:
                                        value = 250;
                                        break;
                                    default:
                                        break;
                                }
                                textView_g.setText(((ActivityPDA) Objects.requireNonNull(getActivity()))
                                        .getGlucoseValue(value *
                                                ActivityPDA.GLUCOSE_UNIT_MG_STEP, false).trim());

                                //判断血糖状态
                                int high = (int) SPUtils.get(Objects.requireNonNull(getActivity()), HIGHGLUCOSE_SAVED, HIGH_DEFAULT);
                                int low = (int) SPUtils.get(getActivity(), LOWGLUCOSE_SAVED, LOW_DEFAULT);
                                if (value >= high) {
                                    glucoseState = HIGH;
                                } else if (value <= low) {
                                    glucoseState = LOW;
                                } else {
                                    glucoseState = NORMAL;
                                }

                                //判断血糖趋势
                                if (list_trends.size() == 2) {
                                    int valueTrend = (value - list_trends.getLast()) / 5 +
                                            (value - list_trends.getFirst()) * 2 / 5;
                                    if (valueTrend <= FAST_DOWN) {
                                        setGlucoseLevel(180);
                                    } else if (valueTrend < SLOW_DOWN) {
                                        setGlucoseLevel(135);
                                    } else if (valueTrend <= SLOW_UP) {
                                        setGlucoseLevel(90);
                                    } else if (valueTrend < FAST_UP) {
                                        setGlucoseLevel(45);
                                    } else {
                                        setGlucoseLevel(0);
                                    }
                                }
                                list_trends.offer(value);


                                //判断血糖时间
                                long space = System.currentTimeMillis() - history.getDateTime().getCalendar().getTimeInMillis();
                                int minute = (int) (space / 60000);
                                textView_t.setText(String.valueOf(minute));

//                                if (sender != null) {
//                                    manager.cancel(sender);
//                                    sender = null;
//                                }
//                                Intent intent = new Intent();
//                                // 设置Intent action属性
//                                intent.setAction("glucose_display");
//                                // 实例化PendingIntent
//                                if (sender == null) {
//                                    sender = PendingIntent.getBroadcast(getActivity(), 100,
//                                            intent, 0);
//                                }
//                                manager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
//                                manager.set(AlarmManager.RTC_WAKEUP,
//                                        System.currentTimeMillis() + GLUCOSE_DISPLAY_TIMEOUT - time_space, sender);
//                                if (countDownTimer != null) {
//                                    countDownTimer.cancel();
//                                    countDownTimer = null;
//                                }
//                                countDownTimer = new CountDownTimer(15 * 60 * 1000 - time_space,
//                                        15 * 60 * 1000 - time_space) {
//                                    @Override
//                                    public void onTick(long l) {
//
//                                    }
//
//                                    @Override
//                                    public void onFinish() {
//                                        if (!getActivity().isFinishing()) {
//                                            textView_g.setText(STRING_UNKNOWN);
////                                        textView_t.setText("");
////                                        setGlucoseVisible(true);
//                                        }
//                                    }
//                                }.start();
//                                handler.postDelayed(runnable, 15 * 60 * 1000 - time_space);
                            } else {
//                            textView_g.setText(STRING_UNKNOWN);
//                            textView_t.setText("");
                            }
                            break;
                        default:

                            break;
                    }
                    if ((history.getEvent().getEventFlag() & 0x20) != 0) {
                        if (textView_g_err.getVisibility() == View.GONE) {
                            long space = System.currentTimeMillis() - history.getDateTime().getCalendar().getTimeInMillis();
                            int minute = (int) (space / 60000);
                            textView_t.setText(String.valueOf(minute));
                        }
                        textView_g_err.setBackgroundResource(R.drawable.glucose_err);
                        setGlucoseVisible(false);
                    } else {
                        setGlucoseVisible(true);
                    }
                }
            }
        }
    }

    private void setCountDownVisible(boolean countDownVisible) {
        if (countDownVisible) {
            textView_g.setVisibility(View.GONE);
//            textView_t.setVisibility(View.GONE);
            textView_g_err.setVisibility(View.GONE);
            textview_unit.setVisibility(View.GONE);
            rl_bg.setVisibility(View.GONE);
            tv_glucose_state.setVisibility(View.GONE);

            text_view_countdown.setVisibility(View.VISIBLE);
            countdownView.setVisibility(View.VISIBLE);
        } else {
            textView_g.setVisibility(View.VISIBLE);
//            textView_t.setVisibility(View.VISIBLE);
            textView_g_err.setVisibility(View.GONE);
            textview_unit.setVisibility(View.VISIBLE);
            rl_bg.setVisibility(View.VISIBLE);
            tv_glucose_state.setVisibility(View.VISIBLE);

            text_view_countdown.setVisibility(View.GONE);
            countdownView.setVisibility(View.GONE);
        }
    }

    public void setGlucoseVisible(boolean glucoseVisible) {
        if (glucoseVisible) {
            textView_g.setVisibility(View.VISIBLE);
            textView_g_err.setVisibility(View.GONE);
        } else {
            textView_g.setVisibility(View.GONE);
            textView_g_err.setVisibility(View.VISIBLE);
        }
    }

    private FragmentDialog showDialogConfirm(String title,
                                             String buttonTextPositive, String buttonTextNegative, Fragment content,
                                             boolean isCancelable, FragmentDialog.ListenerDialog listener) {
        FragmentDialog fragmentDialog = new FragmentDialog();
        fragmentDialog.setTitle(title);
        fragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_POSITIVE,
                buttonTextPositive);
        fragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_NEGATIVE,
                buttonTextNegative);
        fragmentDialog.setContent(content);
        fragmentDialog.setCancelable(isCancelable);
        fragmentDialog.setListener(listener);
        fragmentDialog.show(getChildFragmentManager(), null);
        return fragmentDialog;
    }

//    public void setMargins(View v, int l, int t, int r, int b) {
//        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
//            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
//            p.setMargins(l, t, r, b);
//            v.requestLayout();
//        }
//    }

    private void synchronize_data() {
        if (getActivity() != null) {
            ((ActivityPDA) getActivity()).handleMessage(
                    new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                            ParameterGlobal.PORT_MONITOR,
                            ParameterGlobal.PORT_MONITOR,
                            EntityMessage.OPERATION_SET,
                            ParameterComm.SYNCHRONIZE_DATA,
                            null));
        }
    }
}
