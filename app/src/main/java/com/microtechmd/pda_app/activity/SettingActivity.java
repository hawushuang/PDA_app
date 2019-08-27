package com.microtechmd.pda_app.activity;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.DbHistory;
import com.microtechmd.pda_app.entity.DbRawHistory;
import com.microtechmd.pda_app.entity.LoginEntity;
import com.microtechmd.pda_app.fragment.FragmentDialog;
import com.microtechmd.pda_app.fragment.FragmentInput;
import com.microtechmd.pda_app.manager.AppManager;
import com.microtechmd.pda_app.network.HttpHeader;
import com.microtechmd.pda_app.network.okhttp_util.OkHttpUtils;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.util.JSONUtils;
import com.microtechmd.pda_app.util.MD5Utils;
import com.microtechmd.pda_app.widget.WidgetSettingItem;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;

import static android.provider.ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY;
import static android.provider.MediaStore.Video.VideoColumns.LANGUAGE;
import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;
import static com.microtechmd.pda_app.fragment.FragmentSettings.REALTIMEFLAG;


public class SettingActivity extends ActivityPDA {
    private String accessId;
    private String signKey;

    private WidgetSettingItem modeSettingItem;
    private boolean realtimeFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        modeSettingItem = (WidgetSettingItem) findViewById(R.id.item_mode);
        accessId = (String) SPUtils.get(this, StringConstant.ACCESSID, "");
        signKey = (String) SPUtils.get(this, StringConstant.SIGNKEY, "");
        realtimeFlag = (boolean) SPUtils.get(this, REALTIMEFLAG, true);
        if (realtimeFlag) {
            modeSettingItem.setItemValue(getString(R.string.setting_general_mode_time));
        } else {
            modeSettingItem.setItemValue(getString(R.string.setting_general_mode_history));
        }
        updateVersion();
    }

    private void updateVersion() {
        //获取当前版本号getPackageName()是你当前类的包名，0代表是获取版本信息版本名称
        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String name = pi != null ? pi.versionName : null;
        if (TextUtils.isEmpty(name)) {
            return;
        }
        WidgetSettingItem settingItem = findViewById(R.id.item_software_version);
        if (settingItem != null) {
            settingItem.setItemValue(name);
        }
    }

    @Override
    protected void onClickView(View v) {
        super.onClickView(v);
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.item_user_profile:
                String userName = (String) SPUtils.get(this, StringConstant.USERNAME, "");
                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(this, R.string.unlogin, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, WelcomeActivity.class));
                } else {
                    startActivity(new Intent(this, UserProfileActivity.class));
                }
                break;
            case R.id.item_message:
                startActivity(new Intent(this, MessageTipsActivity.class));
                break;
            case R.id.item_pairing:
                startActivity(new Intent(this, DevicesActivity.class));
                break;
            case R.id.item_friend:
                startActivity(new Intent(this, FriendsActivity.class));
                break;
            case R.id.item_mode:
                setMode();
//                List<DbRawHistory> list = new ArrayList<>();
//                for (int i = 0; i < 10; i++) {
//                    DbRawHistory history = new DbRawHistory();
//                    history.setDevice_id("tb0009");
//                    history.setEvent_index(i - 10);
//                    list.add(history);
//                }
//
//                app.getBoxStore().boxFor(DbRawHistory.class).put(list);
                break;
            case R.id.item_language:
                setLanguage();
                break;
            case R.id.item_recovery:
                setRecovery();
                break;
            case R.id.button_logout:
                setLogout();
                break;

            default:
                break;
        }
    }

    private void setMode() {
        realtimeFlag = (boolean) SPUtils.get(this, REALTIMEFLAG, true);
        FragmentInput fragmentInput = new FragmentInput();
        if (realtimeFlag) {
            fragmentInput
                    .setComment(getString(R.string.setting_general_timemode_switch));
        } else {
            fragmentInput
                    .setComment(getString(R.string.setting_general_historymode_switch));
        }
        fragmentInput.setInputText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_RIGHT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT, null);
        showDialogConfirm(getString(R.string.setting_general_mode), "", "",
                fragmentInput, true, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                if (realtimeFlag) {
                                    handleMessage(
                                            new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                    ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                                                    ParameterGlobal.PORT_MONITOR,
                                                    ParameterGlobal.PORT_MONITOR,
                                                    EntityMessage.OPERATION_SET,
                                                    ParameterComm.BROADCAST_SAVA, new byte[]
                                                    {
                                                            (byte) 0
                                                    }));
                                    handleMessage(
                                            new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                    ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                    ParameterGlobal.PORT_MONITOR,
                                                    ParameterGlobal.PORT_MONITOR,
                                                    EntityMessage.OPERATION_SET,
                                                    ParameterComm.BROADCAST_SAVA, new byte[]
                                                    {
                                                            (byte) 0
                                                    }));
                                    realtimeFlag = false;
                                    modeSettingItem.setItemValue(getString(R.string.setting_general_mode_history));
                                    SPUtils.put(SettingActivity.this, COMMMESSAGETIPS, false);
                                } else {
                                    handleMessage(
                                            new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                    ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                                                    ParameterGlobal.PORT_MONITOR,
                                                    ParameterGlobal.PORT_MONITOR,
                                                    EntityMessage.OPERATION_SET,
                                                    ParameterComm.BROADCAST_SAVA, new byte[]
                                                    {
                                                            (byte) 1
                                                    }));
                                    handleMessage(
                                            new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                    ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                    ParameterGlobal.PORT_MONITOR,
                                                    ParameterGlobal.PORT_MONITOR,
                                                    EntityMessage.OPERATION_SET,
                                                    ParameterComm.BROADCAST_SAVA, new byte[]
                                                    {
                                                            (byte) 1
                                                    }));
                                    realtimeFlag = true;
                                    modeSettingItem.setItemValue(getString(R.string.setting_general_mode_time));
                                    SPUtils.put(SettingActivity.this, COMMMESSAGETIPS, true);
                                }
                                SPUtils.put(SettingActivity.this, REALTIMEFLAG, realtimeFlag);
                                break;
                            default:
                                break;
                        }

                        return true;
                    }
                });
    }

    private void setLogout() {
        FragmentInput fragmentInput = new FragmentInput();
        fragmentInput
                .setComment(getString(R.string.setting_general_logout));
        fragmentInput.setInputText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_RIGHT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT, null);
        showDialogConfirm(getString(R.string.logout), "", "",
                fragmentInput, false, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                logout();
                                break;

                            default:
                                break;
                        }

                        return true;
                    }
                });

    }

    private void logout() {
        try {
            Map<String, Object> reqcontent = new LinkedHashMap<>();
            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim());
            String headerData = HttpHeader.getHeaderData(accessId, "userLogout", reqMd5);
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
                    .execute(new MyStringCallback(SettingActivity.this) {
                        @Override
                        public void onResponse(String response, int id) {
                            super.onResponse(response, id);
                            if (TextUtils.isEmpty(response)) {
                                return;
                            }
                            LoginEntity authorModel = new Gson().fromJson(response, LoginEntity.class);
                            if (authorModel == null) {
                                return;
                            }
                            if (authorModel.getInfo().getCode() != 100000) {
                                showToast(authorModel.getInfo().getMsg());
                            } else {
                                app.setLoginEntity(null);
                                saveUserInfo("", "", "", "");
                                AppManager.getAppManager().finishAllActivity();
                                Intent i = getBaseContext().getPackageManager()
                                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                                assert i != null;
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }
                        }
                    });
//            ViseHttp.POST(Constant.APISERVICE)
//                    .addHeader("x-api-data", headerData)
//                    .addHeader("x-api-sign", headerSign)
//                    .setJson(JSONUtils.toJSON(reqcontent).trim())
//                    .request(new ACallback<LoginEntity>() {
//                        @Override
//                        public void onSuccess(LoginEntity authorModel) {
//                            if (authorModel == null) {
//                                return;
//                            }
//                            if (authorModel.getInfo().getCode() != 100000) {
//                                showToast(authorModel.getInfo().getMsg());
//                            } else {
//                                app.setLoginEntity(null);
//                                saveUserInfo("", "", "", "");
//                                AppManager.getAppManager().finishAllActivity();
//                                Intent i = getBaseContext().getPackageManager()
//                                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
//                                assert i != null;
//                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                startActivity(i);
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

    private void setLanguage() {
        FragmentInput fragmentInput = new FragmentInput();
        fragmentInput
                .setComment(getString(R.string.setting_general_language_switch));
        fragmentInput.setInputText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_RIGHT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT, null);
        showDialogConfirm(getString(R.string.setting_general_language), "", "",
                fragmentInput, false, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                Locale locale;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    locale = getResources().getConfiguration().getLocales().get(0);
                                } else {
                                    locale = getResources().getConfiguration().locale;
                                }
                                if (locale.getLanguage().equals("zh")) {
                                    updateLanguage(Locale.ENGLISH);
                                } else {
                                    updateLanguage(Locale.SIMPLIFIED_CHINESE);
                                }

                                break;

                            default:
                                break;
                        }

                        return true;
                    }
                });
    }

    private void updateLanguage(Locale locale) {
        Resources resources = getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();

        Configuration configuration = resources.getConfiguration();

        configuration.setLocale(locale);

        resources.updateConfiguration(configuration, metrics);


        saveLanguageSetting(this, locale);
//        finish();
//        startActivity(getIntent());

        AppManager.getAppManager().finishAllActivity();
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        assert i != null;
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }

    private static void saveLanguageSetting(Context context, Locale locale) {
        SPUtils.put(context, LANGUAGE, locale.getLanguage());
        SPUtils.put(context, COUNTRY, locale.getCountry());
    }

    private void setRecovery() {
        FragmentInput fragmentInput = new FragmentInput();
        showDialogConfirm(getString(R.string.recovery_pair), "",
                "", fragmentInput, true, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                recovery();
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
    }

    private void recovery() {
        handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                ParameterGlobal.PORT_MONITOR,
                ParameterGlobal.PORT_MONITOR,
                EntityMessage.OPERATION_SET,
                ParameterComm.CLEAN_DATABASES,
                null));

        SPUtils.clear(this);
        if (app != null) {
            app.getBoxStore().boxFor(DbHistory.class).removeAll();
            app.getBoxStore().boxFor(DbRawHistory.class).removeAll();
            app.setLoginEntity(null);
            app.setDataListAll(null);
        }
        AppManager.getAppManager().finishAllActivity();
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        assert i != null;
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
//        handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
//                ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_MONITOR,
//                ParameterGlobal.PORT_MONITOR, EntityMessage.OPERATION_SET,
//                ParameterComm.RESET_DATA,
//                new byte[]{(byte) 0}));
    }
}
