package com.microtechmd.pda_app.activity;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.BaseMsgEntity;
import com.microtechmd.pda_app.entity.LoginEntity;
import com.microtechmd.pda_app.network.HttpHeader;
import com.microtechmd.pda_app.network.okhttp_util.OkHttpUtils;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.util.CoreUtil;
import com.microtechmd.pda_app.util.JSONUtils;
import com.microtechmd.pda_app.util.MD5Utils;
import com.microtechmd.pda_app.widget.StateButton;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.MediaType;

import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;


public class RegistrationOrResetPwdActivity extends ActivityPDA {
    private String registFlag;
    private ImageButton back;
    private TextView skip;
    private TextView tv_name_tips;
    private EditText phoneNum, smsCode, et_username, et_password, et_repeat;
    private StateButton getSmsCode, next;

    private MyCountDownTimer myCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        TextView tv_titlebar = findViewById(R.id.text_view_title_bar);
        back = findViewById(R.id.ibt_back);
        skip = findViewById(R.id.text_view_right);
        tv_name_tips = findViewById(R.id.tv_name_tips);
        phoneNum = findViewById(R.id.et_phone_number);
        smsCode = findViewById(R.id.et_sms_code);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        et_repeat = findViewById(R.id.et_repeat);
        getSmsCode = findViewById(R.id.button_getsmscode);
        next = findViewById(R.id.button_next);

        registFlag = getIntent().getStringExtra(StringConstant.REGISTFLAG);
        if (registFlag.equals(StringConstant.REGISTRATION)) {
            tv_titlebar.setText(R.string.register);
            et_username.setVisibility(View.VISIBLE);
            tv_name_tips.setVisibility(View.VISIBLE);
        } else if (registFlag.equals(StringConstant.RESET_PWD)) {
            tv_titlebar.setText(R.string.pwd_forget);
            et_username.setVisibility(View.GONE);
            tv_name_tips.setVisibility(View.GONE);
        }
        myCountDownTimer = new MyCountDownTimer(60 * 1000, 1000);
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
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startactivity(MainActivity.class);
                finish();
            }
        });
        getSmsCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (registFlag.equals(StringConstant.REGISTRATION)) {
                    getSMSCode("userregcodeGet");
                } else if (registFlag.equals(StringConstant.RESET_PWD)) {
                    getSMSCode("userpwdfindcodeGet");
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (registFlag.equals(StringConstant.REGISTRATION)) {
                    registOrPwdReset("userRegister");
                } else if (registFlag.equals(StringConstant.RESET_PWD)) {
                    registOrPwdReset("userpwdReset");
                }
            }
        });
    }

    private void getSMSCode(String action) {
        try {
            String phone = phoneNum.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, R.string.input_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> reqcontent = new LinkedHashMap<>();
            if (action.equals("userregcodeGet")) {
                reqcontent.put("mobile", phone);
            } else if (action.equals("userpwdfindcodeGet")) {
                reqcontent.put("username", phone);
            }

            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim());
            String headerData = HttpHeader.getHeaderData(Constant.ACCESSID_USER, action, reqMd5);
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
                    .execute(new MyStringCallback(RegistrationOrResetPwdActivity.this) {
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

    private void registOrPwdReset(String action) {
        try {
            String mobile = phoneNum.getText().toString().trim();
            String userid = et_username.getText().toString().trim();
            String acode = smsCode.getText().toString().trim();
            String pwd = et_password.getText().toString().trim();
            String pwd_repeat = et_repeat.getText().toString().trim();

            if (!TextUtils.equals(pwd, pwd_repeat)) {
                showToast(R.string.password_different);
                return;
            }
            Map<String, Object> reqcontent = new LinkedHashMap<>();
            if (action.equals("userRegister")) {
                reqcontent.put("userid", userid);
                reqcontent.put("mobile", mobile);
            } else if (action.equals("userpwdReset")) {
                reqcontent.put("username", mobile);
            }

            reqcontent.put("pwd", CoreUtil.dealPwd(pwd));
            reqcontent.put("acode", acode);
            reqcontent.put("loginflag", "1");
            reqcontent.put("imtokenflag", "1");
            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim());
            String headerData = HttpHeader.getHeaderData(Constant.ACCESSID_USER, action, reqMd5);
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
                    .execute(new MyStringCallback(RegistrationOrResetPwdActivity.this) {
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
                                startactivity(LoginActivity.class);
                                finish();
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
//                                startactivity(LoginActivity.class);
//                                finish();
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
