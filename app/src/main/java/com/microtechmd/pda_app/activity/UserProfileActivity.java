package com.microtechmd.pda_app.activity;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.liangmutian.mypicker.DatePickerDialog;
import com.example.liangmutian.mypicker.HeightPickerDialog;
import com.example.liangmutian.mypicker.WeightPickerDialog;
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
import com.microtechmd.pda_app.util.StringUtils;
import com.microtechmd.pda_app.widget.StateButton;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.MediaType;

import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;


public class UserProfileActivity extends ActivityPDA {
    private String activityFrom;
    public static final String MALE = "1";
    public static final String FEMALE = "2";

    private ImageButton back;
    private TextView skip;
    private StateButton next;
    //    private Spinner sex_spinner;
    private RadioGroup sex_switch_btn;
    private EditText tv_name;
    private EditText tv_email;
    private TextView tv_birthday;
    private TextView tv_height;
    private TextView tv_weight;

    private RelativeLayout rl_item_birthday;
    private RelativeLayout rl_item_height;
    private RelativeLayout rl_item_weight;
    private LoginEntity loginEntity;
    private String sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);
        activityFrom = getIntent().getStringExtra(StringConstant.ACTIVITYFROM);
        String[] tabTexts = {getResources().getString(R.string.male),
                getResources().getString(R.string.female),};
        sex = MALE;
        initViewId();
        if (app != null) {
            loginEntity = app.getLoginEntity();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tabTexts);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//        sex_spinner.setAdapter(adapter);
//        sex_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                switch (i) {
//                    case 0:
//                        sex = MALE;
//                        break;
//                    case 1:
//                        sex = FEMALE;
//                        break;
//                    default:
//
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        if (TextUtils.isEmpty(activityFrom)) {
            skip.setVisibility(View.GONE);
            next.setText(R.string.ok);
        } else {
            skip.setVisibility(View.VISIBLE);
            next.setText(R.string.next);
        }
        initUserProfile();
        initClick();
    }

    private void initViewId() {
        back = findViewById(R.id.ibt_back);
        skip = findViewById(R.id.text_view_right);
        next = findViewById(R.id.button_next);
//        sex_spinner = (Spinner) findViewById(R.id.sex_switch);
        sex_switch_btn = findViewById(R.id.sex_switch_btn);
        tv_name = findViewById(R.id.tv_item_name);
        tv_email = findViewById(R.id.tv_item_email);
        tv_birthday = findViewById(R.id.tv_item_birthday);
        tv_height = findViewById(R.id.tv_item_height);
        tv_weight = findViewById(R.id.tv_item_weight);
        rl_item_birthday = findViewById(R.id.rl_item_birthday);
        rl_item_height = findViewById(R.id.rl_item_height);
        rl_item_weight = findViewById(R.id.rl_item_weight);
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
                startactivity(SafetyWarningActivity.class);
            }
        });

        rl_item_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBirthday();
            }
        });
        rl_item_height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectHeight();
            }
        });
        rl_item_weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectWeight();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginEntity != null) {
                    modifyUserProfile();
                } else {
                    showToast(R.string.unlogin);
                }
            }
        });
//        sex_switch_btn.setOnStateChangeListener(new JellyToggleButton.OnStateChangeListener() {
//            @Override
//            public void onStateChange(float process, State state, JellyToggleButton jtb) {
//                if (state.equals(State.LEFT)) {
//                    sex = MALE;
//                }
//                if (state.equals(State.RIGHT)) {
//                    sex = FEMALE;
//                }
//            }
//        });
        sex_switch_btn.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.male:
                        sex = MALE;
                        break;
                    case R.id.femle:
                        sex = FEMALE;
                        break;
                    default:

                        break;
                }
            }
        });
    }

    private void selectBirthday() {
        int y = 2000, m = 1, d = 1;
        String date_str = tv_birthday.getText().toString().trim();
        if (!TextUtils.isEmpty(date_str)) {
            String[] date = date_str.split("-");
            y = Integer.parseInt(date[0]);
            m = Integer.parseInt(date[1]);
            d = Integer.parseInt(date[2]);
        }
        DatePickerDialog.Builder builder = new DatePickerDialog.Builder(this)
                .setSelectYear(y - 1)
                .setSelectMonth(m - 1)
                .setSelectDay(d - 1);
        builder.setOnDateSelectedListener(new DatePickerDialog.OnDateSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSelected(int[] dates) {
                tv_birthday.setText(StringUtils.unitFormat(dates[0]) + "-" +
                        StringUtils.unitFormat(dates[1]) + "-" +
                        StringUtils.unitFormat(dates[2]));
            }

            @Override
            public void onCancel() {
            }
        }).create().show();
    }

    private void selectHeight() {
        int h = 170;
        String h_str = tv_height.getText().toString().trim();
        if (!TextUtils.isEmpty(h_str)) {
            h = Integer.parseInt(h_str);
        }
        HeightPickerDialog.Builder builder = new HeightPickerDialog.Builder(this)
                .setSelectYear(h - 1);
        builder.setOnDateSelectedListener(new HeightPickerDialog.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int[] dates) {
                tv_height.setText(String.valueOf(dates[0]));
            }

            @Override
            public void onCancel() {
            }
        }).create().show();
    }

    private void selectWeight() {
        int w = 60;
        String h_str = tv_weight.getText().toString().trim();
        if (!TextUtils.isEmpty(h_str)) {
            w = Integer.parseInt(h_str);
        }
        WeightPickerDialog.Builder builder = new WeightPickerDialog.Builder(this)
                .setSelectYear(w - 1);
        builder.setOnDateSelectedListener(new WeightPickerDialog.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int[] dates) {
                tv_weight.setText(String.valueOf(dates[0]));
            }

            @Override
            public void onCancel() {
            }
        }).create().show();
    }

    @SuppressLint("SetTextI18n")
    private void initUserProfile() {
        if (loginEntity != null) {
            tv_name.setText(loginEntity.getContent().getName());
            tv_height.setText(loginEntity.getContent().getHeight());
            tv_weight.setText(loginEntity.getContent().getWeight());
            if (loginEntity.getContent().getSex().equals("1")) {
                sex = MALE;
//                sex_switch_btn.setChecked(false);
                sex_switch_btn.check(R.id.male);
            } else if (loginEntity.getContent().getSex().equals("2")) {
                sex = FEMALE;
//                sex_switch_btn.setChecked(true);
                sex_switch_btn.check(R.id.femle);
            }
            String birthday_str = loginEntity.getContent().getBirthday();
            if (!TextUtils.isEmpty(birthday_str) && birthday_str.length() >= 8) {
                tv_birthday.setText(birthday_str.substring(0, 4) + "-" +
                        birthday_str.substring(4, 6) + "-" +
                        birthday_str.substring(6, 8));
            }
        } else {
            showToast(R.string.unlogin);
        }
    }

    private void modifyUserProfile() {
        try {
            String accessId = (String) SPUtils.get(this, StringConstant.ACCESSID, "");
            String signKey = (String) SPUtils.get(this, StringConstant.SIGNKEY, "");
            final String name = tv_name.getText().toString().trim();
            final String birthday = tv_birthday.getText().toString().replace("-", "").trim();
            final String height = tv_height.getText().toString().trim();
            final String weight = tv_weight.getText().toString().trim();
            Map<String, Object> reqcontent = new LinkedHashMap<>();
            reqcontent.put("name", name);
            reqcontent.put("sex", sex);
            reqcontent.put("birthday", birthday);
            reqcontent.put("height", height);
            reqcontent.put("weight", weight);
            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim());
            String headerData = HttpHeader.getHeaderData(accessId, "userinfoModify", reqMd5);
            String headerSign = HttpHeader.getHeaderSign(headerData, signKey);
            String json = JSONUtils.toJSON(reqcontent).trim();

//            mLog.Error(getClass(), "headerSign:   " + headerSign);
            OkHttpUtils
                    .postString()
                    .url(HOST + APISERVICE_VERSION + "/" + Constant.APISERVICE)
                    .addHeader("x-api-data", headerData)
                    .addHeader("x-api-sign", headerSign)
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .content(json)
                    .build()
                    .execute(new MyStringCallback(UserProfileActivity.this) {
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
                            loginEntity.getContent().setName(name);
                            loginEntity.getContent().setSex(sex);
                            loginEntity.getContent().setBirthday(birthday);
                            loginEntity.getContent().setHeight(height);
                            loginEntity.getContent().setWeight(weight);
                            if (app != null) {
                                app.setLoginEntity(loginEntity);
                            }
                            if (authorModel.getInfo().getCode() != 100000) {
                                showToast(authorModel.getInfo().getMsg());
                            } else {
                                if (!TextUtils.isEmpty(activityFrom)) {
                                    startactivity(SafetyWarningActivity.class);
                                } else {
                                    finish();
                                }
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
//                            loginEntity.getContent().setName(name);
//                            loginEntity.getContent().setSex(sex);
//                            loginEntity.getContent().setBirthday(birthday);
//                            loginEntity.getContent().setHeight(height);
//                            loginEntity.getContent().setWeight(weight);
//                            if (app != null) {
//                                app.setLoginEntity(loginEntity);
//                            }
//                            if (authorModel.getInfo().getCode() != 100000) {
//                                showToast(authorModel.getInfo().getMsg());
//                            } else {
//                                if (!TextUtils.isEmpty(activityFrom)) {
//                                    startactivity(SafetyWarningActivity.class);
//                                } else {
//                                    finish();
//                                }
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
}
