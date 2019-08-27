package com.microtechmd.pda_app.activity;


import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterGlucose;
import com.microtechmd.pda.library.entity.ValueByte;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.util.RfAddressUtil;
import com.microtechmd.pda_app.widget.myswitchbutton.LukeSwitchButton;
import com.triggertrap.seekarc.SeekArc;
import com.vise.log.ViseLog;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;

import java.text.DecimalFormat;


public class MessageTipsActivity extends ActivityPDA {
    private static final int LOW = 1;
    private static final int HIGH = 2;
    private LukeSwitchButton comm_switchBtn;
    private TextView tv_low_glucose;
    private TextView tv_high_glucose;
    private int lowGlucose;
    private int highGlucose;

    private String mRFAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messagetips);

        lowGlucose = (int) SPUtils.get(this, LOWGLUCOSE_SAVED, LOW_DEFAULT);
        highGlucose = (int) SPUtils.get(this, HIGHGLUCOSE_SAVED, HIGH_DEFAULT);
        mRFAddress = RfAddressUtil.getAddress(SPUtils.getbytes(this, SETTING_RF_ADDRESS));
        tv_low_glucose = findViewById(R.id.tv_low_glucose);
        tv_high_glucose = findViewById(R.id.tv_high_glucose);
        comm_switchBtn = findViewById(R.id.comm_message_switch);
        initGlucose();
        initSwitchBtn();
    }

    @Override
    protected void handleAcknowledgement(EntityMessage message) {
        super.handleAcknowledgement(message);
        if (message.getSourceAddress() == ParameterGlobal.ADDRESS_REMOTE_MASTER) {
            if (message.getSourcePort() == ParameterGlobal.PORT_GLUCOSE) {
                if (message.getParameter() == ParameterGlucose.PARAM_FILL_LIMIT) {
                    if (!(message.getData()[0] == EntityMessage.FUNCTION_OK)) {
                        showToast(R.string.setting_failed);
//                        setHypo();
                        return;
                    }
                    SPUtils.put(MessageTipsActivity.this, LOWGLUCOSE_SAVED, lowGlucose);
                    initGlucose();
                    handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.PORT_MONITOR,
                            ParameterGlobal.PORT_MONITOR,
                            EntityMessage.OPERATION_SET,
                            ParameterGlucose.PARAM_FILL_LIMIT,
                            new ValueByte(lowGlucose).getByteArray()
                    ));
                }
                if (message.getParameter() == ParameterGlucose.PARAM_BG_LIMIT) {
                    if (!(message.getData()[0] == EntityMessage.FUNCTION_OK)) {
                        showToast(R.string.setting_failed);
//                        setHyper();
                        return;
                    }
                    SPUtils.put(MessageTipsActivity.this, HIGHGLUCOSE_SAVED, highGlucose);
                    initGlucose();
                    handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.PORT_MONITOR,
                            ParameterGlobal.PORT_MONITOR,
                            EntityMessage.OPERATION_SET,
                            ParameterGlucose.PARAM_BG_LIMIT,
                            new ValueByte(highGlucose).getByteArray()
                    ));
                }
            }
        }
    }

    private void initGlucose() {
        DecimalFormat df = new DecimalFormat("0.0");
        tv_low_glucose.setText(df.format((float) lowGlucose / (float) 10));
        tv_high_glucose.setText(df.format((float) highGlucose / (float) 10));
    }

    private void initSwitchBtn() {
        boolean comm_messageFlag = (boolean) SPUtils.get(this, COMMMESSAGETIPS, true);
        setSwitchState(comm_messageFlag, comm_switchBtn);
        setSwitchToggleChange(COMMMESSAGETIPS, comm_switchBtn);
    }


    private void setSwitchState(boolean state, LukeSwitchButton switchButton) {
        if (state) {
            switchButton.toggleOn();
        } else {
            switchButton.toggleOff();
        }
    }

    private void setSwitchToggleChange(final String key, LukeSwitchButton switchButton) {
        //开关切换事件
        switchButton.setOnToggleChanged(new LukeSwitchButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    SPUtils.put(MessageTipsActivity.this, key, true);
                } else {
                    SPUtils.put(MessageTipsActivity.this, key, false);
                }
            }
        });
    }

    @Override
    protected void onClickView(View v) {
        super.onClickView(v);

        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_urgent_low:
                showUrgentDialog();
                break;
            case R.id.rl_low_glucose:
                showGlucoseDialog(LOW);
                break;
            case R.id.rl_high_glucose:
                showGlucoseDialog(HIGH);
                break;
            default:
                break;
        }
    }

    private void showUrgentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Urgent Low (mmol/L)")
                .setPositiveButton(R.string.ok, null)
                .setMessage("Urgent Low (mmol/L) is preset at 3.1mmol/L and cannot be changed.  It will repeat every 30 minutes")
                .create().show();
    }

    private void showGlucoseDialog(final int glucoseFlag) {
        if (TextUtils.isEmpty(mRFAddress)) {
            showToast(R.string.null_pair);
            return;
        }
        boolean low_messageFlag = (boolean) SPUtils.get(this, LOMESSAGETIPS, true);
        boolean hi_messageFlag = (boolean) SPUtils.get(this, HIMESSAGETIPS, true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View lowView = LayoutInflater.from(this).inflate(R.layout.tips_alert_content, null);
        final TextView tv_item_name = lowView.findViewById(R.id.tv_item_name);
        final LukeSwitchButton switchButton = lowView.findViewById(R.id.message_switch);
        final EditText edit_text_glucose = lowView.findViewById(R.id.edit_text_glucose);
        SeekArc mSeekArc = lowView.findViewById(R.id.seekArc);
        mSeekArc.setArcWidth(45);
        mSeekArc.setProgressWidth(40);
        mSeekArc.setArcColor(Color.GRAY);
        mSeekArc.setMax(300);
        mSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onProgressChanged(SeekArc seekArc, int progress,
                                          boolean fromUser) {
                DecimalFormat df = new DecimalFormat("0.0");
                edit_text_glucose.setText(df.format((float) progress / (float) 10));
            }
        });

        Button ok = lowView.findViewById(R.id.btn_ok);
        Button cancel = lowView.findViewById(R.id.btn_cancel);

        switch (glucoseFlag) {
            case LOW:
                tv_item_name.setText(R.string.lowglucose_alert);
                setSwitchState(low_messageFlag, switchButton);
                mSeekArc.setProgress(lowGlucose);
                setSwitchToggleChange(LOMESSAGETIPS, switchButton);
                break;
            case HIGH:
                tv_item_name.setText(R.string.highglucose_alert);
                setSwitchState(hi_messageFlag, switchButton);
                mSeekArc.setProgress(highGlucose);
                setSwitchToggleChange(HIMESSAGETIPS, switchButton);
                break;
            default:

                break;
        }
        final AlertDialog dialog = builder.setCancelable(true).create();
        dialog.show();
        dialog.getWindow().setContentView(lowView);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //dialog.getWindow().setGravity(Gravity.CENTER);//可以设置显示的位置

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int glucose = (int) (Float.valueOf(edit_text_glucose.getText().toString().trim()) * 10);
                switch (glucoseFlag) {
                    case LOW:
                        if ((glucose > LOW_MAX) || (glucose < LOW_MIN)) {
                            showToast(R.string.fragment_settings_hypo_error);
                            return;
                        }

                        lowGlucose = glucose;
                        handleMessage(new EntityMessage(
                                ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                ParameterGlobal.ADDRESS_REMOTE_MASTER,
                                ParameterGlobal.PORT_GLUCOSE,
                                ParameterGlobal.PORT_GLUCOSE,
                                EntityMessage.OPERATION_SET,
                                ParameterGlucose.PARAM_FILL_LIMIT,
                                new ValueByte(lowGlucose).getByteArray()
                        ));

                        break;
                    case HIGH:
                        if ((glucose > HIGH_MAX) || (glucose < HIGH_MIN)) {
                            showToast(R.string.fragment_settings_hyper_error);
                            return;
                        }

                        highGlucose = glucose;
                        handleMessage(new EntityMessage(
                                ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                ParameterGlobal.ADDRESS_REMOTE_MASTER,
                                ParameterGlobal.PORT_GLUCOSE,
                                ParameterGlobal.PORT_GLUCOSE,
                                EntityMessage.OPERATION_SET,
                                ParameterGlucose.PARAM_BG_LIMIT,
                                new ValueByte(highGlucose).getByteArray()
                        ));
                        break;
                    default:

                        break;
                }
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}
