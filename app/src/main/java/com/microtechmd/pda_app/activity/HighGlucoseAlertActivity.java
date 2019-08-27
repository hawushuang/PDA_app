package com.microtechmd.pda_app.activity;


import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.widget.StateButton;
import com.triggertrap.seekarc.SeekArc;

import java.text.DecimalFormat;


public class HighGlucoseAlertActivity extends ActivityPDA {
    private ImageButton back;
    private StateButton next;
    private EditText editTextGlucose;
    private DecimalFormat df;
    private int mGlucose;
    private SeekArc mSeekArc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highglucose_alert);
        df = new DecimalFormat("0.0");
        mGlucose = (int) SPUtils.get(this, HIGHGLUCOSE_SAVED, HIGH_DEFAULT);

        back = findViewById(R.id.ibt_back);
        next = findViewById(R.id.button_next);
        editTextGlucose = findViewById(R.id.edit_text_glucose);
        editTextGlucose.setText(df.format((float) mGlucose / (float) 10));
        mSeekArc = findViewById(R.id.seekArc);
        mSeekArc.setArcWidth(8);
        mSeekArc.setProgressWidth(8);
        mSeekArc.setArcColor(Color.rgb(66, 71, 91));
        mSeekArc.setProgressColor(Color.RED);
        mSeekArc.setMax(300);
        mSeekArc.setProgress(mGlucose);
        mSeekArc.setEnabled(false);
        editTextGlucose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String progress = editable.toString();
                if (!TextUtils.isEmpty(progress)) {
                    int glucose = (int) (Float.valueOf(progress) * GLUCOSE_UNIT_MG_STEP);
                    if (glucose <= 300) {
                        mSeekArc.setProgress(glucose);
                    } else {
                        editTextGlucose.setEnabled(true);
                        mGlucose = 120;
                        editTextGlucose.setText(getGlucoseValue(mGlucose * 10, false));
                        showToast(R.string.calibration_over_err);
                    }

                }
            }
        });

//        mSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
//
//            @Override
//            public void onStopTrackingTouch(SeekArc seekArc) {
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekArc seekArc) {
//            }
//
//            @Override
//            public void onProgressChanged(SeekArc seekArc, int progress,
//                                          boolean fromUser) {
//                editTextGlucose.setText(df.format((float) progress / (float) 10));
//                glucose = progress;
//            }
//        });
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
                mGlucose = (int) (Float.parseFloat(editTextGlucose.getText().toString()) *
                        10.0f);
                if ((mGlucose > HIGH_MAX) || (mGlucose < HIGH_MIN)) {
                    showToast(R.string.fragment_settings_hyper_error);
                    return;
                }
                SPUtils.put(HighGlucoseAlertActivity.this, HIGHGLUCOSE_SAVED, mGlucose);
                startactivity(ReceivingCGMAlertsActivity.class);
            }
        });
    }
}
