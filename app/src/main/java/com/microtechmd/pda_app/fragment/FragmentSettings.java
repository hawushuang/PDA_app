package com.microtechmd.pda_app.fragment;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterGlucose;
import com.microtechmd.pda.library.entity.ValueShort;
import com.microtechmd.pda.library.entity.comm.RFAddress;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.util.DataCleanUtil;
import com.microtechmd.pda_app.widget.WidgetSettingItem;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.UUID;

import static android.provider.ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY;
import static android.provider.MediaStore.Video.VideoColumns.LANGUAGE;


public class FragmentSettings extends FragmentBase {
    private static final String TAG = FragmentSettings.class.getSimpleName();
    private static final UUID UUID_SERVICE =
            UUID.fromString("0000F000-0000-1000-8000-00805F9B34FB");
    private static final UUID UUID_CHARACTERISTIC =
            UUID.fromString("0000F001-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice bluetoothDevice;

    public static final String SETTING_HYPER = "hyper";
    public static final String SETTING_HYPO = "hypo";
    public static final String REALTIMEFLAG = "realtimeFlag";

    private static final int HYPER_DEFAULT = 1200;
    private static final int HYPER_MAX = 2500;
    private static final int HYPER_MIN = 800;
    private static final int HYPO_DEFAULT = 350;
    private static final int HYPO_MAX = 500;
    private static final int HYPO_MIN = 200;

    private boolean mIsProgressNotShow = false;
    private boolean mIsProgressNotDismiss = false;

    private boolean realtimeFlag = true;
    private static final int QUERY_STATE_CYCLE = 1000;
    private static final int QUERY_STATE_TIMEOUT = 10000;

    private boolean mIsRFStateChecking = false;
    private int mQueryStateTimeout = 0;
    private int mHyper = HYPER_DEFAULT;
    private int mHypo = HYPO_DEFAULT;
    private String mRFAddress = "";
    private View mRootView = null;

    private WidgetSettingItem modeSettingItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.activity_settings, container, false);

        modeSettingItem = (WidgetSettingItem) mRootView.findViewById(R.id.item_mode);
        realtimeFlag = (boolean) SPUtils.get(getActivity(), REALTIMEFLAG, true);
        if (realtimeFlag) {
            modeSettingItem.setItemValue(getString(R.string.setting_general_mode_time));
        } else {
            modeSettingItem.setItemValue(getString(R.string.setting_general_mode_history));
        }
        mRFAddress = getAddress(((ActivityPDA) getActivity())
                .getDataStorage(ActivityPDA.class.getSimpleName())
                .getExtras(ActivityPDA.SETTING_RF_ADDRESS, null));
        ((WidgetSettingItem) mRootView.findViewById(R.id.item_pairing))
                .setItemValue(mRFAddress);
        mHyper = ((ActivityPDA) getActivity())
                .getDataStorage(FragmentSettings.class.getSimpleName())
                .getInt(SETTING_HYPER, HYPER_DEFAULT);
        mHypo = ((ActivityPDA) getActivity())
                .getDataStorage(FragmentSettings.class.getSimpleName())
                .getInt(SETTING_HYPO, HYPO_DEFAULT);
        updateHyper(mHyper);
        updateHypo(mHypo);

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        EventBus.getDefault().register(this);
    }


    @Override
    public void onDestroyView() {
//        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }


    private void pair(String addressString) {
        mRFAddress = addressString;
        if (mRFAddress.equals(RFAddress.RF_ADDRESS_UNPAIR)) {
            ((ActivityPDA) getActivity())
                    .getDataStorage(
                            ActivityPDA.class.getSimpleName())
                    .setExtras(ActivityPDA.SETTING_RF_ADDRESS, null);
        } else {
            ((ActivityPDA) getActivity())
                    .getDataStorage(
                            ActivityPDA.class.getSimpleName())
                    .setExtras(ActivityPDA.SETTING_RF_ADDRESS,
                            new RFAddress(mRFAddress).getByteArray());
        }
    }


    private void checkRFState() {
        mLog.Debug(getClass(), "Check RF state");
        if (getActivity() != null) {
            ((ActivityPDA) getActivity()).handleMessage(
                    new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                            ParameterGlobal.PORT_COMM, ParameterGlobal.PORT_COMM,
                            EntityMessage.OPERATION_GET, ParameterComm.PARAM_RF_STATE,
                            null));
        }
    }

    private void showDialogProgress() {
        ((ActivityPDA) getActivity()).showDialogProgress();
    }

    private void dismissDialogProgress() {
        if (!mIsProgressNotDismiss) {
            ((ActivityPDA) getActivity()).dismissDialogProgress();
        }
    }


    private void showDialogConfirm(String title, String buttonTextPositive,
                                   String buttonTextNegative, Fragment content,
                                   FragmentDialog.ListenerDialog listener) {
        FragmentDialog fragmentDialog = new FragmentDialog();
        fragmentDialog.setTitle(title);
        fragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_POSITIVE,
                buttonTextPositive);
        fragmentDialog.setButtonText(FragmentDialog.BUTTON_ID_NEGATIVE,
                buttonTextNegative);
        fragmentDialog.setContent(content);
        fragmentDialog.setListener(listener);
        fragmentDialog.show(getChildFragmentManager(), null);
    }

    private void setTransmitterID() {
        final FragmentInput fragmentInput = new FragmentInput();

        if ((mRFAddress.equals("")) ||
                (mRFAddress.equals(RFAddress.RF_ADDRESS_UNPAIR))) {
            fragmentInput.setInputText(FragmentInput.POSITION_CENTER,
                    "");
            fragmentInput.setInputType(FragmentInput.POSITION_LEFT,
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            fragmentInput.setInputWidth(FragmentInput.POSITION_CENTER, 210);
            showDialogConfirm(getString(R.string.fragment_settings_pairing), "",
                    "", fragmentInput, new FragmentDialog.ListenerDialog() {
                        @Override
                        public boolean onButtonClick(int buttonID, Fragment content) {
                            switch (buttonID) {
                                case FragmentDialog.BUTTON_ID_POSITIVE:
                                    String address = fragmentInput.getInputText(
                                            FragmentInput.POSITION_CENTER);

                                    if ((address.trim().length() != 6) ||
                                            (address.trim()
                                                    .equals(RFAddress.RF_ADDRESS_UNPAIR))) {
                                        Toast.makeText(getActivity(),
                                                R.string.actions_pump_id_blank,
                                                Toast.LENGTH_SHORT).show();
                                        return false;
                                    } else {
                                        mIsProgressNotDismiss = true;
//                                        showDialogProgress();
                                        pair(address.trim());
                                    }

                                    break;

                                default:
                                    break;
                            }

                            return true;
                        }
                    });
        } else {
            fragmentInput
                    .setComment(getString(R.string.fragment_settings_unpair));
            showDialogConfirm(getString(R.string.fragment_settings_pairing), "",
                    "", fragmentInput, new FragmentDialog.ListenerDialog() {
                        @Override
                        public boolean onButtonClick(int buttonID, Fragment content) {
                            switch (buttonID) {
                                case FragmentDialog.BUTTON_ID_POSITIVE:
                                    mRFAddress = RFAddress.RF_ADDRESS_UNPAIR;
                                    mIsProgressNotDismiss = true;
                                    showDialogProgress();
                                    ((ActivityPDA) getActivity())
                                            .handleMessage(new EntityMessage(
                                                    ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                    ParameterGlobal.ADDRESS_REMOTE_MASTER,
                                                    ParameterGlobal.PORT_COMM,
                                                    ParameterGlobal.PORT_COMM,
                                                    EntityMessage.OPERATION_SET,
                                                    ParameterComm.PARAM_RF_REMOTE_ADDRESS,
                                                    new RFAddress(mRFAddress)
                                                            .getByteArray()));
                                    break;

                                default:
                                    break;
                            }
                            return true;
                        }
                    });
        }
    }

    private void setMode() {
        realtimeFlag = (boolean) SPUtils.get(getActivity(), REALTIMEFLAG, true);
        FragmentInput fragmentInput = new FragmentInput();
        if (realtimeFlag) {
            fragmentInput
                    .setComment(getString(R.string.setting_general_timemode_switch));
        } else {
            fragmentInput
                    .setComment(getString(R.string.setting_general_historymode_switch));
        }
        fragmentInput.setInputText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_RIGHT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT, null);
        showDialogConfirm(getString(R.string.setting_general_mode), "", "",
                fragmentInput, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                if (realtimeFlag) {
                                    realtimeFlag = false;
                                    modeSettingItem.setItemValue(getString(R.string.setting_general_mode_history));
                                } else {
                                    realtimeFlag = true;
                                    modeSettingItem.setItemValue(getString(R.string.setting_general_mode_time));
                                }
                                SPUtils.put(getActivity(), REALTIMEFLAG, realtimeFlag);
                                break;
                            default:
                                break;
                        }

                        return true;
                    }
                });
    }

    private void setHyper() {
        mHyper = ((ActivityPDA) getActivity())
                .getDataStorage(FragmentSettings.class.getSimpleName())
                .getInt(SETTING_HYPER, HYPER_DEFAULT);
        final FragmentInput fragmentInput = new FragmentInput();
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER,
                new DecimalFormat("0.0").format((double) mHyper / 100));
        fragmentInput.setInputType(FragmentInput.POSITION_CENTER,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT,
                getString(R.string.unit_mmol_l));
        showDialogConfirm(getString(R.string.fragment_settings_hi_bg_threshold),
                "", "", fragmentInput, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                mHyper = (int) (Float.parseFloat(fragmentInput
                                        .getInputText(FragmentInput.POSITION_CENTER)) * 100.0f);

                                if ((mHyper > HYPER_MAX) || (mHyper < HYPER_MIN)) {
                                    Toast.makeText(getActivity(),
                                            R.string.fragment_settings_hyper_error,
                                            Toast.LENGTH_SHORT).show();
                                    return false;
                                } else {
                                    if ((!mRFAddress.equals("")) && (!mRFAddress
                                            .equals(RFAddress.RF_ADDRESS_UNPAIR))) {
                                        ((ActivityPDA) getActivity())
                                                .handleMessage(new EntityMessage(
                                                        ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                        ParameterGlobal.ADDRESS_REMOTE_MASTER,
                                                        ParameterGlobal.PORT_GLUCOSE,
                                                        ParameterGlobal.PORT_GLUCOSE,
                                                        EntityMessage.OPERATION_SET,
                                                        ParameterGlucose.PARAM_BG_LIMIT,
                                                        new ValueShort((short) mHyper)
                                                                .getByteArray()));
                                    } else {
                                        Toast.makeText(getActivity(),
                                                R.string.connect_fail,
                                                Toast.LENGTH_SHORT).show();
//                                        updateHyper(mHyper);
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


    private void setHypo() {
        mHypo = ((ActivityPDA) getActivity())
                .getDataStorage(FragmentSettings.class.getSimpleName())
                .getInt(SETTING_HYPO, HYPO_DEFAULT);
        final FragmentInput fragmentInput = new FragmentInput();
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER,
                new DecimalFormat("0.0").format((double) mHypo / 100));
        fragmentInput.setInputType(FragmentInput.POSITION_CENTER,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT,
                getString(R.string.unit_mmol_l));
        showDialogConfirm(getString(R.string.fragment_settings_lo_bg_threshold),
                "", "", fragmentInput, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:
                                mHypo = (int) (Float.parseFloat(fragmentInput
                                        .getInputText(FragmentInput.POSITION_CENTER)
                                        .toString()) * 100.0f);

                                if ((mHypo > HYPO_MAX) || (mHypo < HYPO_MIN)) {
                                    Toast.makeText(getActivity(),
                                            R.string.fragment_settings_hypo_error,
                                            Toast.LENGTH_SHORT).show();
                                    return false;
                                } else {
                                    if ((!mRFAddress.equals("")) && (!mRFAddress
                                            .equals(RFAddress.RF_ADDRESS_UNPAIR))) {
                                        ((ActivityPDA) getActivity())
                                                .handleMessage(new EntityMessage(
                                                        ParameterGlobal.ADDRESS_LOCAL_VIEW,
                                                        ParameterGlobal.ADDRESS_REMOTE_MASTER,
                                                        ParameterGlobal.PORT_GLUCOSE,
                                                        ParameterGlobal.PORT_GLUCOSE,
                                                        EntityMessage.OPERATION_SET,
                                                        ParameterGlucose.PARAM_FILL_LIMIT,
                                                        new ValueShort((short) mHypo)
                                                                .getByteArray()));
                                    } else {
//                                        updateHypo(mHypo);
                                        Toast.makeText(getActivity(),
                                                R.string.connect_fail,
                                                Toast.LENGTH_SHORT).show();
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


    private void setLanguage() {
        FragmentInput fragmentInput = new FragmentInput();
        fragmentInput
                .setComment(getString(R.string.setting_general_language_switch));
        fragmentInput.setInputText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_RIGHT, null);
        fragmentInput.setInputText(FragmentInput.POSITION_CENTER, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_LEFT, null);
        fragmentInput.setSeparatorText(FragmentInput.POSITION_RIGHT, null);
        showDialogConfirm(getString(R.string.setting_general_language), "", "",
                fragmentInput, new FragmentDialog.ListenerDialog() {
                    @Override
                    public boolean onButtonClick(int buttonID, Fragment content) {
                        switch (buttonID) {
                            case FragmentDialog.BUTTON_ID_POSITIVE:

                                if (Locale.getDefault().getLanguage().equals("zh")) {
                                    updateLanguage(Locale.ENGLISH);
                                } else {
                                    updateLanguage(Locale.SIMPLIFIED_CHINESE);
                                }

                                break;

                            default:
                                break;
                        }

                        return true;
                    }
                });
    }


    private void updateHyper(int hyper) {
        WidgetSettingItem settingItem = mRootView.findViewById(R.id.item_hi_bg);

        if (settingItem != null) {
            settingItem.setItemValue(
                    new DecimalFormat("0.0").format((double) hyper / 100.0));
        }

    }


    private void updateHypo(int hypo) {
        WidgetSettingItem settingItem = mRootView.findViewById(R.id.item_lo_bg);

        if (settingItem != null) {
            settingItem.setItemValue(
                    new DecimalFormat("0.0").format((double) hypo / 100.0));
        }
    }


    private void updateLanguage(Locale locale) {
        Resources resources = getActivity().getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();

        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

            configuration.setLocale(locale);

        } else {

            configuration.locale = locale;

        }

        resources.updateConfiguration(configuration, metrics);


        saveLanguageSetting(getActivity(), locale);
        Intent intent = getActivity().getIntent();
        getActivity().finish();
        getActivity().startActivity(intent);

    }

    private static void saveLanguageSetting(Context context, Locale locale) {

        String name = context.getPackageName() + "_" + LANGUAGE;

        SharedPreferences preferences =

                context.getSharedPreferences(name, Context.MODE_PRIVATE);

        preferences.edit().putString(LANGUAGE, locale.getLanguage()).apply();

        preferences.edit().putString(COUNTRY, locale.getCountry()).apply();

    }

    private void recovery() {
        restoreHg();
        unTransmitterId();
        cleanCache();
    }

    private void restoreHg() {
    }

    private void unTransmitterId() {
    }

    private void cleanCache() {
        DataCleanUtil.cleanSharedPreference(getActivity());
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
