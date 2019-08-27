package com.microtechmd.pda_app.activity;


import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterGlucose;
import com.microtechmd.pda.library.entity.comm.RFAddress;
import com.microtechmd.pda.library.entity.comm.UserId;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.util.ListUtil;
import com.microtechmd.pda_app.widget.StateButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TransmitterSNEnterActivity extends ActivityPDA {
    private int mQueryStateTimeout = 0;
    private ImageButton back;
    private EditText transmitter_sn;
    private StateButton next;

    private String mRFAddress = "";
    private int lowGlucose;
    private int highGlucose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmittersn_enter);
        lowGlucose = (int) SPUtils.get(this, LOWGLUCOSE_SAVED, LOW_DEFAULT);
        highGlucose = (int) SPUtils.get(this, HIGHGLUCOSE_SAVED, HIGH_DEFAULT);

        back = findViewById(R.id.ibt_back);
        transmitter_sn = findViewById(R.id.transmitter_sn);
        next = findViewById(R.id.button_next);
        initClick();
    }

    private void initClick() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = transmitter_sn.getText().toString();
                if ((address.trim().length() != 6) ||
                        (address.trim().equals(RFAddress.RF_ADDRESS_UNPAIR))) {
                    Toast.makeText(TransmitterSNEnterActivity.this,
                            R.string.actions_pump_id_blank,
                            Toast.LENGTH_SHORT).show();
                } else {
                    pair(address);
//                    startactivity(SensorTransmitterActivity.class);
                }
            }
        });
    }

    @Override
    protected void handleAcknowledgement(EntityMessage message) {
        super.handleAcknowledgement(message);
        switch (message.getParameter()) {
            case ParameterComm.PARAM_RF_ADDRESS:
                if (message.getData()[0] == EntityMessage.FUNCTION_OK) {
                    checkState();
                }
                break;

            case ParameterGlucose.PARAM_BG_LIMIT:
            case ParameterGlucose.PARAM_FILL_LIMIT:
                if (message.getData()[0] == EntityMessage.FUNCTION_OK) {
                    dismissDialogProgress();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void handlePair(EntityMessage message) {
        super.handlePair(message);
        byte[] data = message.getData();
        dismissDialogProgress();
        mLog.Error(getClass(), "配对返回" + Arrays.toString(data));
        if (!(message.getData()[0] == EntityMessage.FUNCTION_OK)) {
            showToast(R.string.bluetooth_setting_match_failed);
            mRFAddress = RFAddress.RF_ADDRESS_UNPAIR;
            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                    ParameterComm.PARAM_RF_BROADCAST_SWITCH, new byte[]{0}));
            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                    ParameterComm.PAIRFLAG, new byte[]{0}));
        }
        if (mRFAddress.equals(RFAddress.RF_ADDRESS_UNPAIR)) {
            pair(RFAddress.RF_ADDRESS_UNPAIR);
        } else {
            showToast(R.string.bluetooth_setting_match_success);
            transmitter_sn.setText(mRFAddress.toUpperCase());

            startactivity(SensorTransmitterActivity.class);
            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                    ParameterComm.PARAM_RF_BROADCAST_SWITCH, new byte[]{1}));
            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                    ParameterComm.PAIRFLAG, new byte[]{1}));
//            handleMessage(new EntityMessage(
//                    ParameterGlobal.ADDRESS_LOCAL_VIEW,
//                    ParameterGlobal.ADDRESS_REMOTE_MASTER,
//                    ParameterGlobal.PORT_GLUCOSE,
//                    ParameterGlobal.PORT_GLUCOSE,
//                    EntityMessage.OPERATION_SET,
//                    ParameterGlucose.PARAM_BG_LIMIT,
//                    new ValueByte(lowGlucose).getByteArray()
//            ));
//            handleMessage(new EntityMessage(
//                    ParameterGlobal.ADDRESS_LOCAL_VIEW,
//                    ParameterGlobal.ADDRESS_REMOTE_MASTER,
//                    ParameterGlobal.PORT_GLUCOSE,
//                    ParameterGlobal.PORT_GLUCOSE,
//                    EntityMessage.OPERATION_SET,
//                    ParameterGlucose.PARAM_FILL_LIMIT,
//                    new ValueByte(highGlucose).getByteArray()
//            ));
        }
    }

    private void checkState() {
        int signal = (int) SPUtils.get(this, RFSIGNAL, 0);
        if (signal > 0) {
            mQueryStateTimeout = 0;
            mLog.Error(getClass(), "发送配对");
            String userId = (String) SPUtils.get(this, StringConstant.USERNO, "");
            byte[] id = new UserId(userId).getByteArray();
            dismissDialogProgress();
            handleMessage(new EntityMessage(
                    ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_REMOTE_MASTER,
                    ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM,
                    EntityMessage.OPERATION_PAIR,
                    ParameterComm.PARAM_USERID,
                    id));
        } else {
            if (mQueryStateTimeout < QUERY_STATE_TIMEOUT) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mQueryStateTimeout += QUERY_STATE_CYCLE;
                        checkState();
                    }
                }, QUERY_STATE_CYCLE);
            } else {
                mQueryStateTimeout = 0;
                pair(RFAddress.RF_ADDRESS_UNPAIR);
                dismissDialogProgress();
                showToast(R.string.connect_fail);
            }
        }
    }

    private void pair(String addressString) {
        showDialogProgress();
        mRFAddress = addressString;
        if (mRFAddress.equals(RFAddress.RF_ADDRESS_UNPAIR)) {
            SPUtils.putbytes(this, SETTING_RF_ADDRESS, null);
        } else {
            SPUtils.putbytes(this, SETTING_RF_ADDRESS, new RFAddress(mRFAddress).getByteArray());
        }

        handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                ParameterGlobal.ADDRESS_REMOTE_SLAVE, ParameterGlobal.PORT_COMM,
                ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                ParameterComm.PARAM_RF_ADDRESS,
                new RFAddress(addressString).getByteArray()));
        if (mRFAddress.equals(RFAddress.RF_ADDRESS_UNPAIR)) {
            dismissDialogProgress();
        }
    }

    public String getAddress(byte[] addressByte) {
        if (addressByte != null) {
            for (int i = 0; i < addressByte.length; i++) {
                if (addressByte[i] < 10) {
                    addressByte[i] += '0';
                } else {
                    addressByte[i] -= 10;
                    addressByte[i] += 'A';
                }
            }

            return new String(addressByte);
        } else {
            return "";
        }
    }
}
