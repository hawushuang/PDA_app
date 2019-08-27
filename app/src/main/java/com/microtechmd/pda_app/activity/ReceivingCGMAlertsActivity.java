package com.microtechmd.pda_app.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageButton;

import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.fragment.FragmentDialog;
import com.microtechmd.pda_app.fragment.FragmentInput;
import com.microtechmd.pda_app.widget.StateButton;


public class ReceivingCGMAlertsActivity extends ActivityPDA {
    private ImageButton back;
    private StateButton next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_cgm_alerts);

        back = findViewById(R.id.ibt_back);
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
                startactivity(DisturbActivity.class);
//                showReceivAlertDialog();
            }
        });
    }

    private void showReceivAlertDialog() {
        FragmentInput fragmentInput = new FragmentInput();
        fragmentInput
                .setComment(getString(R.string.allow_alert));
        fragmentInput.setInputText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_RIGHT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT, null);
        showDialogConfirm(getString(R.string.notification), getString(R.string.alllow), getString(R.string.not_alllow),
                fragmentInput, false, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                setAllowSendNotification(true);
                                break;
                            case FragmentDialog.BUTTON_ID_NEGATIVE:
                                setAllowSendNotification(false);
                                break;
                            default:
                                break;
                        }
                        startactivity(DisturbActivity.class);
                        return true;
                    }
                });

    }

    private void setAllowSendNotification(boolean flag) {
        SPUtils.put(ReceivingCGMAlertsActivity.this, COMMMESSAGETIPS, flag);
        SPUtils.put(ReceivingCGMAlertsActivity.this, LOMESSAGETIPS, flag);
        SPUtils.put(ReceivingCGMAlertsActivity.this, HIMESSAGETIPS, flag);
    }
}
