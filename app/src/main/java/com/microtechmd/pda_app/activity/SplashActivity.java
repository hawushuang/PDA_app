package com.microtechmd.pda_app.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.ApplicationPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.LoginEntity;
import com.microtechmd.pda_app.network.HttpHeader;
import com.microtechmd.pda_app.network.okhttp_util.OkHttpUtils;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.util.JSONUtils;
import com.microtechmd.pda_app.util.MD5Utils;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;

import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;


public class SplashActivity extends ActivityPDA {
    private final int SPLASH_DISPLAY_LENGTH = 200;
    private Handler handler;
    private Runnable loginRunnable;
    private Runnable noneLoginRunnable;

    private ApplicationPDA app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_splash);
        app = (ApplicationPDA) getApplication();
        handler = new Handler();

        loginRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this,
                        MainActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        };
        noneLoginRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this,
                        WelcomeActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        };

        String accessId = (String) SPUtils.get(this, StringConstant.ACCESSID, "");
        String signKey = (String) SPUtils.get(this, StringConstant.SIGNKEY, "");
//        String accessId = "584bfbb19363be3975cd8be298e6700a";
//        String signKey = "N2U5Mzc2M2Y2YTU2YTg2YmUxMmUxYWJkNTJlMWZlYzI=";
        if (TextUtils.isEmpty(accessId) || TextUtils.isEmpty(signKey)) {
            handler.postDelayed(noneLoginRunnable, SPLASH_DISPLAY_LENGTH);
        } else {
            getUserInfo(accessId, signKey);
        }
    }

    private void getUserInfo(String accessId, String signKey) {
        try {
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
                    .execute(new MyStringCallback(SplashActivity.this) {
                        @Override
                        public void onBefore(Request request, int id) {
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            super.onError(call, e, id);
                            handler.postDelayed(loginRunnable, SPLASH_DISPLAY_LENGTH);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            super.onResponse(response, id);
                            if (TextUtils.isEmpty(response)) {
                                return;
                            }
                            Log.e("aaaa", response);
                            LoginEntity authorModel = new Gson().fromJson(response, LoginEntity.class);
                            if (authorModel == null) {
                                return;
                            }
                            if (authorModel.getInfo().getCode() != 100000) {
                                showToast(authorModel.getInfo().getMsg());
                                if (authorModel.getInfo().getCode() == 120014) {
                                    SplashActivity.this.finish();
                                    return;
                                }
                                handler.postDelayed(noneLoginRunnable, SPLASH_DISPLAY_LENGTH);
                            } else {
                                if (app != null) {
                                    app.setLoginEntity(authorModel);
                                }
                                handler.postDelayed(loginRunnable, SPLASH_DISPLAY_LENGTH);
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
//                                Toast.makeText(SplashActivity.this,
//                                        authorModel.getInfo().getMsg(), Toast.LENGTH_SHORT).show();
//                                handler.postDelayed(otherLoginRunnable, SPLASH_DISPLAY_LENGTH);
//                            } else {
//                                if (app != null) {
//                                    app.setLoginEntity(authorModel);
//                                }
//                                handler.postDelayed(loginRunnable, SPLASH_DISPLAY_LENGTH);
//                            }
//                        }
//
//                        @Override
//                        public void onFail(int errCode, String errMsg) {
//                            Toast.makeText(SplashActivity.this,
//                                    R.string.toast_network_connection_failed, Toast.LENGTH_SHORT).show();
//                            handler.postDelayed(loginRunnable, SPLASH_DISPLAY_LENGTH);
//                        }
//                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
