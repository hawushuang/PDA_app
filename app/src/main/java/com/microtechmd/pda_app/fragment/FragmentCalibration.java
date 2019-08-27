package com.microtechmd.pda_app.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterGlucose;
import com.microtechmd.pda.library.entity.ValueByte;
import com.microtechmd.pda.library.entity.ValueShort;
import com.microtechmd.pda.library.entity.monitor.DateTime;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.ApplicationPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.activity.SettingActivity;
import com.microtechmd.pda_app.entity.CalibrationHistory;
import com.microtechmd.pda_app.util.SpObjectListSaveUtil;
import com.microtechmd.pda_app.widget.MainActionBar;
import com.triggertrap.seekarc.SeekArc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.microtechmd.pda_app.ActivityPDA.CALIBRATION_HISTORY;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE_UNIT_MG_STEP;


public class FragmentCalibration extends FragmentBase implements
        EntityMessage.Listener {
    private int mGlucose = 0;
    private EditText editTextGlucose;
    private MainActionBar mainActionBar;
    private View mRootView;

    private Button button_calibration;
    private Button button_record;
    private SeekArc mSeekArc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_calibration, container,
                false);
        editTextGlucose = mRootView.findViewById(R.id.edit_text_glucose);
        button_calibration = mRootView.findViewById(R.id.button_calibration);
        button_record = mRootView.findViewById(R.id.button_record);
        initActionBar();
        initialize();
        initClick();
        mSeekArc = mRootView.findViewById(R.id.seekArc);
        mSeekArc.setArcWidth(10);
        mSeekArc.setProgressWidth(10);
        mSeekArc.setArcColor(Color.rgb(66, 71, 91));
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
                        initialize();
                        Toast.makeText(getActivity(), R.string.calibration_over_err, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
//        mSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
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
//                DecimalFormat df = new DecimalFormat("0.0");
//                editTextGlucose.setText(df.format((float) progress / (float) 10));
//            }
//        });

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ApplicationPDA) getActivity().getApplication())
                .registerMessageListener(ParameterGlobal.PORT_MONITOR, this);
//        ((ApplicationPDA) getActivity().getApplication())
//                .registerMessageListener(ParameterGlobal.PORT_GLUCOSE, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ApplicationPDA) getActivity().getApplication())
                .unregisterMessageListener(ParameterGlobal.PORT_MONITOR, this);
//        ((ApplicationPDA) getActivity().getApplication())
//                .unregisterMessageListener(ParameterGlobal.PORT_GLUCOSE, this);
    }

    @Override
    public void onReceive(EntityMessage message) {
        switch (message.getOperation()) {
            case EntityMessage.OPERATION_SET:
                break;

            case EntityMessage.OPERATION_GET:
                break;

            case EntityMessage.OPERATION_EVENT:
                break;

            case EntityMessage.OPERATION_NOTIFY:
                break;

            case EntityMessage.OPERATION_ACKNOWLEDGE:
                handleAcknowledgement(message);
                break;

            default:
                break;
        }
    }

    private void initActionBar() {
        mainActionBar = mRootView.findViewById(R.id.toolbar);
        mainActionBar.setTitleText(R.string.activity_main_tab_calibration);
        mainActionBar.setRightButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });
    }

    private void initialize() {
        editTextGlucose.setEnabled(true);
        mGlucose = 5;
        mGlucose *= GLUCOSE_UNIT_MG_STEP;
        editTextGlucose.setText(((ActivityPDA) getActivity()).getGlucoseValue(mGlucose * 10, false));
    }

    private void initClick() {
        button_calibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalibrateConfirmDialog();
            }
        });
        button_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecordConfirmDialog();
            }
        });
    }

    private void showRecordConfirmDialog() {
        FragmentInput fragmentInput = new FragmentInput();
        fragmentInput
                .setComment(getString(R.string.calibrate_confirm));
        fragmentInput.setInputText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_RIGHT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT, null);
        showDialogConfirm(getString(R.string.record), "", "",
                fragmentInput, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                sendRecord();
                                break;

                            default:
                                break;
                        }

                        return true;
                    }
                });
    }

    private void showCalibrateConfirmDialog() {
        FragmentInput fragmentInput = new FragmentInput();
        fragmentInput
                .setComment(getString(R.string.record_calibrate_confirm));
        fragmentInput.setInputText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_RIGHT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT, null);
        showDialogConfirm(getString(R.string.fragment_calibrate), "", "",
                fragmentInput, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                sendCalibrate();
                                break;

                            default:
                                break;
                        }

                        return true;
                    }
                });
    }

    private void showDialogConfirm(String title, String buttonTextPositive,
                                   String buttonTextNegative, Fragment content,
                                   FragmentDialog.ListenerDialog listener) {
        final FragmentDialog fragmentDialog = new FragmentDialog();
        fragmentDialog.setTitle(title);
        fragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_POSITIVE,
                buttonTextPositive);
        fragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_NEGATIVE,
                buttonTextNegative);
        fragmentDialog.setContent(content);
        fragmentDialog.setListener(listener);
        fragmentDialog.show(getChildFragmentManager(), null);
    }

    private void sendCalibrate() {
        mGlucose = (int) (Float.parseFloat(editTextGlucose.getText().toString()) *
                100.0f);
        ValueShort value = new ValueShort((short) (mGlucose / GLUCOSE_UNIT_MG_STEP));
        DateTime dateTime = new DateTime(Calendar.getInstance());
        byte[] send = new byte[6];
        System.arraycopy(dateTime.getByteArray(), 0, send, 0, 4);
        System.arraycopy(value.getByteArray(), 0, send, 4, 2);
        ((ActivityPDA) getActivity()).handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                ParameterGlobal.ADDRESS_REMOTE_MASTER, ParameterGlobal.PORT_MONITOR,
                ParameterGlobal.PORT_GLUCOSE, EntityMessage.OPERATION_SET,
                ParameterGlucose.TASK_GLUCOSE_PARAM_CALIBRATON, send));
    }

    private void sendRecord() {
        mGlucose = (int) (Float.parseFloat(editTextGlucose.getText().toString()) *
                100.0f);
        ValueShort value = new ValueShort((short) (mGlucose / GLUCOSE_UNIT_MG_STEP));
        DateTime dateTime = new DateTime(Calendar.getInstance());
        byte[] send = new byte[6];
        System.arraycopy(dateTime.getByteArray(), 0, send, 0, 4);
        System.arraycopy(value.getByteArray(), 0, send, 4, 2);
        record();
        ((ActivityPDA) getActivity()).handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                ParameterGlobal.ADDRESS_REMOTE_MASTER, ParameterGlobal.PORT_MONITOR,
                ParameterGlobal.PORT_GLUCOSE, EntityMessage.OPERATION_SET,
                ParameterGlucose.TASK_GLUCOSE_PARAM_GLUCOSE, send));
    }

    protected void handleAcknowledgement(final EntityMessage message) {
        if ((message.getSourceAddress() == ParameterGlobal.ADDRESS_REMOTE_MASTER) &&
                (message.getSourcePort() == ParameterGlobal.PORT_GLUCOSE)) {
            if (message.getParameter() == ParameterGlucose.TASK_GLUCOSE_PARAM_CALIBRATON || message.getParameter() == ParameterGlucose.TASK_GLUCOSE_PARAM_CALIBRATON) {
                if (!(message.getData()[0] == EntityMessage.FUNCTION_OK)) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.calibrate_failed),
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
            }
            if (message.getParameter() == ParameterGlucose.TASK_GLUCOSE_PARAM_CALIBRATON) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.calibration_done), Toast.LENGTH_SHORT).show();
                record();
            }
            if (message.getParameter() == ParameterGlucose.TASK_GLUCOSE_PARAM_GLUCOSE) {
                Toast.makeText(getActivity(), getResources().getString(R.string.record_done), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void record() {
        List<CalibrationHistory> list = (List<CalibrationHistory>) SpObjectListSaveUtil.get(getActivity(), CALIBRATION_HISTORY);
        if (list == null) {
            list = new ArrayList<>();
        }
        long time = Calendar.getInstance().getTimeInMillis();
        float value = Float.parseFloat(editTextGlucose.getText().toString());
        CalibrationHistory calibrationHistory = new CalibrationHistory(time, value);
        list.add(calibrationHistory);
        SpObjectListSaveUtil.save(getActivity(), CALIBRATION_HISTORY, list);

        showDialogProgress();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                ((ActivityPDA) getActivity()).handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                        ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_MONITOR,
                        ParameterGlobal.PORT_MONITOR, EntityMessage.OPERATION_SET,
                        ParameterComm.RESET_DATA,
                        new byte[]{(byte) 1}));

                dismissDialogProgress();
            }
        }, 500);
    }

    private void showDialogProgress() {
        ((ActivityPDA) getActivity()).showDialogLoading();
    }


    private void dismissDialogProgress() {
        ((ActivityPDA) getActivity()).dismissDialogLoading();
    }
}
