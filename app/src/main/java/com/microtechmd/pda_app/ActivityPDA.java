package com.microtechmd.pda_app;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.microtechmd.pda.library.entity.DataList;
import com.microtechmd.pda.library.entity.DataStorage;
import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterGlucose;
import com.microtechmd.pda.library.entity.ParameterMonitor;
import com.microtechmd.pda.library.entity.ParameterSystem;
import com.microtechmd.pda.library.entity.ValueInt;
import com.microtechmd.pda.library.entity.monitor.DateTime;
import com.microtechmd.pda.library.entity.monitor.Event;
import com.microtechmd.pda.library.entity.monitor.History;
import com.microtechmd.pda.library.entity.monitor.Status;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.LogPDA;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.activity.MainActivity;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.fragment.FragmentDialog;
import com.microtechmd.pda_app.fragment.FragmentInput;
import com.microtechmd.pda_app.fragment.FragmentMessage;
import com.microtechmd.pda_app.fragment.FragmentNewProgress;
import com.microtechmd.pda_app.manager.AppManager;
import com.microtechmd.pda_app.util.KeyNavigation;
import com.microtechmd.pda_app.util.MediaUtil;
import com.microtechmd.pda_app.util.RfAddressUtil;
import com.microtechmd.pda_app.util.notificationUtil.NotifyUtil;
import com.microtechmd.pda_app.widget.WidgetStatusBar;
import com.vise.baseble.utils.BleUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;


public class ActivityPDA extends AppCompatActivity
        implements
        KeyNavigation.OnClickViewListener {
    protected static final int QUERY_STATE_CYCLE = 1000;
    protected static final int QUERY_STATE_TIMEOUT = 10000;
    public static final int GLUCOSE_UNIT_MMOL = 0;
    public static final int GLUCOSE_UNIT_MG = 1;
    public static final int COUNT_GLUCOSE_UNIT = 2;
    public static final int GLUCOSE_UNIT_MG_STEP = 10;
    public static final int GLUCOSE_UNIT_MMOL_STEP = 18;
    public static final String SETTING_GLUCOSE_UNIT = "glucose_unit";
    public static final String CALIBRATION_HISTORY = "calibration_history";
    public static final String DEVICES_LIST = "devices_list";
    public static final String HIMESSAGETIPS = "hi_messagetips";
    public static final String LOMESSAGETIPS = "low_messagetips";
    public static final String COMMMESSAGETIPS = "comm_messagetips";
    public static final String LOWGLUCOSE_SAVED = "lowglucose_saved";
    public static final String HIGHGLUCOSE_SAVED = "highglucose_saved";
    protected static final String SETTING_STATUS_BAR = "status_bar";
    public static final String PAIRLIST = "pair_list";


    public static final int HIGH_DEFAULT = 120;
    public static final int HIGH_MAX = 250;
    public static final int HIGH_MIN = 80;
    public static final int LOW_DEFAULT = 35;
    public static final int LOW_MAX = 50;
    public static final int LOW_MIN = 22;

    private static final int INVALID = -1;
    private static final int TRANSMITTER_STARTUP = 0;
    public static final int TRANSMITTER_ERROR = 1;
    private static final int BATTERY_LOW = 2;
    private static final int BATTERY_EXHAUSTED = 3;
    private static final int SENSOR_NEW = 4;
    public static final int SENSOR_ERROR = 5;
    public static final int SENSOR_EXPIRATION = 6;
    public static final int GLUCOSE = 7;
    public static final int GLUCOSE_RECOMMEND_CAL = 8;
    public static final int GLUCOSE_INVALID = 9;
    public static final int HYPO = 10;
    public static final int HYPER = 11;
    public static final int IMPENDANCE = 12;
    public static final int BLOOD_GLUCOSE = 13;
    public static final int CALIBRATION = 14;
    private static final int PDA_BATTERY_LOW = 21;
    public static final int PDA_ERROR = 22;
    private static final int PDA_COMM_ERROR = 23;
    public static final String SETTING_TIME_FORMAT = "setting_time_format";
    public static final String SETTING_RF_ADDRESS = "setting_rf_address";
    public static final String RFSIGNAL = "rfsignal";
    public static int YEAR_MIN = 2017;

    protected ActivityPDA mBaseActivity;
    protected LayoutInflater mLayoutInflater;
    protected boolean mLandscape;

    protected boolean commErrorFlag;

    private long mToastLastShowTime;
    private String mToastLastShowString;


    private static boolean sIsPowerdown = false;
    private static boolean sIsPDABatteryLow = false;
    private static boolean sIsPDABatteryCharging = false;
    private static PowerManager.WakeLock sWakeLock = null;
    private static WidgetStatusBar sStatusBar = null;

    protected LogPDA mLog = null;
    protected MessageListener mMessageListener = null;

    private boolean mIsForeground = false;
    private DataStorage mDataStorage = null;
    private KeyNavigation mKeyNavigation = null;
    private BroadcastReceiver mBroadcastReceiver = null;
    private Handler mHandlerBrightness = null;
    private Runnable mRunnableBrightness = null;
    private Stack<Window> mScreenWindowStack = null;

    private FragmentDialog mFragmentDialog = null;
    private FragmentMessage mFragmentAlarm = null;
    //    private FragmentProgress mFragmentProgress = null;
    private FragmentNewProgress mFragmentProgress = null;

    private int mDialogLoadingCount = 0;
    private boolean newSensorFlag = true;
    private FragmentDialog newSensorFragmentDialog;
    private FragmentDialog commErrFragmentDialog;
    private FragmentDialog pdaLowBatteryFragmentDialog;

    private FragmentDialog sensorErrFragmentDialog;
    private FragmentDialog sensorExpirationFragmentDialog;
    private FragmentDialog lowGlucoseFragmentDialog;
    private FragmentDialog highGlucoseFragmentDialog;

    private static final int COMM_ERR_TYPE = 101;
    private static final int PAD_LOWBATTERY_ERR_TYPE = 102;
    protected ApplicationPDA app;

    private CountDownTimer timer;

    private CountDownTimer conn_timer;

    private PendingIntent sender;
    private AlarmManager manager;

    protected class MessageListener
            implements
            EntityMessage.Listener {
        @Override
        public void onReceive(EntityMessage message) {
            mLog.Debug(getClass(),
                    "Handle message: " + "Source Address:" +
                            message.getSourceAddress() + " Target Address:" +
                            message.getTargetAddress() + " Source Port:" +
                            message.getSourcePort() + " Target Port:" +
                            message.getTargetPort() + " Operation:" +
                            message.getOperation() + " Parameter:" +
                            message.getParameter());

            switch (message.getOperation()) {
                case EntityMessage.OPERATION_SET:
                    setParameter(message);
                    break;

                case EntityMessage.OPERATION_GET:
                    getParameter(message);
                    break;

                case EntityMessage.OPERATION_EVENT:
                    handleEvent(message);
                    break;

                case EntityMessage.OPERATION_NOTIFY:
                    handleNotification(message);
                    break;

                case EntityMessage.OPERATION_ACKNOWLEDGE:
                    handleAcknowledgement(message);
                    break;
                case EntityMessage.OPERATION_PAIR:
                    handlePair(message);
                    break;

                case EntityMessage.OPERATION_UNPAIR:
                    handleUnPair(message);
                    break;

                case EntityMessage.OPERATION_BOND:
                    handleBond(message);
                    break;
                default:
                    break;
            }
        }
    }

    protected void handlePair(EntityMessage message) {
    }

    protected void handleUnPair(EntityMessage message) {
    }

    protected void handleBond(EntityMessage message) {
    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_pda);

        ViewStub viewStub = (ViewStub) findViewById(R.id.stub_activity);

        if (viewStub != null) {
            viewStub.setLayoutResource(layoutResID);
            viewStub.inflate();
            resetKeyNavigation();
        }
    }

//
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            updateScreenBrightness();
//        }
//
//        return super.dispatchTouchEvent(ev);
//    }
//
//
//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        if (event.getAction() == KeyEvent.ACTION_UP) {
//            updateScreenBrightness();
//        }
//
//        return super.dispatchKeyEvent(event);
//    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            updateScreenBrightness();
        }
    }


    @Override
    public void onClick(View v) {
        onClickView(v);
    }


    public void handleMessage(final EntityMessage message) {
        if (message.getTargetAddress() == ParameterGlobal.ADDRESS_REMOTE_MASTER) {
            showDialogProgress();
        }
        if (conn_timer == null) {
            conn_timer = new CountDownTimer(25 * 1000, 25 * 1000) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    dismissDialogProgress();
                }
            }.start();
        } else {
            conn_timer.cancel();
            conn_timer.start();
        }
        ((ApplicationPDA) getApplication()).handleMessage(message);
    }


    public void updateScreenBrightness() {
        final int SCREEN_BRIGHTNESS_MAX = 255;
        final int SCREEN_BRIGHTNESS_MIN = 15;

        int reduceBrightnessCycle = 0;


        if (reduceBrightnessCycle > 0) {
            if (mHandlerBrightness == null) {
                mHandlerBrightness = new Handler();
            }

            if (mRunnableBrightness != null) {
                mHandlerBrightness.removeCallbacks(mRunnableBrightness);
            }

            mRunnableBrightness = new Runnable() {

                @Override
                public void run() {
                    if ((mScreenWindowStack != null) &&
                            (!mScreenWindowStack.isEmpty())) {
                        // Set screen brightness to minimum
                        WindowManager.LayoutParams layoutParams =
                                mScreenWindowStack.peek().getAttributes();
                        layoutParams.screenBrightness =
                                (float) SCREEN_BRIGHTNESS_MIN /
                                        (float) SCREEN_BRIGHTNESS_MAX;
                        mScreenWindowStack.peek().setAttributes(layoutParams);
                    }
                }
            };

            mHandlerBrightness.postDelayed(mRunnableBrightness,
                    reduceBrightnessCycle);
        }

        if ((mScreenWindowStack != null) && (!mScreenWindowStack.isEmpty())) {
            // Restore screen brightness to system setting
            WindowManager.LayoutParams layoutParams =
                    mScreenWindowStack.peek().getAttributes();
            layoutParams.screenBrightness =
                    (float) Settings.System.getInt(getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS, SCREEN_BRIGHTNESS_MAX) /
                            (float) SCREEN_BRIGHTNESS_MAX;
            mScreenWindowStack.peek().setAttributes(layoutParams);
        }
    }


    public void pushScreenWindow(final Window window) {
        if (mScreenWindowStack == null) {
            mScreenWindowStack = new Stack<Window>();
        }

        mScreenWindowStack.push(window);
    }


    public void popScreenWindow() {
        if (mScreenWindowStack == null) {
            mScreenWindowStack = new Stack<Window>();
        }

        if (!mScreenWindowStack.isEmpty()) {
            mScreenWindowStack.pop();
        }
    }


    public DataStorage getDataStorage(String name) {
        if (name == null) {
            name = getClass().getSimpleName();
        }

        if (mDataStorage == null) {
            mDataStorage = new DataStorage(this, name);
        }

        if (!mDataStorage.getName().equals(name)) {
            mDataStorage = new DataStorage(this, name);
        }

        return mDataStorage;
    }


    public int getGlucoseUnit() {
        return (int) SPUtils.get(this, SETTING_GLUCOSE_UNIT, GLUCOSE_UNIT_MMOL);
    }


    public String getGlucoseValue(int value, boolean unit) {
        String result;


        if (getGlucoseUnit() == GLUCOSE_UNIT_MMOL) {
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            result = decimalFormat
                    .format((double) value / (double) (GLUCOSE_UNIT_MG_STEP * 10));

            if (unit) {
                result += getResources().getString(R.string.unit_mmol_l);
            }
        } else {
            result =
                    ((value + GLUCOSE_UNIT_MG_STEP - 1) / GLUCOSE_UNIT_MG_STEP) +
                            "";

            if (unit) {
                result += getResources().getString(R.string.unit_mg_dl);
            }
        }

        return result;
    }


    public String getDateString(long dateTime, TimeZone timeZone) {
        String template = "yyyy-M-d";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(template);

        if (timeZone != null) {
            simpleDateFormat.setTimeZone(timeZone);
        }

        return simpleDateFormat.format(new Date(dateTime));
    }


    public String getTimeString(long dateTime, TimeZone timeZone) {
        String template;
        if ((boolean) SPUtils.get(this, SETTING_TIME_FORMAT, true)) {
            template = "H:mm";
        } else {
            template = "h:mm a";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(template, Locale.getDefault());

        if (timeZone != null) {
            simpleDateFormat.setTimeZone(timeZone);
        }

        return simpleDateFormat.format(new Date(dateTime));
    }


    public WidgetStatusBar getStatusBar() {
        return sStatusBar;
    }


    public void resetKeyNavigation() {
        mKeyNavigation.resetNavigation(getWindow().getDecorView());
    }


    public FragmentDialog showDialogConfirm(String title,
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
        fragmentDialog.show(getSupportFragmentManager(), null);

        return fragmentDialog;
    }

    private FragmentDialog showNewSensorDialogConfirm(String title,
                                                      String buttonTextPositive, String buttonTextNegative, Fragment content,
                                                      boolean isCancelable, FragmentDialog.ListenerDialog listener) {
        newSensorFragmentDialog = new FragmentDialog();
        newSensorFragmentDialog.setTitle(title);
        newSensorFragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_POSITIVE,
                buttonTextPositive);
        newSensorFragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_NEGATIVE,
                buttonTextNegative);
        newSensorFragmentDialog.setContent(content);
        newSensorFragmentDialog.setCancelable(isCancelable);
        newSensorFragmentDialog.setListener(listener);
        newSensorFragmentDialog.show(getSupportFragmentManager(), null);

        return newSensorFragmentDialog;
    }

    private FragmentDialog showErrConfirm(int errType, String title,
                                          String buttonTextPositive, String buttonTextNegative,
                                          Fragment content, FragmentDialog.ListenerDialog listener) {
        FragmentDialog errFragmentDialog;
        switch (errType) {
            case COMM_ERR_TYPE:
                commErrFragmentDialog = new FragmentDialog();
                errFragmentDialog = commErrFragmentDialog;
                break;
            case PAD_LOWBATTERY_ERR_TYPE:
                pdaLowBatteryFragmentDialog = new FragmentDialog();
                errFragmentDialog = pdaLowBatteryFragmentDialog;
                break;
            default:
                errFragmentDialog = new FragmentDialog();
                break;
        }
        errFragmentDialog.setTitle(title);
        errFragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_POSITIVE,
                buttonTextPositive);
        errFragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_NEGATIVE,
                buttonTextNegative);
        errFragmentDialog.setContent(content);
        errFragmentDialog.setCancelable(false);
        errFragmentDialog.setListener(listener);
        errFragmentDialog.show(getSupportFragmentManager(), null);

        return errFragmentDialog;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        app = (ApplicationPDA) getApplication();
        //添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);
        mLog = new LogPDA();
        mKeyNavigation = new KeyNavigation(this, this);
        mMessageListener = new MessageListener();
        ((ApplicationPDA) getApplication()).registerMessageListener(
                ParameterGlobal.PORT_COMM, mMessageListener);
        ((ApplicationPDA) getApplication()).registerMessageListener(
                ParameterGlobal.PORT_MONITOR, mMessageListener);
        ((ApplicationPDA) getApplication()).registerMessageListener(
                ParameterGlobal.PORT_GLUCOSE, mMessageListener);

        if (sWakeLock == null) {
            PowerManager powerManager =
                    (PowerManager) getSystemService(Context.POWER_SERVICE);
            assert powerManager != null;
            sWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getSimpleName());
        }

        if (sStatusBar == null) {
            sStatusBar = new WidgetStatusBar();
            sStatusBar.setByteArray(SPUtils.getbytes(this, SETTING_STATUS_BAR));
            final int reaction;

            if (sStatusBar.getAlarm() != null) {
                reaction = ParameterSystem.REACTION_ALARM;
            } else if (sStatusBar.getAlertList().size() > 0) {
                reaction = ParameterSystem.REACTION_ALERT;
            } else {
                reaction = ParameterSystem.REACTION_NORMAL;
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    triggerReaction(reaction);
                }
            }, 2000);
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                assert action != null;
                switch (action) {
                    case Intent.ACTION_BATTERY_CHANGED:
                        onBatteryChanged(
                                intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0),
                                intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1));
                        break;
                    case Intent.ACTION_TIME_TICK:

                        onTimeTick();
                        //刷新血糖时间
                        handleMessage(
                                new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                        ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                        ParameterGlobal.PORT_MONITOR,
                                        ParameterGlobal.PORT_MONITOR,
                                        EntityMessage.OPERATION_SET,
                                        ParameterMonitor.GLUCOSE_DISPLAY,
                                        null));
                        break;
//                    case "glucose_display":
//                        handleMessage(
//                                new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
//                                        ParameterGlobal.ADDRESS_LOCAL_VIEW,
//                                        ParameterGlobal.PORT_MONITOR,
//                                        ParameterGlobal.PORT_MONITOR,
//                                        EntityMessage.OPERATION_SET,
//                                        ParameterMonitor.GLUCOSE_DISPLAY,
//                                        null));
//                        break;
                    case "comm_err":
                        String mRFAddress = RfAddressUtil.getAddress(SPUtils.getbytes(getApplicationContext(), SETTING_RF_ADDRESS));
                        if (TextUtils.isEmpty(mRFAddress)) {
                            return;
                        }
                        boolean comm_messageFlag = (boolean) SPUtils.get(getApplicationContext(), COMMMESSAGETIPS, true);
                        if (comm_messageFlag) {
                            History history = new History();
                            Event event = new Event();
                            event.setEvent(PDA_COMM_ERROR);
                            history.setEvent(event);
                            notifyErrEventAlert(history, COMM_ERR_TYPE);
                        }
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                        byte[] addressByte = SPUtils.getbytes(ActivityPDA.this, SETTING_RF_ADDRESS);
                        switch (blueState) {
                            case BluetoothAdapter.STATE_TURNING_ON:
                                break;
                            case BluetoothAdapter.STATE_ON:
                                getStatusBar().setBluetoothOn(true);
                                if (addressByte != null) {
                                    handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                            ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                                            ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                                            ParameterComm.PARAM_RF_BROADCAST_SWITCH, new byte[]{1}));
                                }
                                break;
                            case BluetoothAdapter.STATE_TURNING_OFF:
                                break;
                            case BluetoothAdapter.STATE_OFF:
                                getStatusBar().setBluetoothOn(false);
                                handleSignal(0);
                                if (addressByte != null) {
                                    handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                            ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                                            ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                                            ParameterComm.PARAM_RF_BROADCAST_SWITCH, new byte[]{0}));
                                }
                                break;
                        }
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        intentFilter.addAction("glucose_display");
        intentFilter.addAction("comm_err");
        registerReceiver(mBroadcastReceiver, intentFilter);
        pushScreenWindow(getWindow());

        mBaseActivity = this;
        mToastLastShowTime = 0;
        mLayoutInflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//        new CountDownTimer(50 * 1000, 10 * 1000) {
//
//            @Override
//            public void onTick(long l) {
//                History h = new History();
//                Event e = new Event();
//                if (typ == 12) {
//                    typ = 10;
//                }
//                e.setEvent(typ);
//                typ++;
//                h.setEvent(e);
//                alertTips(h);
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        }.start();
    }

    int typ = 10;

    @Override
    protected void onResume() {
        super.onResume();

        getStatusBar().setView(getWindow().getDecorView());

        onTimeTick();
        if (BleUtil.isBleEnable(this)) {
            getStatusBar().setBluetoothOn(true);
        } else {
            getStatusBar().setBluetoothOn(false);
        }
        mIsForeground = true;
    }


    @Override
    protected void onPause() {
        super.onPause();

        mIsForeground = false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //结束Activity&从堆栈中移除
        AppManager.getAppManager().finishActivity(this);
        mDialogLoadingCount = 0;
        ((ApplicationPDA) getApplication()).unregisterMessageListener(
                ParameterGlobal.PORT_COMM, mMessageListener);
        ((ApplicationPDA) getApplication()).unregisterMessageListener(
                ParameterGlobal.PORT_MONITOR, mMessageListener);
        ((ApplicationPDA) getApplication()).unregisterMessageListener(
                ParameterGlobal.PORT_GLUCOSE, mMessageListener);
        unregisterReceiver(mBroadcastReceiver);
        dismissDialogProgress();
        popScreenWindow();
    }


    protected void onBatteryChanged(int level, final int status) {
        final int BATTERY_LEVEL_LOW = 20;
        final int BATTERY_LEVEL_RECOVER = 30;
        final int BATTERY_UPDATE_CYCLE = 1000;


        if ((status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                (status == BatteryManager.BATTERY_STATUS_FULL)) {
            if (pdaLowBatteryFragmentDialog != null) {
                Event event = new Event(0, PDA_BATTERY_LOW, 0);
                History history = new History(
                        new DateTime(Calendar.getInstance()), new Status(), event);
                confirmAlarm(history);
                pdaLowBatteryFragmentDialog.dismissAllowingStateLoss();
            }

            if (!sIsPDABatteryCharging) {
                sIsPDABatteryCharging = true;
                final Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        WidgetStatusBar statusBar = getStatusBar();

                        if (sIsPDABatteryCharging) {
                            statusBar.setPDACharger(!statusBar.getPDACharger());
                            handler.postDelayed(this, BATTERY_UPDATE_CYCLE);
                        } else {
                            statusBar.setPDACharger(false);
                        }
                    }
                });
            }
        } else {
            if (sIsPDABatteryCharging) {
                sIsPDABatteryCharging = false;
                getStatusBar().setPDACharger(false);
            }
        }

        getStatusBar().setPDABattery(level);

//        if ((!sIsPDABatteryLow) && (level < BATTERY_LEVEL_LOW)) {
//            sIsPDABatteryLow = true;
//
//            Event event = new Event(0, PDA_BATTERY_LOW, 0);
//            History history = new History(
//                    new DateTime(Calendar.getInstance()), new Status(), event);
//            notifyErrEventAlert(history, PAD_LOWBATTERY_ERR_TYPE);
//        }

        if ((sIsPDABatteryLow) && (level > BATTERY_LEVEL_RECOVER)) {
            sIsPDABatteryLow = false;
        }
    }


    protected void onTimeTick() {
        getStatusBar().setDateTime(System.currentTimeMillis(),
                (boolean) SPUtils.get(this, SETTING_TIME_FORMAT, true));

    }

    protected void onClickView(View v) {
    }

    public void setProgressContent(final String content) {
        if (mFragmentProgress == null) {
            mFragmentProgress = new FragmentNewProgress(this,
                    R.style.Theme_Light_NoTitle_Dialog);
        }

        mFragmentProgress.setComment(content);

        if (mDialogLoadingCount <= 0) {
            showDialogProgress();
        }
    }


    public void showDialogProgress() {
        mLog.Debug(getClass(), "Show progress dialog");

        if (mFragmentProgress == null) {
            mFragmentProgress = new FragmentNewProgress(this,
                    R.style.Theme_Light_NoTitle_Dialog);
            mFragmentProgress.setCancelable(false);
            mFragmentProgress.setComment(getString(R.string.connecting));
        }

//        if (mFragmentDialog == null) {
//            mFragmentDialog = new FragmentDialog();
//            mFragmentDialog.setBottom(false);
//            mFragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_POSITIVE,
//                    null);
//            mFragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_NEGATIVE,
//                    null);
//            mFragmentDialog.setContent(mFragmentProgress);
//            mFragmentDialog.setCancelable(false);
//        }

        if (mDialogLoadingCount <= 0) {
            mFragmentProgress.show();
        }
        mDialogLoadingCount++;
    }


    public void dismissDialogProgress() {
        mLog.Debug(getClass(), "Dismiss progress dialog");

        if (mDialogLoadingCount > 0) {
            mDialogLoadingCount--;
        }

        if (mDialogLoadingCount <= 0) {
            mDialogLoadingCount = 0;

            if (mFragmentProgress != null) {
                mFragmentProgress.setComment(getString(R.string.connecting));
                mFragmentProgress.dismiss();
            }

//            if (mFragmentDialog != null) {
//                mFragmentDialog.dismissAllowingStateLoss();
//            }
        }
    }

    public void showDialogLoading() {
        mLog.Debug(getClass(), "Show progress dialog");

        if (mFragmentProgress == null) {
            mFragmentProgress = new FragmentNewProgress(this,
                    R.style.Theme_Light_NoTitle_Dialog);
            mFragmentProgress.setCancelable(false);
            mFragmentProgress.setComment(getString(R.string.loading));
        }

//        if (mFragmentDialog == null) {
//            mFragmentDialog = new FragmentDialog();
//            mFragmentDialog.setBottom(false);
//            mFragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_POSITIVE,
//                    null);
//            mFragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_NEGATIVE,
//                    null);
//            mFragmentDialog.setContent(mFragmentProgress);
//            mFragmentDialog.setCancelable(false);
//        }

        if (mDialogLoadingCount <= 0) {
            mFragmentProgress.show();
        }
        mDialogLoadingCount++;
    }


    public void dismissDialogLoading() {
        mLog.Debug(getClass(), "Dismiss progress dialog");

        if (mDialogLoadingCount > 0) {
            mDialogLoadingCount--;
        }

        if (mDialogLoadingCount <= 0) {
            mDialogLoadingCount = 0;

            if (mFragmentProgress != null) {
                mFragmentProgress.setComment(getString(R.string.loading));
                mFragmentProgress.dismiss();
            }

//            if (mFragmentDialog != null) {
//                mFragmentDialog.dismissAllowingStateLoss();
//            }
        }
    }


    protected void setParameter(final EntityMessage message) {
        mLog.Debug(getClass(), "Set Parameter: " + message.getParameter());
        if (message.getParameter() == ParameterComm.CLOSE_COMM) {
            commErrorFlag = message.getData()[0] != 0;
        }
        if (message.getParameter() == ParameterComm.PAIRAGAIN) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.fragment_settings_pairing)
                    .setMessage(R.string.pair_data_err)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            SPUtils.putbytes(this, SETTING_RF_ADDRESS, null);
        }
    }


    protected void getParameter(final EntityMessage message) {
        mLog.Debug(getClass(), "Get Parameter: " + message.getParameter());
    }


    protected void handleEvent(final EntityMessage message) {
        mLog.Debug(getClass(), "Handle Event: " + message.getEvent());

        if (message.getSourceAddress() == ParameterGlobal.ADDRESS_REMOTE_MASTER) {
            if (message.getEvent() == EntityMessage.EVENT_TIMEOUT) {
                dismissDialogProgress();
                if (mIsForeground || hasWindowFocus()) {
                    Toast.makeText(this,
                            getResources().getString(R.string.connect_timeout),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (message.getParameter() == ParameterMonitor.PARAM_EVENT) {
            boolean comm_messageFlag = (boolean) SPUtils.get(this, COMMMESSAGETIPS, true);
            if (comm_messageFlag) {
                History history = new History(message.getData());
                notifyEventAlert(history);
            }
        }

        if (message.getParameter() == ParameterMonitor.PARAM_NEW) {
            History history = new History(message.getData());
            Status status = history.getStatus();
            int value = status.getShortValue1();
            mLog.Error(getClass(), "初始化：" + history.getDateTime().getBCD()
                    + "index  :" + history.getEvent().getIndex() + "type :" + history.getEvent().getEvent()
                    + "value  :" + value);
            if (history.getEvent().getEvent() == SENSOR_NEW) {
                if (value == 0xFF) {
                    if (newSensorFlag) {
                        newSensorFlag = false;
                        if (mIsForeground || hasWindowFocus() ||
                                (mFragmentAlarm != null)) {
                            notifyNewSensorEventAlert(history);
                        }
                    }
                } else {
                    newSensorFlag = true;
                    if (newSensorFragmentDialog != null) {
                        confirmNewSensorAlarm(history);
                        newSensorFragmentDialog.dismissAllowingStateLoss();
                    }
                    handleMessage(
                            new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                    ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                    ParameterGlobal.PORT_MONITOR,
                                    ParameterGlobal.PORT_MONITOR,
                                    EntityMessage.OPERATION_SET,
                                    ParameterMonitor.COUNTDOWNVIEW_VISIBLE,
                                    new ValueInt(value).getByteArray()));
                }

            }
        }
    }


    protected void handleNotification(EntityMessage message) {
        mLog.Debug(getClass(), "Notify Port: " + message.getSourcePort() +
                " Parameter: " + message.getParameter());

        if (message.getSourcePort() == ParameterGlobal.PORT_COMM) {
            switch (message.getParameter()) {
                case ParameterComm.PARAM_RF_SIGNAL:
                    handleSignal((int) message.getData()[0]);
                    if (timer == null) {
                        timer = new CountDownTimer(20 * 1000, 20 * 1000) {
                            @Override
                            public void onTick(long l) {
                            }

                            @Override
                            public void onFinish() {
                                handleSignal(0);
                            }
                        };
                    } else {
                        timer.cancel();
                    }
                    timer.start();
                    break;
                case ParameterComm.BLE_SCAN:
                    getStatusBar().setBluetoothStation(R.drawable.ic_bluetooth_scaning);
                    break;
                case ParameterComm.BLE_CONNECTING:
                    getStatusBar().setBluetoothStation(R.drawable.ic_bluetooth_connect);
                    break;
                case ParameterComm.BLE_NORMAL:
                    getStatusBar().setBluetoothStation(R.drawable.ic_bluetooth);
                    break;
                default:

                    break;
            }
        }

        if ((message.getSourcePort() == ParameterGlobal.PORT_MONITOR) &&
                (message.getParameter() == ParameterMonitor.PARAM_HISTORY)) {
            if (mIsForeground || hasWindowFocus()) {
                if (message.getData() != null) {
                    DataList dataList = new DataList(message.getData());
                    for (int i = 0; i < dataList.getCount(); i++) {
                        History history = new History(dataList.getData(i));
                        Event event = history.getEvent();
                        if (event.getEvent() == TRANSMITTER_STARTUP || event.getEvent() == INVALID || event.getEvent() == GLUCOSE) {
                            continue;
                        }
                        long timeChange = System.currentTimeMillis() - history.getDateTime().getCalendar().getTimeInMillis();
                        if (Math.abs(timeChange) < 15 * 60 * 1000) {
                            boolean low_messageFlag = (boolean) SPUtils.get(this, LOMESSAGETIPS, true);
                            boolean hi_messageFlag = (boolean) SPUtils.get(this, HIMESSAGETIPS, true);
                            switch (event.getEvent()) {
                                case HYPO:
                                    if (low_messageFlag) {
                                        alertTips(history);
                                    }
                                    break;
                                case HYPER:
                                    if (hi_messageFlag) {
                                        alertTips(history);
                                    }
                                    break;
                                case SENSOR_ERROR:
                                case SENSOR_EXPIRATION:
                                    alertTips(history);
                                    break;
                                default:

                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleSignal(int signal) {
        getStatusBar().setRFSignal(signal);
        SPUtils.put(this, RFSIGNAL, signal);
        byte[] addressByte = SPUtils.getbytes(ActivityPDA.this, SETTING_RF_ADDRESS);
        if (addressByte == null) {
            return;
        }
        if (signal == 0) {
            manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (sender != null) {
                assert manager != null;
                manager.cancel(sender);
                sender = null;
            }
            Intent intent = new Intent();
            // 设置Intent action属性
            intent.setAction("comm_err");
            // 实例化PendingIntent
            if (sender == null) {
                sender = PendingIntent.getBroadcast(this, COMM_ERR_TYPE,
                        intent, 0);
            }
            manager.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 15 * 60 * 1000, sender);
        } else {
            if (commErrFragmentDialog != null) {
                commErrFragmentDialog.dismissAllowingStateLoss();
                Event event = new Event(0, PDA_COMM_ERROR, 0);
                History history = new History();
                history.setEvent(event);
                confirmAlarm(history);
            }
        }
    }


    protected void handleAcknowledgement(final EntityMessage message) {
        mLog.Debug(getClass(), "Acknowledge Port: " + message.getSourcePort() +
                " Parameter: " + message.getParameter());

        if (message.getData()[0] == EntityMessage.FUNCTION_OK) {
            mLog.Debug(getClass(), "Acknowledge OK");
        } else {
            mLog.Debug(getClass(), "Acknowledge Fail");

//            Toast.makeText(this, getResources().getString(R.string.connect_fail),
//                    Toast.LENGTH_SHORT)
//                    .show();
        }

        if (message.getSourceAddress() == ParameterGlobal.ADDRESS_REMOTE_MASTER) {
            dismissDialogProgress();
        }
    }


    protected String getEventContent(final Event event) {
        String content = "";

        switch (event.getEvent()) {
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

            case PDA_BATTERY_LOW:
                content = getString(R.string.alarm_pda_battery);
                break;

            case PDA_ERROR:
                content = getString(R.string.alarm_pda_error);
                break;

            case PDA_COMM_ERROR:
                content = getString(R.string.alarm_pda_comm_error);
                break;
            default:
                break;
        }
        return content;
    }


    private static synchronized void acquireWakeLock() {
        if (!sWakeLock.isHeld()) {
            sWakeLock.acquire();
        }
    }


    private static synchronized void releaseWakeLock() {
        if (sWakeLock.isHeld()) {
            sWakeLock.release();
        }
    }

    private void notifyNewSensorEventAlert(final History history) {
        ArrayList<History> alertList = getStatusBar().getAlertList();
        if (history.getEvent().getEvent() > 0) {
            if (getStatusBar().getAlarm() == null) {
                for (int i = 0; i < alertList.size(); i++) {
                    if (history.getEvent().getEvent() == alertList.get(i).getEvent().getEvent()) {
                        alertList.remove(i);
                    }
                }
                triggerReaction(ParameterSystem.REACTION_ALERT);
                showNewSensorDialog(history);
                alertList.add(history);

                getStatusBar().setAlertList(alertList);
                mLog.Error(getClass(), "添加报警1：" + alertList.size() + "类型：" + history.getEvent().getEvent());
                getDataStorage(ActivityPDA.class.getSimpleName())
                        .setExtras(SETTING_STATUS_BAR, getStatusBar().getByteArray());
            }
        }
    }

    private void notifyErrEventAlert(final History history, int errType) {
        ArrayList<History> alertList = getStatusBar().getAlertList();
        if (history.getEvent().getEvent() > 0) {
            if (getStatusBar().getAlarm() == null) {
                if (!TextUtils.isEmpty(getEventContent(history.getEvent()))) {
                    for (int i = 0; i < alertList.size(); i++) {
                        if (history.getEvent().getEvent() == alertList.get(i).getEvent().getEvent()) {
                            alertList.remove(i);
                        }
                    }
                    triggerReaction(ParameterSystem.REACTION_ALERT);
                    showErrDialog(history, errType);
                    alertList.add(history);

                    getStatusBar().setAlertList(alertList);
                    mLog.Error(getClass(), "添加报警2：" + alertList.size() + "类型：" + history.getEvent().getEvent());

                    getDataStorage(ActivityPDA.class.getSimpleName())
                            .setExtras(SETTING_STATUS_BAR, getStatusBar().getByteArray());
                }
            }
        }
    }

    private void alertTips(History history) {
        //有报警刷新历史日志
        handleMessage(
                new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                        ParameterGlobal.ADDRESS_LOCAL_VIEW,
                        ParameterGlobal.PORT_MONITOR,
                        ParameterGlobal.PORT_MONITOR,
                        EntityMessage.OPERATION_SET,
                        ParameterMonitor.TIPSREFRESH,
                        null));
        //有报警同步原始数据
//        handleMessage(
//                new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
//                        ParameterGlobal.ADDRESS_LOCAL_CONTROL,
//                        ParameterGlobal.PORT_MONITOR,
//                        ParameterGlobal.PORT_MONITOR,
//                        EntityMessage.OPERATION_SET,
//                        ParameterComm.SYNCHRONIZE_RAW_DATA,
//                        null));

        switch (history.getEvent().getEvent()) {
            case SENSOR_ERROR:
                triggerReaction(ParameterSystem.REACTION_ALERT);
                if (sensorErrFragmentDialog == null) {
                    sensorErrFragmentDialog = new FragmentDialog();
                    newAlertTips(sensorErrFragmentDialog, history);
                } else {
                    reshowAlertTips(sensorErrFragmentDialog);
                }
                break;
            case SENSOR_EXPIRATION:
                triggerReaction(ParameterSystem.REACTION_ALERT);
                if (sensorExpirationFragmentDialog == null) {
                    sensorExpirationFragmentDialog = new FragmentDialog();
                    newAlertTips(sensorExpirationFragmentDialog, history);
                } else {
                    reshowAlertTips(sensorExpirationFragmentDialog);
                }
                break;
            case HYPO:
                triggerReaction(ParameterSystem.REACTION_ALARM);
                if (lowGlucoseFragmentDialog == null) {
                    lowGlucoseFragmentDialog = new FragmentDialog();
                    newAlertTips(lowGlucoseFragmentDialog, history);
                } else {
                    reshowAlertTips(lowGlucoseFragmentDialog);
                }
                break;
            case HYPER:
                triggerReaction(ParameterSystem.REACTION_ALERT);

                if (highGlucoseFragmentDialog == null) {
                    highGlucoseFragmentDialog = new FragmentDialog();
                    newAlertTips(highGlucoseFragmentDialog, history);
                } else {
                    reshowAlertTips(highGlucoseFragmentDialog);
                }
                break;
            default:
                break;
        }
    }

    private void newAlertTips(FragmentDialog fragmentDialog, History history) {
        FragmentMessage message = new FragmentMessage();
        message.setComment(getEventContent(history.getEvent()));

        fragmentDialog.setTitle(getString(R.string.alarm_dialog_title));
        fragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_POSITIVE,
                "");
        fragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_NEGATIVE,
                null);
        fragmentDialog.setContent(message);
        fragmentDialog.setCancelable(false);
        fragmentDialog.setListener(new FragmentDialog.ListenerDialog() {
            @Override
            public boolean onButtonClick(int buttonID, Fragment content) {
                switch (buttonID) {
                    case FragmentDialog.BUTTON_ID_POSITIVE:
                        triggerReaction(ParameterSystem.REACTION_NORMAL);
                    default:
                        break;
                }

                return true;
            }
        });
        fragmentDialog.show(getSupportFragmentManager(), null);
    }

    private void reshowAlertTips(FragmentDialog fragmentDialog) {
        fragmentDialog.dismissAllowingStateLoss();
        fragmentDialog.show(getSupportFragmentManager(), null);
    }

    private void notifyEventAlert(final History history) {
        ArrayList<History> alertList = getStatusBar().getAlertList();

        if (history.getEvent().getEvent() > 0) {
            if (getStatusBar().getAlarm() == null) {
                if (!TextUtils.isEmpty(getEventContent(history.getEvent()))) {
                    for (int i = 0; i < alertList.size(); i++) {
                        if (history.getEvent().getEvent() == alertList.get(i).getEvent().getEvent()) {
                            alertList.remove(i);
                        }
                    }
                    triggerReaction(ParameterSystem.REACTION_ALERT);
                    showDialogAlarm(history);
                    alertList.add(history);
                    getStatusBar().setAlertList(alertList);
                    mLog.Error(getClass(), "添加报警3：" + alertList.size() + "类型：" + history.getEvent().getEvent());
                    getDataStorage(ActivityPDA.class.getSimpleName())
                            .setExtras(SETTING_STATUS_BAR, getStatusBar().getByteArray());
                }
            }
        }
    }


    private void notifyEventAlarm(final History history) {
        acquireWakeLock();
        ArrayList<History> alertList = getStatusBar().getAlertList();

        if (history.getEvent().getEvent() > 0) {
            if (getStatusBar().getAlarm() == null) {
                if (!TextUtils.isEmpty(getEventContent(history.getEvent()))) {
                    for (int i = 0; i < alertList.size(); i++) {
                        if (history.getEvent().getEvent() == alertList.get(i).getEvent().getEvent()) {
                            alertList.remove(i);
                        }
                    }
                    triggerReaction(ParameterSystem.REACTION_ALARM);
                    showDialogAlarm(history);
                    alertList.add(history);

                    getStatusBar().setAlertList(alertList);
                    mLog.Error(getClass(), "添加报警4：" + alertList.size() + "类型：" + history.getEvent().getEvent());
                    getDataStorage(ActivityPDA.class.getSimpleName())
                            .setExtras(SETTING_STATUS_BAR, getStatusBar().getByteArray());
                }
            }
        }
    }


    private void showDialogAlarm(final History history) {
        mLog.Error(getClass(), "messageType:  " + history.getEvent().getEvent());
        if (!TextUtils.isEmpty(getEventContent(history.getEvent()))) {
            if (mFragmentAlarm == null) {
                mFragmentAlarm = new FragmentMessage();
                mFragmentAlarm.setComment(getEventContent(history.getEvent()));
                showDialogConfirm(getString(R.string.alarm_dialog_title), "", null,
                        mFragmentAlarm, false, new FragmentDialog.ListenerDialog() {
                            @Override
                            public boolean onButtonClick(int buttonID, Fragment content) {
                                switch (buttonID) {
                                    case FragmentDialog.BUTTON_ID_POSITIVE:
                                        return confirmAlarm(history);

                                    default:
                                        break;
                                }

                                return true;
                            }
                        });
            } else {
                mFragmentAlarm.setComment(getEventContent(history.getEvent()));
            }
        }
    }

    private void showNewSensorDialog(final History history) {
        FragmentInput fragmentInput = new FragmentInput();
        fragmentInput
                .setComment(getString(R.string.alarm_new_sensor));
        showNewSensorDialogConfirm("", "",
                "", fragmentInput, false, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        confirmNewSensorAlarm(history);
                        newSensorFlag = true;
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                sendNewSensorMessage(1);
                                break;
                            case FragmentDialog.BUTTON_ID_NEGATIVE:
                                FragmentDialog fragmentDialog = new FragmentDialog();
                                fragmentDialog.setTitle(getString(R.string.alarm_dialog_title));
                                fragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_POSITIVE,
                                        "");
                                fragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_NEGATIVE,
                                        "");

                                FragmentMessage mFragmentEnsure = new FragmentMessage();
                                mFragmentEnsure.setComment(getString(R.string.ensure));
                                fragmentDialog.setContent(mFragmentEnsure);
                                fragmentDialog.setCancelable(false);
                                fragmentDialog.setListener(new FragmentDialog.ListenerDialog() {
                                    @Override
                                    public boolean onButtonClick(int buttonID, Fragment content) {
                                        if (buttonID == FragmentDialog.BUTTON_ID_POSITIVE) {
                                            sendNewSensorMessage(0);
                                        }
                                        return true;
                                    }
                                });
                                fragmentDialog.show(getSupportFragmentManager(), null);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
    }

    private void showErrDialog(final History history, int errType) {
        if (!TextUtils.isEmpty(getEventContent(history.getEvent()))) {
            if (mFragmentAlarm == null) {
                mFragmentAlarm = new FragmentMessage();
                mFragmentAlarm.setComment(getEventContent(history.getEvent()));
                showErrConfirm(errType, getString(R.string.alarm_dialog_title), "", null,
                        mFragmentAlarm, new FragmentDialog.ListenerDialog() {
                            @Override
                            public boolean onButtonClick(int buttonID, Fragment content) {
                                switch (buttonID) {
                                    case FragmentDialog.BUTTON_ID_POSITIVE:
                                        return confirmAlarm(history);

                                    default:
                                        break;
                                }

                                return true;
                            }
                        });
            } else {
                mFragmentAlarm.setComment(getEventContent(history.getEvent()));
            }
        }
    }

    private void sendNewSensorMessage(int i) {
        handleMessage(new EntityMessage(
                ParameterGlobal.ADDRESS_LOCAL_VIEW,
                ParameterGlobal.ADDRESS_REMOTE_MASTER,
                ParameterGlobal.PORT_GLUCOSE,
                ParameterGlobal.PORT_GLUCOSE,
                EntityMessage.OPERATION_SET,
                ParameterGlucose.TASK_GLUCOSE_PARAM_NEW_SENSOR,
                new byte[]{(byte) i}));
        dismissDialogProgress();
    }


    private boolean confirmAlarm(final History history) {
        ArrayList<History> alertList = getStatusBar().getAlertList();

        if (alertList.size() > 0) {
            alertList.remove(alertList.size() - 1);

            getStatusBar().setAlertList(alertList);
        }

        getDataStorage(ActivityPDA.class.getSimpleName())
                .setExtras(SETTING_STATUS_BAR, getStatusBar().getByteArray());
        mLog.Error(getClass(), "报警数量：" + alertList.size());
        if (alertList.size() == 0) {
            triggerReaction(ParameterSystem.REACTION_NORMAL);
            mFragmentAlarm = null;
            return true;
        } else {
            if (alertList.get(alertList.size() - 1).getEvent().getEvent() > 0) {
                if (!TextUtils.isEmpty(getEventContent(alertList.get(alertList.size() - 1).getEvent()))) {
//                    if (alertList.get(alertList.size() - 1).getEvent().getEvent() == HYPO) {
//                        triggerReaction(ParameterSystem.REACTION_ALARM);
//                    } else {
//                        triggerReaction(ParameterSystem.REACTION_ALERT);
//                    }
                    if (alertList.get(alertList.size() - 1).getEvent().getEvent() == SENSOR_NEW) {
                        showNewSensorDialog(alertList.get(alertList.size() - 1));
                    } else {
                        showDialogAlarm(alertList.get(alertList.size() - 1));
                    }
                }
            }
            return false;
        }
    }

    private boolean confirmNewSensorAlarm(final History history) {
        ArrayList<History> alertList = getStatusBar().getAlertList();

        if (getStatusBar().getAlarm() != null) {
            getStatusBar().setAlarm(null);
            releaseWakeLock();
        } else {
            if (alertList.size() > 0) {
                alertList.remove(alertList.size() - 1);

                getStatusBar().setAlertList(alertList);
            }
        }

        getDataStorage(ActivityPDA.class.getSimpleName())
                .setExtras(SETTING_STATUS_BAR, getStatusBar().getByteArray());
        mLog.Error(getClass(), "报警数量：" + alertList.size());
        if (alertList.size() == 0) {
            triggerReaction(ParameterSystem.REACTION_NORMAL);
            mFragmentAlarm = null;
            return true;
        } else {
            if (alertList.get(alertList.size() - 1).getEvent().getEvent() > 0) {
//                triggerReaction(ParameterSystem.REACTION_ALERT);
                if (alertList.get(alertList.size() - 1).getEvent().getEvent() == SENSOR_NEW) {
                    showNewSensorDialog(alertList.get(alertList.size() - 1));
                } else {
                    showDialogAlarm(alertList.get(alertList.size() - 1));
                }
            }
            return false;
        }
    }


    private void triggerReaction(int reaction) {
        final int BEEP_ALARM_CYCLE = 28000;


        switch (reaction) {
            case ParameterSystem.REACTION_ALARM:
                MediaUtil.playMp3ByType(ActivityPDA.this, "beep_alarm.mp3",
                        false);

                final Handler handlerSound = new Handler();

                handlerSound.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getStatusBar().getAlarm() != null) {
                            handlerSound.postDelayed(this, BEEP_ALARM_CYCLE);
                            MediaUtil.playMp3ByType(ActivityPDA.this,
                                    "beep_alarm.mp3", false);
                        } else {
                            handlerSound.removeCallbacks(this);
                        }
                    }
                }, BEEP_ALARM_CYCLE);

                break;

            case ParameterSystem.REACTION_ALERT:
                MediaUtil.playMp3ByType(this, "beep_alert.mp3", false);
                break;

            case ParameterSystem.REACTION_NORMAL:
                break;

            default:
                return;
        }

        handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_SYSTEM,
                ParameterGlobal.PORT_SYSTEM, EntityMessage.OPERATION_SET,
                ParameterSystem.PARAM_REACTION,
                new ValueInt(reaction).getByteArray()));
    }


    protected void startactivity(Class<? extends Activity> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    protected void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    protected void showToast(String txt) {
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
    }

    protected void saveUserInfo(String userName, String accessId, String signKey, String userNo) {
        SPUtils.put(this, StringConstant.USERNAME, userName);
        SPUtils.put(this, StringConstant.ACCESSID, accessId);
        SPUtils.put(this, StringConstant.SIGNKEY, signKey);
        SPUtils.put(this, StringConstant.USERNO, userNo);
    }


    protected void showToast(String txt, int interval) {

        if (mToastLastShowString != txt) {
            mToastLastShowString = txt;
            mToastLastShowTime = 0;
        }

        long NowTime = System.currentTimeMillis();
        if (NowTime - mToastLastShowTime > interval) {
            mToastLastShowTime = NowTime;
            Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
        }
    }
}