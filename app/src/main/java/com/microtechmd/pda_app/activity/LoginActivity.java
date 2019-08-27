package com.microtechmd.pda_app.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.LoginEntity;
import com.microtechmd.pda_app.network.HttpHeader;
import com.microtechmd.pda_app.network.okhttp_util.OkHttpUtils;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.util.CoreUtil;
import com.microtechmd.pda_app.util.JSONUtils;
import com.microtechmd.pda_app.util.MD5Utils;
import com.microtechmd.pda_app.widget.StateButton;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;

import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;


public class LoginActivity extends ActivityPDA {
    private ImageButton back;
    private EditText et_username, et_pwd;
    private StateButton next;
    private TextView forget;
    private TextView skip;

    private boolean firstLoginFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        back = findViewById(R.id.ibt_back);
        skip = findViewById(R.id.text_view_right);
        et_username = findViewById(R.id.et_username);
        et_pwd = findViewById(R.id.et_pwd);
        next = findViewById(R.id.button_next);
        forget = findViewById(R.id.forget);

        firstLoginFlag = (boolean) SPUtils.get(this, StringConstant.FIRST_LOGIN, true);
        initClick();
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
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String username = et_username.getText().toString().trim();
                    String pwd = et_pwd.getText().toString().trim();
                    Map<String, Object> reqcontent = new LinkedHashMap<>();
                    reqcontent.put("username", username);
                    reqcontent.put("pwd", CoreUtil.dealPwd(pwd));
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
                            .execute(new MyStringCallback(LoginActivity.this) {
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
                                        SPUtils.put(LoginActivity.this, StringConstant.FIRST_LOGIN, false);
                                        if (firstLoginFlag) {
                                            Intent intent = new Intent(LoginActivity.this,
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
        });
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,
                        RegistrationOrResetPwdActivity.class);
                intent.putExtra(StringConstant.REGISTFLAG, StringConstant.RESET_PWD);
                startActivity(intent);
            }
        });
    }
}
