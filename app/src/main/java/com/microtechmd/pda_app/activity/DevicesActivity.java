package com.microtechmd.pda_app.activity;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterGlucose;
import com.microtechmd.pda.library.entity.ParameterMonitor;
import com.microtechmd.pda.library.entity.ValueInt;
import com.microtechmd.pda.library.entity.comm.RFAddress;
import com.microtechmd.pda.library.entity.comm.UserId;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.adapter.BaseViewHolder;
import com.microtechmd.pda_app.adapter.CommonAdapter;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.fragment.FragmentDialog;
import com.microtechmd.pda_app.fragment.FragmentInput;
import com.microtechmd.pda_app.util.ButtonClickUtil;
import com.microtechmd.pda_app.widget.StateButton;
import com.mylhyl.zxing.scanner.common.Scanner;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;
import com.wyh.slideAdapter.BottomListener;
import com.wyh.slideAdapter.FooterBind;
import com.wyh.slideAdapter.HeaderBind;
import com.wyh.slideAdapter.ItemBind;
import com.wyh.slideAdapter.ItemView;
import com.wyh.slideAdapter.SlideAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.microtechmd.pda_app.activity.ScannerActivity.REQUEST_CODE_SCANNER;


public class DevicesActivity extends ActivityPDA {
    private int mQueryStateTimeout = 0;
    private ImageButton back;
    private RecyclerView devices_lv;
    private StateButton next;
    private ImageButton ibt_right;
    private List<String> pairList;
    private CommonAdapter<String> mAdapter;
    private String mRFAddress;

    private boolean pairEventFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        pairList = new ArrayList<>();
        mRFAddress = getAddress(SPUtils.getbytes(this, SETTING_RF_ADDRESS));

        back = findViewById(R.id.ibt_back);
        devices_lv = findViewById(R.id.devices_lv);
        devices_lv.setLayoutManager(new LinearLayoutManager(this));
        next = findViewById(R.id.button_next);
        ibt_right = findViewById(R.id.ibt_right);

        if (!TextUtils.isEmpty(mRFAddress)) {
            pairList.add(mRFAddress);
            next.setVisibility(View.GONE);
        } else {
            next.setVisibility(View.VISIBLE);
        }
        initDevices();
        initClick();
    }

    @Override
    protected void handlePair(EntityMessage message) {
        super.handlePair(message);
        dismissDialogProgress();
        if (!(message.getData()[0] == EntityMessage.FUNCTION_OK)) {
            showToast(R.string.bluetooth_setting_match_failed);
            pair("");

            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                    ParameterComm.PARAM_RF_BROADCAST_SWITCH, new byte[]{0}));

            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                    ParameterComm.PAIRFLAG, new byte[]{0}));
        } else {
            showToast(R.string.bluetooth_setting_match_success);
            pairList.clear();
            pairList.add(mRFAddress.toUpperCase());
//            mAdapter.notifyDataSetChanged();

            devices_lv.getAdapter().notifyDataSetChanged();
            next.setVisibility(View.GONE);
            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                    ParameterComm.PARAM_RF_BROADCAST_SWITCH, new byte[]{1}));

            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                    ParameterComm.PAIRFLAG, new byte[]{1}));

            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_MONITOR,
                    ParameterGlobal.PORT_MONITOR, EntityMessage.OPERATION_SET,
                    ParameterComm.PAIR_SUCCESS, null));
        }
    }

    @Override
    protected void handleUnPair(EntityMessage message) {
        super.handleUnPair(message);
        dismissDialogProgress();
        if (message.getData()[0] == EntityMessage.FUNCTION_OK) {
            pair("");
            pairList.clear();
            devices_lv.getAdapter().notifyDataSetChanged();
            next.setVisibility(View.VISIBLE);

            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                    ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                    ParameterComm.PAIRFLAG, new byte[]{0}));
            handleMessage(
                    new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.PORT_MONITOR,
                            ParameterGlobal.PORT_MONITOR,
                            EntityMessage.OPERATION_SET,
                            ParameterMonitor.COUNTDOWNVIEW_VISIBLE,
                            new ValueInt(0).getByteArray()));

            handleMessage(
                    new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.PORT_MONITOR,
                            ParameterGlobal.PORT_MONITOR,
                            EntityMessage.OPERATION_SET,
                            ParameterMonitor.UNPAIR_CLEAN,
                            null));
        }
    }

    @Override
    protected void handleEvent(EntityMessage message) {
        super.handleEvent(message);
        switch (message.getEvent()) {
            case EntityMessage.EVENT_TIMEOUT:
                if (message.getSourcePort() == ParameterGlobal.PORT_COMM &&
                        message.getParameter() == ParameterComm.PARAM_USERID) {
                    if (pairEventFlag) {
                        pair("");
                        pairList.clear();
                        devices_lv.getAdapter().notifyDataSetChanged();
                        next.setVisibility(View.VISIBLE);
                    }
                }
                dismissDialogProgress();
                break;

            default:
                break;
        }
    }

    private void ensureUnpair() {
        FragmentInput fragmentInput = new FragmentInput();
        fragmentInput
                .setComment(getString(R.string.force_unpair_info));
        fragmentInput.setInputText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_RIGHT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT, null);
        showDialogConfirm(getString(R.string.fragment_settings_force_unpair), "", "",
                fragmentInput, true, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                pair("");
                                pairList.clear();
                                devices_lv.getAdapter().notifyDataSetChanged();
                                next.setVisibility(View.VISIBLE);
                                handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                        ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                                        ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                                        ParameterComm.PAIRFLAG, new byte[]{0}));

                                handleMessage(
                                        new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                ParameterGlobal.PORT_MONITOR,
                                                ParameterGlobal.PORT_MONITOR,
                                                EntityMessage.OPERATION_SET,
                                                ParameterMonitor.COUNTDOWNVIEW_VISIBLE,
                                                new ValueInt(0).getByteArray()));
                                break;
                            default:
                                break;
                        }

                        return true;
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
                break;

            default:
                break;
        }
    }

    private void initDevices() {
//        mAdapter = new CommonAdapter<String>(this, R.layout.item_device, pairList) {
//            @Override
//            public void convert(BaseViewHolder holder, final int position) {
//                holder.setText(R.id.item_time, mDatas.get(position));
//                holder.setText(R.id.item_operation, R.string.unpair);
//                holder.getView(R.id.item_operation).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        handleMessage(new EntityMessage(
//                                ParameterGlobal.ADDRESS_LOCAL_VIEW,
//                                ParameterGlobal.ADDRESS_REMOTE_MASTER,
//                                ParameterGlobal.PORT_COMM,
//                                ParameterGlobal.PORT_COMM,
//                                EntityMessage.OPERATION_UNPAIR,
//                                ParameterComm.PARAM_USERID,
//                                new byte[]{}));
//                        pairEventFlag = false;
////                        pair("");
//                    }
//                });
//            }
//        };
////        //添加Android自带的分割线
////        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
//        devices_lv.setAdapter(mAdapter);
////        mAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
////            @Override
////            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
////                DevicesActivity.this.startActivity(new Intent(DevicesActivity.this,
////                        UserProfileActivity.class));
////            }
////        });


        ItemBind<String> itemBind = new ItemBind<String>() {
            @Override
            public void onBind(final ItemView itemView, String data, int position) {
                itemView.setText(R.id.item_time, data)
//                        .setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Toast.makeText(HomeActivity.this, "click", Toast.LENGTH_SHORT).show();
//                            }
//                        })
                        .setOnClickListener(R.id.upair, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                handleMessage(new EntityMessage(
                                        ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                        ParameterGlobal.ADDRESS_REMOTE_MASTER,
                                        ParameterGlobal.PORT_COMM,
                                        ParameterGlobal.PORT_COMM,
                                        EntityMessage.OPERATION_UNPAIR,
                                        ParameterComm.PARAM_USERID,
                                        new byte[]{}));
                                pairEventFlag = false;
                            }
                        })
                        .setOnClickListener(R.id.delete, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ensureUnpair();
                                itemView.closeMenu();
                            }
                        });
            }
        };
        SlideAdapter.load(pairList)
                .item(R.layout.item_device, 0, 0, R.layout.menu, 0.4f)
                .padding(1)
                .bind(itemBind)
                .into(devices_lv);
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
                if (ButtonClickUtil.isFastClick()) {
                    pairNewSensor();
                }
//                DevicesActivity.this.startActivity(new Intent(DevicesActivity.this,
//                        TransmitterSNEnterActivity.class));
//                finish();
            }
        });
        ibt_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(DevicesActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    PermissionManager.instance().with(DevicesActivity.this).request(new OnPermissionCallback() {
                        @Override
                        public void onRequestAllow(String permissionName) {
                            DevicesActivity.this.startActivityForResult(
                                    new Intent(DevicesActivity.this, ScannerActivity.class),
                                    REQUEST_CODE_SCANNER);
                        }

                        @Override
                        public void onRequestRefuse(String permissionName) {
                            showToast(R.string.need_camera_permissions);
                        }

                        @Override
                        public void onRequestNoAsk(String permissionName) {
                            showToast(R.string.need_camera_permissions);
                        }
                    }, Manifest.permission.CAMERA);
                } else {
                    DevicesActivity.this.startActivityForResult(
                            new Intent(DevicesActivity.this, ScannerActivity.class),
                            REQUEST_CODE_SCANNER);
                }
            }
        });
    }

    private void pairNewSensor() {
        final FragmentInput fragmentInput = new FragmentInput();
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER,
                mRFAddress);
        fragmentInput.setInputType(FragmentInput.POSITION_LEFT,
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        fragmentInput.setInputWidth(FragmentInput.POSITION_CENTER, 450);
        showDialogConfirm(getString(R.string.fragment_settings_pairing), "",
                "", fragmentInput, true, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                String address = fragmentInput.getInputText(
                                        FragmentInput.POSITION_CENTER);
                                if ((address.trim().length() != 6) ||
                                        (address.trim()
                                                .equals(RFAddress.RF_ADDRESS_UNPAIR))) {
                                    showToast(R.string.actions_pump_id_blank);
                                    return false;
                                } else {
                                    showDialogProgress();
                                    pair(address.trim());
                                }
                                break;
                            default:
                                break;
                        }

                        return true;
                    }
                });
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
            pairEventFlag = true;
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
                pair("");
                dismissDialogProgress();
                showToast(R.string.connect_fail);
            }
        }
    }

    private void pair(String addressString) {
        mRFAddress = addressString;
        if (TextUtils.isEmpty(mRFAddress)) {
            SPUtils.putbytes(this, SETTING_RF_ADDRESS, null);
            dismissDialogProgress();
        } else {
            SPUtils.putbytes(this, SETTING_RF_ADDRESS, new RFAddress(mRFAddress).getByteArray());
        }

        handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                ParameterGlobal.ADDRESS_REMOTE_SLAVE, ParameterGlobal.PORT_COMM,
                ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_SET,
                ParameterComm.PARAM_RF_ADDRESS,
                new RFAddress(addressString).getByteArray()));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SCANNER) {
                if (data != null) {
                    String stringExtra = data.getStringExtra(Scanner.Scan.RESULT);
                    if ((stringExtra.trim().length() != 6) ||
                            (stringExtra.trim()
                                    .equals(RFAddress.RF_ADDRESS_UNPAIR))) {
                        showToast(R.string.actions_pump_id_blank);
                    } else {
                        showDialogProgress();
                        pair(stringExtra.trim());
                    }
                }
            }
        }
    }
}
