package com.microtechmd.pda_app.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.ImageButton;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterGlucose;
import com.microtechmd.pda.library.entity.comm.RFAddress;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.fragment.FragmentDialog;
import com.microtechmd.pda_app.fragment.FragmentInput;
import com.microtechmd.pda_app.util.SpObjectListSaveUtil;
import com.microtechmd.pda_app.widget.StateButton;
import com.wyh.slideAdapter.ItemBind;
import com.wyh.slideAdapter.ItemView;
import com.wyh.slideAdapter.SlideAdapter;

import java.util.ArrayList;
import java.util.List;


public class DevicesMultiActivity extends ActivityPDA {
    private int mQueryStateTimeout = 0;
    private ImageButton back;
    private RecyclerView devices_lv;
    private StateButton next;

    private List<String> pairList;

    private boolean pairEventFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        pairList = (List<String>) SpObjectListSaveUtil.get(this, DEVICES_LIST);
        if (pairList == null) {
            pairList = new ArrayList<>();
        }

        back = findViewById(R.id.ibt_back);
        devices_lv = findViewById(R.id.devices_lv);
        devices_lv.setLayoutManager(new LinearLayoutManager(this));
        next = findViewById(R.id.button_next);

        initDevices();
        initClick();
    }

    @Override
    protected void handlePair(EntityMessage message) {
        super.handlePair(message);
        dismissDialogProgress();
    }

    @Override
    protected void handleUnPair(EntityMessage message) {
        super.handleUnPair(message);
        dismissDialogProgress();
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
                    }
                }
                dismissDialogProgress();
                break;

            default:
                break;
        }
    }

    private void ensureUnpair(final String data) {
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
                                if (pairList.contains(data)) {
                                    pairList.remove(data);
                                }
                                saveAndNotify();
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
        ItemBind<String> itemBind = new ItemBind<String>() {
            @Override
            public void onBind(final ItemView itemView, final String data, int position) {
                itemView.setText(R.id.item_time, data)
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
                                ensureUnpair(data);
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
                pairNewSensor();
            }
        });
    }

    private void pairNewSensor() {
        final FragmentInput fragmentInput = new FragmentInput();
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER,
                "");
        fragmentInput.setInputType(FragmentInput.POSITION_LEFT,
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        fragmentInput.setInputWidth(FragmentInput.POSITION_CENTER, 350);
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
                                    String adr = getAddress(new RFAddress(address).getByteArray());
                                    if (pairList.contains(adr.trim())) {
                                        showToast(R.string.paired_already);
                                        return false;
                                    } else {
                                        pairList.add(adr.trim());
                                        saveAndNotify();
                                    }
                                }
                                break;
                            default:
                                break;
                        }

                        return true;
                    }
                });
    }

    private void saveAndNotify() {
        SpObjectListSaveUtil.save(DevicesMultiActivity.this, DEVICES_LIST, pairList);
        devices_lv.getAdapter().notifyDataSetChanged();
    }

    private void checkState() {
    }

    private void pair(String addressString) {
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
