package com.microtechmd.pda_app.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.monitor.History;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.ApplicationPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.LoginEntity;
import com.microtechmd.pda_app.fragment.FragmentBase;
import com.microtechmd.pda_app.fragment.FragmentCalibration;
import com.microtechmd.pda_app.fragment.FragmentHistory;
import com.microtechmd.pda_app.fragment.FragmentHome;
import com.microtechmd.pda_app.fragment.FragmentShop;
import com.microtechmd.pda_app.fragment.FragmentTrends;
import com.microtechmd.pda_app.manager.AppManager;
import com.microtechmd.pda_app.network.HttpHeader;
import com.microtechmd.pda_app.network.okhttp_util.OkHttpUtils;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.util.JSONUtils;
import com.microtechmd.pda_app.util.MD5Utils;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.BleUtil;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;

import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;

public class MainActivity extends ActivityPDA {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String USERINFO_GET = "userinfo_get";
    private static History history = null;

    private FragmentBase mFragmentHistory = null;
    private FragmentBase mFragmentTrends = null;
    private FragmentBase mFragmentHome = null;
    private FragmentBase mFragmentCalibration = null;
    private FragmentBase mFragmentShop = null;

    private BroadcastReceiver mBroadcastReceiver = null;

    private Handler mHandler = null;
    private RadioGroup radioGroup;


    public static History getStatus() {
        return history;
    }


    public static void setStatus(History history) {
        if (history == null) {
            MainActivity.history = null;
        } else {
            MainActivity.history = new History(history.getByteArray());
        }
    }

    public void switchContent(FragmentBase to) {
        MainActivity.this.getSupportFragmentManager()
                .beginTransaction()
                .hide(mFragmentHistory).commit();
        MainActivity.this.getSupportFragmentManager()
                .beginTransaction()
                .hide(mFragmentTrends).commit();
        MainActivity.this.getSupportFragmentManager()
                .beginTransaction()
                .hide(mFragmentHome).commit();
        MainActivity.this.getSupportFragmentManager()
                .beginTransaction()
                .hide(mFragmentCalibration).commit();
        MainActivity.this.getSupportFragmentManager()
                .beginTransaction()
                .hide(mFragmentShop).commit();
        MainActivity.this.getSupportFragmentManager()
                .beginTransaction()
                .show(to).commit();
    }

    public void add(FragmentBase from) {
        MainActivity.this.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.layout_fragment, from).commit();
    }

    private BluetoothLeDevice mBluetoothLeDevice;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SPUtils.put(this, RFSIGNAL, 0);
        if (mHandler == null) {
            mHandler = new Handler();
        }
//        updateTimer();
        mFragmentHistory = new FragmentHistory();
        mFragmentTrends = new FragmentTrends();
        mFragmentHome = new FragmentHome();
        mFragmentCalibration = new FragmentCalibration();
        mFragmentShop = new FragmentShop();

        add(mFragmentHistory);
        add(mFragmentTrends);
        add(mFragmentHome);
        add(mFragmentCalibration);
        add(mFragmentShop);

        switchContent(mFragmentHome);

        radioGroup = findViewById(R.id.radio_group_tab);
        radioGroup
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    FragmentBase fragMent = null;


                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (group.getCheckedRadioButtonId()) {
                            case R.id.radio_button_tab_history:

                                if (mFragmentHistory == null) {
                                    mFragmentHistory = new FragmentHistory();
                                }
                                fragMent = mFragmentHistory;

//                                ViseBle.getInstance().startScan(new ScanCallback(new IScanCallback() {
//                                    @Override
//                                    public void onDeviceFound(final BluetoothLeDevice bluetoothLeDevice) {
//                                        byte[] adr = new byte[]{29, 11, 0, 0, 0, 8};
//                                        byte[] address = Arrays.copyOfRange(bluetoothLeDevice.getScanRecord(),
//                                                bluetoothLeDevice.getScanRecord().length - 6, bluetoothLeDevice.getScanRecord().length);
//                                        if (Arrays.equals(adr, address)) {
//                                            mBluetoothLeDevice = bluetoothLeDevice;
//                                            Log.e(TAG, "onDeviceFound: " + Arrays.toString(address));
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
//                                    }
//
//                                    @Override
//                                    public void onScanTimeout() {
//                                    }
//                                }));
                                break;
                            case R.id.radio_button_tab_trends:

                                if (mFragmentTrends == null) {
                                    mFragmentTrends = new FragmentTrends();
                                }

                                fragMent = mFragmentTrends;

//                                ViseBle.getInstance().connect(mBluetoothLeDevice, new IConnectCallback() {
//                                    @Override
//                                    public void onConnectSuccess(DeviceMirror deviceMirror) {
//
//                                    }
//
//                                    @Override
//                                    public void onConnectFailure(BleException exception) {
//
//                                    }
//
//                                    @Override
//                                    public void onDisconnect(boolean isActive) {
//
//                                    }
//                                });
                                break;
                            case R.id.radio_button_tab_home:

                                if (mFragmentHome == null) {
                                    mFragmentHome = new FragmentHome();
                                }

                                fragMent = mFragmentHome;
//                                ViseBle.getInstance().disconnect(mBluetoothLeDevice);
                                break;

                            case R.id.radio_button_tab_calibration:

                                if (mFragmentCalibration == null) {
                                    mFragmentCalibration = new FragmentCalibration();
                                }

                                fragMent = mFragmentCalibration;
                                break;

                            case R.id.radio_button_tab_shop:

                                if (mFragmentShop == null) {
                                    mFragmentShop = new FragmentShop();
                                }

                                fragMent = mFragmentShop;
                                break;

                            default:
                                break;
                        }
                        switchContent(fragMent);
                    }
                });

        if (radioGroup.getCheckedRadioButtonId() < 0) {
            radioGroup.check(R.id.radio_button_tab_home);
        }

        ((ApplicationPDA) getApplication()).registerMessageListener(
                ParameterGlobal.PORT_GLUCOSE, mMessageListener);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                assert action != null;
                switch (action) {
                    case Intent.ACTION_SCREEN_ON:
                        onScreenOn();
                        break;
                    case Intent.ACTION_SCREEN_OFF:
                        onScreenOff();
                        break;
                    case USERINFO_GET:
                        getUserInfo();
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(USERINFO_GET);
        registerReceiver(mBroadcastReceiver, intentFilter);

        // 实例化Intent
        Intent intent = new Intent();
        // 设置Intent action属性
        intent.setAction(USERINFO_GET);
        // 实例化PendingIntent
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        // 进行闹铃注册
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert manager != null;
        manager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 10 * 60 * 60 * 1000,
                24 * 60 * 60 * 1000, sender);
    }


    private void getUserInfo() {
        try {
            String accessId = (String) SPUtils.get(this, StringConstant.ACCESSID, "");
            String signKey = (String) SPUtils.get(this, StringConstant.SIGNKEY, "");
            Map<String, Object> reqcontent = new LinkedHashMap<>();
            reqcontent.put("avatarurlflag", "1");
            reqcontent.put("imtokenflag", "1");
            reqcontent.put("authflag", "1");
            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim());
            String headerData = HttpHeader.getHeaderData(accessId, "userinfoGet", reqMd5);
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
                    .execute(new MyStringCallback(MainActivity.this) {
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getStatusBar().setGlucose(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkBluetoothPermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        radioGroup.check(R.id.radio_button_tab_home);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        ((ApplicationPDA) getApplication()).unregisterMessageListener(
                ParameterGlobal.PORT_GLUCOSE, mMessageListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AppManager.getAppManager().finishAllActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void handleNotification(final EntityMessage message) {
        super.handleNotification(message);
    }


    private void onScreenOn() {
        mLog.Debug(getClass(), "Screen on");

//        handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
//                ParameterGlobal.ADDRESS_REMOTE_SLAVE, ParameterGlobal.PORT_COMM,
//                ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
//                ParameterComm.PARAM_BROADCAST_OFFSET, new byte[]
//                {
//                        ParameterComm.BROADCAST_OFFSET_ALL
//                }));
    }

    private void onScreenOff() {
        mLog.Debug(getClass(), "Screen off");

//        handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
//                ParameterGlobal.ADDRESS_REMOTE_SLAVE, ParameterGlobal.PORT_COMM,
//                ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
//                ParameterComm.PARAM_BROADCAST_OFFSET, new byte[]
//                {
//                        ParameterComm.BROADCAST_OFFSET_STATUS
//                }));
    }

    /**
     * 检查蓝牙权限
     */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionManager.instance().with(this).request(new OnPermissionCallback() {
                    @Override
                    public void onRequestAllow(String permissionName) {
                        enableBluetooth();
                    }

                    @Override
                    public void onRequestRefuse(String permissionName) {
                        finish();
                    }

                    @Override
                    public void onRequestNoAsk(String permissionName) {
                        finish();
                    }
                }, Manifest.permission.ACCESS_COARSE_LOCATION);
            } else {
                enableBluetooth();
            }
        } else {
            enableBluetooth();
        }
    }

    public boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (networkProvider || gpsProvider) return true;
        return false;
    }

    private void enableBluetooth() {
        if (!BleUtil.isBleEnable(this)) {
            BleUtil.enableBluetooth(this, 1);
        } else {
            boolean isSupport = BleUtil.isSupportBle(this);
            if (!isSupport) {
                Toast.makeText(this, "该设备不支持蓝牙", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (!isLocationEnable(MainActivity.this)) {
                new AlertDialog.Builder(this)
                        .setTitle("定位服务")
                        .setMessage("您的手机可能需要开启定位服务才能搜索到设备。如果您确定身边有设备，却无法搜索到，请开启定位服务。")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //跳转到开启定位服务界面
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, 2);
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
            byte[] addressByte = SPUtils.getbytes(this, SETTING_RF_ADDRESS);
            if (addressByte != null) {
                handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                        ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                        ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                        ParameterComm.PARAM_RF_BROADCAST_SWITCH, new byte[]{1}));
                handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                        ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                        ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                        ParameterComm.PAIRFLAG, new byte[]{1}));
            }
//
//            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//
//            assert bluetoothManager != null;
//            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
//            mBluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
//                @Override
//                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
//                    byte[] adr = new byte[]{29, 11, 0, 0, 0, 8};
//                    byte[] address = Arrays.copyOfRange(scanRecord,
//                            scanRecord.length - 6, scanRecord.length);
//                    Log.e(TAG, "扫描结果：" + Arrays.toString(address));
//                    if (Arrays.equals(adr, address)) {
//                        //连接
//                        device.connectGatt(MainActivity.this, false, new BluetoothGattCallback() {
//                            @Override
//                            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                                super.onConnectionStateChange(gatt, status, newState);
//                                Log.e(TAG, "status：" + status + "newState:" + newState);
//                            }
//                        });
//                    }
//                }
//            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    enableBluetooth();
                } else if (resultCode == RESULT_CANCELED) {
                    finish();
                }
                break;
            case 2:
//                enableBluetooth();
            default:

                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
