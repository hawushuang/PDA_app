package com.microtechmd.pda_app.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.BaseMsgEntity;
import com.microtechmd.pda_app.entity.LoginEntity;
import com.microtechmd.pda_app.network.HttpHeader;
import com.microtechmd.pda_app.network.okhttp_util.OkHttpUtils;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.util.JSONUtils;
import com.microtechmd.pda_app.util.MD5Utils;
import com.microtechmd.pda_app.widget.StateButton;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;


public class LoginPhoneActivity extends ActivityPDA {
    private ImageButton back;
    private EditText phoneNum, smsCode;
    private StateButton getSmsCode, next;
    private TextView tv_tousername;

    private MyCountDownTimer myCountDownTimer;
    private boolean firstLoginFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);

        back = findViewById(R.id.ibt_back);
        phoneNum = findViewById(R.id.et_phone_number);
        smsCode = findViewById(R.id.et_sms_code);
        getSmsCode = findViewById(R.id.button_getsmscode);
        next = findViewById(R.id.button_next);
        tv_tousername = findViewById(R.id.tv_tousername);

        myCountDownTimer = new MyCountDownTimer(60 * 1000, 1000);
        firstLoginFlag = (boolean) SPUtils.get(this, StringConstant.FIRST_LOGIN, true);
        initClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
    }

    private void initClick() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getSmsCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSMSCode();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        tv_tousername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPhoneActivity.this,
                        LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getSMSCode() {
        try {
            String phone = phoneNum.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, R.string.input_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> reqcontent = new LinkedHashMap<>();
            reqcontent.put("username", phone);

            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim());
            String headerData = HttpHeader.getHeaderData(Constant.ACCESSID_USER, "userlogincodeGet", reqMd5);
            String headerSign = HttpHeader.getHeaderSign(headerData, Constant.ACCESSKEY_USER);
            String json = JSONUtils.toJSON(reqcontent).trim();

            OkHttpUtils
                    .postString()
                    .url(HOST + APISERVICE_VERSION + "/" + Constant.APISERVICE)
                    .addHeader("x-api-data", headerData)
                    .addHeader("x-api-sign", headerSign)
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .content(json)
                    .build()
                    .execute(new MyStringCallback(LoginPhoneActivity.this) {
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
                                myCountDownTimer.start();
                                showToast(R.string.ssdk_sms_top_identify_text);
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
//                            if (authorModel.getInfo().getCode() != 100000) {
//                                showToast(authorModel.getInfo().getMsg());
//                            } else {
//                                myCountDownTimer.start();
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

    private void login() {
        try {
            String mobile = phoneNum.getText().toString().trim();
            String acode = smsCode.getText().toString().trim();
            Map<String, Object> reqcontent = new LinkedHashMap<>();
            reqcontent.put("username", mobile);
            reqcontent.put("acode", acode);
            reqcontent.put("avatarurlflag", "1");
            reqcontent.put("imtokenflag", "1");
//                    reqcontent.put("authflag", "");
            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim()).trim();
            String headerData = HttpHeader.getHeaderData(Constant.ACCESSID_USER, "userLogin", reqMd5);
            String headerSign = HttpHeader.getHeaderSign(headerData, Constant.ACCESSKEY_USER);
            String json = JSONUtils.toJSON(reqcontent).trim();

            OkHttpUtils
                    .postString()
                    .url(HOST + APISERVICE_VERSION + "/" + Constant.APISERVICE)
                    .addHeader("x-api-data", headerData)
                    .addHeader("x-api-sign", headerSign)
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .content(json)
                    .build()
                    .execute(new MyStringCallback(LoginPhoneActivity.this) {
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
                                saveUserInfo(authorModel.getContent().getUserid(),
                                        authorModel.getContent().getAccessid(),
                                        authorModel.getContent().getAccesskey(),
                                        authorModel.getContent().getUserno());
                                if (app != null) {
                                    app.setLoginEntity(authorModel);
                                }
                                SPUtils.put(LoginPhoneActivity.this, StringConstant.FIRST_LOGIN, false);
                                if (firstLoginFlag) {
                                    Intent intent = new Intent(LoginPhoneActivity.this,
                                            UserProfileActivity.class);
                                    intent.putExtra(StringConstant.ACTIVITYFROM, StringConstant.LOGINACTIVITY);
                                    startActivity(intent);
                                } else {
                                    startactivity(MainActivity.class);
                                }
                                finish();
                            }
                        }
                    });

//                    ViseHttp.POST(Constant.APISERVICE)
//                            .addHeader("x-api-data", headerData)
//                            .addHeader("x-api-sign", headerSign)
//                            .setJson(JSONUtils.toJSON(reqcontent).trim())
//                            .request(new ACallback<LoginEntity>() {
//                                @Override
//                                public void onSuccess(LoginEntity authorModel) {
//                                    if (authorModel == null) {
//                                        return;
//                                    }
//                                    if (authorModel.getInfo().getCode() != 100000) {
//                                        showToast(authorModel.getInfo().getMsg());
//                                    } else {
//                                        saveUserInfo(authorModel.getContent().getUserid(),
//                                                authorModel.getContent().getAccessid(),
//                                                authorModel.getContent().getAccesskey(),
//                                                authorModel.getContent().getUserno());
//                                        if (app != null) {
//                                            app.setLoginEntity(authorModel);
//                                        }
//                                        SPUtils.put(LoginActivity.this, StringConstant.FIRST_LOGIN, false);
//                                        if (firstLoginFlag) {
//                                            Intent intent = new Intent(LoginActivity.this,
//                                                    UserProfileActivity.class);
//                                            intent.putExtra(StringConstant.ACTIVITYFROM, StringConstant.LOGINACTIVITY);
//                                            startActivity(intent);
//                                        } else {
//                                            startactivity(MainActivity.class);
//                                        }
//                                        finish();
//                                    }
//                                }
//
//                                @Override
//                                public void onFail(int errCode, String errMsg) {
//                                    showToast(R.string.toast_network_connection_failed);
//                                }
//                            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //验证码倒计时
    private class MyCountDownTimer extends CountDownTimer {

        MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        //计时过程
        @SuppressLint("SetTextI18n")
        @Override
        public void onTick(long l) {
            //防止计时过程中重复点击
            getSmsCode.setEnabled(false);
            getSmsCode.setText(l / 1000 + "  s");

        }

        //计时完毕的方法
        @Override
        public void onFinish() {
            //重新给Button设置文字
            getSmsCode.setText(R.string.get_sms_code);
            //设置可点击
            getSmsCode.setEnabled(true);
        }
    }
}
