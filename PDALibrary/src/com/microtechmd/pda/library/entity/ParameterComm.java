package com.microtechmd.pda.library.entity;

public class ParameterComm {
    public static final int PARAM_USERID = 1;
    public static final int PARAM_RF_STATE = 0;
    public static final int PARAM_RF_SIGNAL = 1;
    public static final int PARAM_RF_ADDRESS = 3;
    public static final int PARAM_RF_REMOTE_ADDRESS = 3;
    public static final int PARAM_RF_BROADCAST_SWITCH = 4;
    public static final int PARAM_BROADCAST_DATA = 5;
    public static final int PARAM_BROADCAST_OFFSET = 6;
    public static final int COUNT_PARAM = 7;
    public static final int PARAM_BROADCAST_SCAN = 8;
    public static final int CLEAN_DATABASES = 10;
    public static final int BROADCAST_SAVA = 11;
    public static final int SYNCHRONIZE_DATA = 12;
    public static final int CLOSE_COMM = 13;
    public static final int NEW_SENSER_COMM = 14;
    public static final int RESET_DATA = 15;
    public static final int SYNCHRONIZE_RAW_DATA = 16;
    public static final int SYNCHRONIZE_DATA_TEST = 17;

    public static final int BLE_SCAN = 18;
    public static final int BLE_CONNECTING = 19;
    public static final int BLE_NORMAL = 20;

    public static final int PAIR_SUCCESS = 34;
    public static final int PAIRFLAG = 33;
    public static final int PAIRAGAIN = 32;
    public static final int TRUNOFF = 30;
    public static final int READY = 31;
    public static final byte RF_STATE_IDLE = 0;
    public static final byte RF_STATE_BROADCAST = 1;
    public static final byte RF_STATE_CONNECTED = 2;
    public static final byte COUNT_RF_STATE = 3;

    public static final byte BROADCAST_OFFSET_ALL = 0;
    public static final byte BROADCAST_OFFSET_STATUS = 8;
    public static final byte BROADCAST_OFFSET_EVENT = 12;

    public static final int CLEAR = 40;
}
