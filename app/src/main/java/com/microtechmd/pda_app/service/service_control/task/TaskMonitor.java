package com.microtechmd.pda_app.service.service_control.task;


import com.microtechmd.pda.library.entity.DataList;
import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterGlucose;
import com.microtechmd.pda.library.entity.ParameterMonitor;
import com.microtechmd.pda.library.entity.ValueShort;
import com.microtechmd.pda.library.entity.monitor.DateTime;
import com.microtechmd.pda.library.entity.monitor.Event;
import com.microtechmd.pda.library.entity.monitor.History;
import com.microtechmd.pda.library.entity.monitor.RawData;
import com.microtechmd.pda.library.entity.monitor.Status;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.ByteUtil;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.entity.DbHistory;
import com.microtechmd.pda_app.entity.DbHistory_;
import com.microtechmd.pda_app.entity.DbRawHistory;
import com.microtechmd.pda_app.entity.DbRawHistory_;
import com.microtechmd.pda_app.service.service_control.service.ServiceBase;
import com.microtechmd.pda_app.service.service_control.service.TaskBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.objectbox.query.QueryBuilder;

import static com.microtechmd.pda_app.ActivityPDA.BLOOD_GLUCOSE;
import static com.microtechmd.pda_app.ActivityPDA.CALIBRATION;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE_INVALID;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE_RECOMMEND_CAL;
import static com.microtechmd.pda_app.ActivityPDA.HYPER;
import static com.microtechmd.pda_app.ActivityPDA.HYPO;
import static com.microtechmd.pda_app.ActivityPDA.IMPENDANCE;
import static com.microtechmd.pda_app.ActivityPDA.SETTING_RF_ADDRESS;


public final class TaskMonitor extends TaskBase {
    // Constant and variable definition
    private static final int SENSOR_NEW = 4;
    public static final int SENSOR_EXPIRATION = 6;
    private static final String SETTING_STATUS_LAST = "status_last";
    private static final String SETTING_BOLUS_LAST = "bolus_last";
    private static final String SETTING_HISTORY_SYNC = "history_sync";

    private static final String SETTING_BROADCAST_SAVE = "broadcast_save";
    private String MEVENT_INDEX_MODEL = "index_model";
    private String MEVENT_INDEX_RAW_MODEL = "index_raw_model";
    private String SENSORINDEX = "sensorIndex";

    private static final int EVENT_INDEX_MAX = 10000;

    private static TaskMonitor sInstance = null;

    private DataList mStatusLast = null;
    private History mBolusLast = null;
    private boolean mIsNewStatusPump = false;
    private boolean mIsHistorySync = false;
    private int mEventIndexRemote = -1;//广播包数据index最大值
    private int mEventIndexModel = -1;
    private int mRawEventIndexModel = -1;
    private boolean mBroadcastSave = true;
    private boolean synchronizeDone;

    private History history_broadcast;
    private int sensorIndex;

    private boolean forceSynchronizeFlag = false;
    private boolean sendFlag = false;
    private String mRFAddress;

    private boolean rawSynchronizeFlag = false;

    // Method definition
    private TaskMonitor(ServiceBase service) {
        super(service);
        mRFAddress = getAddress(SPUtils.getbytes(mService, SETTING_RF_ADDRESS));

        mBroadcastSave = (boolean) SPUtils.get(mService, SETTING_BROADCAST_SAVE, true);
        synchronizeDone = true;
//        sensorIndex = (int) SPUtils.get(mService, SENSORINDEX, 0);
        mEventIndexModel = (int) SPUtils.get(mService, MEVENT_INDEX_MODEL, 0) + 1;
        mRawEventIndexModel = (int) SPUtils.get(mService, MEVENT_INDEX_RAW_MODEL, 0) + 1;

        QueryBuilder<DbHistory> builder = app.getBoxStore().boxFor(DbHistory.class).query();
        builder.equal(DbHistory_.rf_address, mRFAddress).orderDesc(DbHistory_.id);
        DbHistory dbLast = builder.build().findFirst();
        if (dbLast != null) {
            sensorIndex = dbLast.getSensorIndex();
        }

        History mStatusLastHistory = new History(
                mService.getDataStorage(null).getExtras(SETTING_STATUS_LAST, null));
        mStatusLast = new DataList();
        mStatusLast.pushData(mStatusLastHistory.getByteArray());
        mBolusLast = new History(
                mService.getDataStorage(null).getExtras(SETTING_BOLUS_LAST, null));
        mIsNewStatusPump = false;
        mIsHistorySync = mService.getDataStorage(null)
                .getBoolean(SETTING_HISTORY_SYNC, false);


        mLog.Debug(getClass(), "Initialization");
    }

    private String getAddress(byte[] addressByte) {
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

    public static synchronized TaskMonitor getInstance(
            final ServiceBase service) {
        if (sInstance == null) {
            sInstance = new TaskMonitor(service);
        }

        return sInstance;
    }


    @Override
    public void handleMessage(EntityMessage message) {
        if ((message
                .getTargetAddress() == ParameterGlobal.ADDRESS_LOCAL_CONTROL) &&
                (message.getTargetPort() == ParameterGlobal.PORT_MONITOR)) {
            switch (message.getOperation()) {
                case EntityMessage.OPERATION_SET:
                    setParameter(message);
                    break;

                case EntityMessage.OPERATION_GET:
                    getParameter(message);
                    break;

                case EntityMessage.OPERATION_EVENT:
                    handleEvent(message);
                    break;

                case EntityMessage.OPERATION_NOTIFY:
                    handleNotification(message);
                    break;

                case EntityMessage.OPERATION_ACKNOWLEDGE:
                    handleAcknowledgement(message);
                    break;

                default:
                    break;
            }
        } else {
            mService.onReceive(message);
        }
    }

    private void setParameter(final EntityMessage message) {
        mLog.Debug(getClass(), "Set parameter: " + message.getParameter());

        switch (message.getParameter()) {
            case ParameterComm.BROADCAST_SAVA:
                mBroadcastSave = message.getData()[0] != 0;
                SPUtils.put(mService, SETTING_BROADCAST_SAVE, mBroadcastSave);
                break;
            case ParameterComm.SYNCHRONIZE_DATA:
                synchronizeHistoryManual();
                break;
            case ParameterComm.SYNCHRONIZE_RAW_DATA:
                synchronizeRawHistory();
                break;
            case ParameterComm.SYNCHRONIZE_DATA_TEST:
                ValueShort value = new ValueShort((short) 1);

                handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                        ParameterGlobal.ADDRESS_REMOTE_MASTER, ParameterGlobal.PORT_MONITOR,
                        ParameterGlobal.PORT_MONITOR, EntityMessage.OPERATION_GET,
                        ParameterMonitor.PARAM_HISTORIES, value.getByteArray()));
                break;
            case ParameterComm.CLEAN_DATABASES:
                mEventIndexModel = 1;
                mRawEventIndexModel = 1;
                break;
            case ParameterComm.PAIR_SUCCESS:
                mRFAddress = getAddress(SPUtils.getbytes(mService, SETTING_RF_ADDRESS));
                QueryBuilder<DbHistory> builder = app.getBoxStore().boxFor(DbHistory.class).query();
                builder.equal(DbHistory_.rf_address, mRFAddress).orderDesc(DbHistory_.id);
                DbHistory dbLast = builder.build().findFirst();
                if (dbLast != null) {
                    sensorIndex = dbLast.getSensorIndex();
                    mEventIndexModel = dbLast.getEvent_index() + 1;
                } else {
                    sensorIndex = 0;
                    mEventIndexModel = 1;
                }
                QueryBuilder<DbRawHistory> builder_raw = app.getBoxStore().boxFor(DbRawHistory.class).query();
                builder_raw.equal(DbRawHistory_.device_id, mRFAddress).orderDesc(DbRawHistory_.id);
                DbRawHistory dbRawLast = builder_raw.build().findFirst();
                if (dbRawLast != null) {
                    mRawEventIndexModel = dbRawLast.getEvent_index();
                    SPUtils.put(mService, MEVENT_INDEX_RAW_MODEL, dbRawLast.getEvent_index());
                } else {
                    mRawEventIndexModel = 0;
                    SPUtils.put(mService, MEVENT_INDEX_RAW_MODEL, 0);
                }
                break;
        }
    }


    private void getParameter(final EntityMessage message) {
    }


    private void handleEvent(final EntityMessage message) {
//        mLog.Debug(getClass(), "Handle event: " + message.getEvent());
//
//        switch (message.getEvent()) {
//            case EntityMessage.EVENT_SEND_DONE:
//                break;
//
//            case EntityMessage.EVENT_ACKNOWLEDGE:
//                break;
//
//            case EntityMessage.EVENT_TIMEOUT:
//                mLog.Debug(getClass(), "Command Time Out!");
//                break;
//        }
    }


    private void handleNotification(final EntityMessage message) {
        mLog.Debug(getClass(), "Notify parameter: " + message.getParameter());
        if (message.getSourcePort() == ParameterGlobal.PORT_MONITOR) {
            if (message.getParameter() == ParameterMonitor.PARAM_BROADCAST_HISTORY
                    || message.getParameter() == ParameterMonitor.PARAM_HISTORIES ||
                    message.getParameter() == ParameterMonitor.PARAM_HISTORIES_RAW) {
                onNotifyHistory(message);
            }
        }
    }


    private void handleAcknowledgement(final EntityMessage message) {
        mLog.Debug(getClass(),
                "Acknowledge port comm: " + message.getData()[0]);
        if (message.getParameter() == ParameterGlucose.TASK_GLUCOSE_PARAM_NEW_SENSOR) {
            if (message.getData()[0] == EntityMessage.FUNCTION_OK) {
                if (history_broadcast != null) {
                    synchronizeDateTime(history_broadcast);
                }
            }
        }

    }


    private void reverseMessagePath(EntityMessage message) {
        message.setTargetAddress(message.getSourceAddress());
        message.setSourceAddress(ParameterGlobal.ADDRESS_LOCAL_CONTROL);
        message.setTargetPort(message.getSourcePort());
        message.setSourcePort(ParameterGlobal.PORT_MONITOR);
    }


    private void onNotifyHistory(final EntityMessage message) {
        mLog.Error(getClass(), "SourceAddress()" + message.getSourceAddress()
                + "Parameter()" + message.getParameter());

//		连接后返回的数据
        if (message.getSourceAddress() == ParameterGlobal.ADDRESS_REMOTE_MASTER) {
            switch (message.getParameter()) {
                case ParameterMonitor.PARAM_HISTORIES:
                    onNotifyHistoryRemote(message);
                    break;
                case ParameterMonitor.PARAM_HISTORIES_RAW:
                    onNotifyRawHistoryRemote(message);
                    break;
                default:

                    break;
            }
        }
//		接收广播包数据
        else if (message
                .getSourceAddress() == ParameterGlobal.ADDRESS_LOCAL_CONTROL) {
            onNotifyHistoryControl(message);
        }
    }

    private int flag;


    private void onNotifyRawHistoryRemote(final EntityMessage message) {
        long t1 = System.currentTimeMillis();

        DataList dataList = new DataList();
        DataList rawDataList = new DataList();

        byte[] data = message.getData();
        int pointer = 0;
        boolean error = false;

        byte[] history_byte = new byte[12];
        byte[] short_byte = new byte[2];
        byte[] int_byte = new byte[4];
        int index = 0;
        int dateTime = 0;
        int value = 0;

        for (int i = 0; pointer < data.length; i++) {
            if (i == 0) {
                if (data.length < 7) {
                    error = true;
                    break;
                }

                history_byte[0] = 0;
                history_byte[1] = 0;
                System.arraycopy(data, 2, history_byte, 2, 4);
                System.arraycopy(data, 0, history_byte, 6, 2);
                history_byte[8] = ByteUtil.intToByte(sensorIndex);
                history_byte[9] = data[6];
//                history_byte[12] = 0;
//                history_byte[13] = 0;
                pointer = 7;

                System.arraycopy(history_byte, 2, int_byte, 0, 4);
                dateTime = ByteUtil.bytesToInt(int_byte);
                System.arraycopy(history_byte, 6, short_byte, 0, 2);
                index = ByteUtil.bytesToInt(short_byte);

                if (index != mRawEventIndexModel) {
                    error = true;
                    break;
                }
            } else {
                byte firstByte = data[pointer];
                pointer += 1;

                int addTime = firstByte & 0x7F;
                if (addTime <= 120) {
                    dateTime += addTime * 10;
                } else {
                    if (pointer + 4 > data.length) {
                        error = true;
                        break;
                    }
                    System.arraycopy(data, pointer, int_byte, 0, 4);
                    dateTime = ByteUtil.bytesToInt(int_byte);
                    pointer += 4;
                }
                System.arraycopy(ByteUtil.intToBytes(dateTime), 0, history_byte, 2, 4);

                index++;
                System.arraycopy(ByteUtil.intToBytes(index), 0, history_byte, 6, 2);

                if ((firstByte & 0x80) != 0) {
                    if (pointer + 1 > data.length) {
                        error = true;
                        break;
                    }
                    history_byte[9] = data[pointer];
                    pointer += 1;
                }

            }
            byte type = history_byte[9];
            if (type == GLUCOSE || type == GLUCOSE_RECOMMEND_CAL || type == HYPO || type == HYPER) {
                if (pointer + 1 > data.length) {
                    error = true;
                    break;
                }
                history_byte[10] = data[pointer];
                pointer += 1;
            } else if (type == BLOOD_GLUCOSE || type == CALIBRATION) {
                if (pointer + 2 > data.length) {
                    error = true;
                    break;
                }
                history_byte[10] = data[pointer];
                history_byte[11] = data[pointer + 1];
                pointer += 2;
            } else {
                history_byte[10] = 0;
            }
//            if (type == GLUCOSE || type == GLUCOSE_RECOMMEND_CAL || type == HYPO || type == HYPER || type == BLOOD_GLUCOSE || type == CALIBRATION) {
//                if (pointer + 1 > data.length) {
//                    error = true;
//                    break;
//                }
//                history_byte[10] = data[pointer];
//                pointer += 1;
//            } else {
//                history_byte[10] = 0;
//            }

            byte[] rawBytes = new byte[17];
            if (type == GLUCOSE || type == GLUCOSE_RECOMMEND_CAL || type == GLUCOSE_INVALID) {
                if (pointer + 17 > data.length) {
                    error = true;
                    break;
                }
                System.arraycopy(data, pointer, rawBytes, 0, 17);
                pointer += 17;
            } else if (type == IMPENDANCE) {
                if (pointer + 5 > data.length) {
                    error = true;
                    break;
                }
                System.arraycopy(data, pointer, rawBytes, 0, 5);
                pointer += 5;
            }
            rawDataList.pushData(rawBytes);
//            mLog.Error(getClass(), "原始数据：" + Arrays.toString(rawBytes));

            byte[] history_byte_add = new byte[12];
            System.arraycopy(history_byte, 0, history_byte_add, 0, 12);
            dataList.pushData(history_byte_add);
        }
        if (mRawEventIndexModel >= 0) {
            if (!error) {
                mRawEventIndexModel = index;
                flag = 0;
            } else {
                if (flag < 3) {
                    flag++;
                    synchronizeRawHistory(mRawEventIndexModel);
                    return;
                } else {
                    flag = 0;
                }
            }

            long t2 = System.currentTimeMillis();
            mLog.Error(getClass(), "解析时间：" + (t2 - t1));

            index = mRawEventIndexModel;
            mRawEventIndexModel += 1;
            if (index < mEventIndexRemote) {
                mLog.Error(getClass(), "连续同步" + mRawEventIndexModel);
                synchronizeRawHistory(mRawEventIndexModel);
            } else {
                synchronizeDone = true;
            }
            SPUtils.put(mService, MEVENT_INDEX_RAW_MODEL, index);
            updateRawHistory(message, dataList, rawDataList);
        }
    }

    private void onNotifyHistoryRemote(final EntityMessage message) {
        mLog.Debug(getClass(), "Notify history remote begin");
        long t1 = System.currentTimeMillis();

        DataList dataList = new DataList();

        byte[] data = message.getData();
        int pointer = 0;
        boolean error = false;

        byte[] history_byte = new byte[14];
        byte[] short_byte = new byte[2];
        byte[] int_byte = new byte[4];
        int index = 0;
        int dateTime = 0;
        int value = 0;

        for (int i = 0; pointer < data.length; i++) {
            if (i == 0) {
                if (data.length < 7) {
                    error = true;
                    break;
                }

                history_byte[0] = 0;
                history_byte[1] = 0;
                System.arraycopy(data, 2, history_byte, 2, 4);
                System.arraycopy(data, 0, history_byte, 6, 2);
                history_byte[8] = ByteUtil.intToByte(sensorIndex);
                history_byte[9] = data[6];
                history_byte[12] = 0;
                history_byte[13] = 0;
                pointer = 7;

                System.arraycopy(history_byte, 2, int_byte, 0, 4);
                dateTime = ByteUtil.bytesToInt(int_byte);
                System.arraycopy(history_byte, 6, short_byte, 0, 2);
                index = ByteUtil.bytesToInt(short_byte);

                if (index != mEventIndexModel) {
                    error = true;
                    break;
                }
            } else {
                byte firstByte = data[pointer];
                pointer += 1;

                int addTime = firstByte & 0x7F;
                if (addTime <= 120) {
                    dateTime += addTime * 10;
                } else {
                    if (pointer + 4 > data.length) {
                        error = true;
                        break;
                    }
                    System.arraycopy(data, pointer, int_byte, 0, 4);
                    dateTime = ByteUtil.bytesToInt(int_byte);
                    pointer += 4;
                }
                System.arraycopy(ByteUtil.intToBytes(dateTime), 0, history_byte, 2, 4);

                index++;
                System.arraycopy(ByteUtil.intToBytes(index), 0, history_byte, 6, 2);

                if ((firstByte & 0x80) != 0) {
                    if (pointer + 1 > data.length) {
                        error = true;
                        break;
                    }
                    history_byte[9] = data[pointer];
                    pointer += 1;
                }

            }
            byte type = history_byte[9];
            if (type == GLUCOSE || type == GLUCOSE_RECOMMEND_CAL || type == HYPO || type == HYPER) {
                if (pointer + 1 > data.length) {
                    error = true;
                    break;
                }
                history_byte[10] = data[pointer];
                pointer += 1;
            } else if (type == BLOOD_GLUCOSE || type == CALIBRATION) {
                if (pointer + 2 > data.length) {
                    error = true;
                    break;
                }
                history_byte[10] = data[pointer];
                history_byte[11] = data[pointer + 1];
                pointer += 2;
            } else {
                history_byte[10] = 0;
            }
//            if (type == GLUCOSE || type == GLUCOSE_RECOMMEND_CAL || type == HYPO || type == HYPER || type == BLOOD_GLUCOSE || type == CALIBRATION) {
//                if (pointer + 1 > data.length) {
//                    error = true;
//                    break;
//                }
//                history_byte[10] = data[pointer];
//                pointer += 1;
//            } else {
//                history_byte[10] = 0;
//            }
            byte[] history_byte_add = new byte[14];
            System.arraycopy(history_byte, 0, history_byte_add, 0, 14);
            dataList.pushData(history_byte_add);
        }
        if ((mEventIndexModel >= 0) && (mEventIndexRemote >= 0)) {

            if (!error) {
                mEventIndexModel = index;
                flag = 0;
            } else {
                if (flag < 3) {
                    flag++;
                    synchronizeHistory(mEventIndexModel);
                    return;
                } else {
                    flag = 0;
                }
            }

            long t2 = System.currentTimeMillis();
            mLog.Error(getClass(), "解析时间：" + (t2 - t1));

            index = mEventIndexModel;
            mEventIndexModel += 1;
            if (index < mEventIndexRemote) {
                mLog.Error(getClass(), "连续同步" + mEventIndexModel);
                synchronizeHistory(mEventIndexModel);
            } else {
                synchronizeDone = true;
            }
            SPUtils.put(mService, MEVENT_INDEX_MODEL, index);
            updateHistory(message, dataList);
        }
    }

    private void onNotifyHistoryControl(final EntityMessage message) {
        byte[] broadcastBytes = new byte[12];
        if (message.getData().length >= 12) {
            System.arraycopy(message.getData(), 0, broadcastBytes, 0, 11);
        }
        History history = new History(broadcastBytes);
        mLog.Error(getClass(), "广播包：" + history.getDateTime().getBCD() +
                "type:" + history.getEvent().getEvent() +
                "index: " + history.getEvent().getIndex() +
                "value" + history.getStatus().getShortValue1());

        mLog.Error(getClass(), "广播包：" + Arrays.toString(broadcastBytes));
        if ((history.getDateTime().getYear() == 2000)
                && (history.getEvent().getEvent() == 0)
                && (history.getEvent().getIndex() == 0)
                && (history.getStatus().getShortValue1() == 0)) {
            return;
        }
        history_broadcast = new History(history.getByteArray());

        mEventIndexRemote = history.getEvent().getIndex();

        // 新传感器
        int value = history.getStatus().getShortValue1();
        if (history.getEvent().getEvent() == SENSOR_NEW) {
            if (value == (0xFF - 1)) {
                return;
            }
            if (value == 0xFF) {
                handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                        ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_MONITOR,
                        ParameterGlobal.PORT_MONITOR, EntityMessage.OPERATION_EVENT,
                        ParameterMonitor.PARAM_NEW, history.getByteArray()));
                sendFlag = true;
                return;
            }
        } else {
            if (sendFlag) {
                History newSensorRecovery = new History(new DateTime(Calendar.getInstance()),
                        new Status(0), new Event(0, SENSOR_NEW, 0));
                handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                        ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_MONITOR,
                        ParameterGlobal.PORT_MONITOR, EntityMessage.OPERATION_EVENT,
                        ParameterMonitor.PARAM_NEW, newSensorRecovery.getByteArray()));
                sendFlag = false;
            }
        }
        /////////////////////////////////////////////

        //校准时间
        long systemDateTime = System.currentTimeMillis();
        long historyDateTime = history.getDateTime().getCalendar().getTimeInMillis()
                + history.getBattery().getElapsedtime() * 10 * 1000;
        long dateTimeError = Math.abs(systemDateTime - historyDateTime);

        if (dateTimeError < 60 * 1000) {
            forceSynchronizeFlag = false;
        } else if (forceSynchronizeFlag ||
                (history.getEvent().getEvent() != SENSOR_EXPIRATION && history.getBattery().getElapsedtime() <= 120)) {
            synchronizeDateTime(history);
            return;
        }
        ////////////////////////////////////////

        //修正倒计时
        if (history.getEvent().getEvent() == SENSOR_NEW) {
            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                    ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_MONITOR,
                    ParameterGlobal.PORT_MONITOR, EntityMessage.OPERATION_EVENT,
                    ParameterMonitor.PARAM_NEW, history.getByteArray()));
            return;
        }
        /////////////////////////////////////////////
        if (mEventIndexRemote <= 0) {
            return;
        }

        if (!mIsHistorySync) {
            mIsHistorySync = true;
        } else {
            mLog.Error(getClass(), "mEventIndexRemote: " + mEventIndexRemote);
            mLog.Error(getClass(), "BROADCAST_SAVE: " + mBroadcastSave);
            mLog.Error(getClass(), "synchronizeDone: " + synchronizeDone);
            if (mBroadcastSave) {
                message.setTargetAddress(ParameterGlobal.ADDRESS_LOCAL_VIEW);
                message.setOperation(EntityMessage.OPERATION_NOTIFY);
                message.setParameter(ParameterMonitor.PARAM_STATUS);
                message.setData(history.getByteArray());
                handleMessage(message);
            }
            if (mEventIndexRemote != (mEventIndexModel - 1)) {
                if (mBroadcastSave || !synchronizeDone) {
                    DataList dataList = new DataList();
                    dataList.pushData(history.getByteArray());

                    int sensorIndex_last = history.getEvent().getSensorIndex();
                    if (sensorIndex_last != sensorIndex) {
                        SPUtils.put(mService, SENSORINDEX, sensorIndex_last);
                        sensorIndex = sensorIndex_last;
                        SPUtils.put(mService, MEVENT_INDEX_MODEL, 0);
                        SPUtils.put(mService, MEVENT_INDEX_RAW_MODEL, 0);
                        mEventIndexModel = 1;
                        mRawEventIndexModel = 1;
                        synchronizeHistory(mEventIndexModel);
                        return;
                    }
                    if (history.getEvent().getIndex() > 0) {
                        if ((mEventIndexRemote == mEventIndexModel)) {
                            if (history.getEvent().getEvent() == 0x1E) {
                                mLog.Error(getClass(), "同步: " + mEventIndexModel);
                                synchronizeHistory(mEventIndexModel);
                            } else {
                                updateHistory(message, dataList);
                                SPUtils.put(mService, MEVENT_INDEX_MODEL, mEventIndexModel);
                                mEventIndexModel++;
                                synchronizeDone = true;
                            }

                        } else if (mEventIndexRemote < mEventIndexModel - 1) {
                            updateHistory(message, dataList);
                            SPUtils.put(mService, MEVENT_INDEX_MODEL, mEventIndexModel);
                            mEventIndexModel = mEventIndexRemote + 1;
                            mRawEventIndexModel = mEventIndexRemote;
                            synchronizeDone = true;
                        } else if (mEventIndexRemote > mEventIndexModel) {
                            mLog.Error(getClass(), "同步: " + mEventIndexModel);
                            synchronizeHistory(mEventIndexModel);
                        }
                    }
                }
            }
        }
        mLog.Debug(getClass(), "Notify history control: " + mEventIndexRemote);

//        if (rawSynchronizeFlag) {
//            if (mRawEventIndexModel < mEventIndexRemote) {
//                synchronizeRawHistory();
//            } else {
//                rawSynchronizeFlag = false;
//            }
//        } else {
//            if (mEventIndexRemote - mRawEventIndexModel >= 50) {
//                synchronizeRawHistory();
//            }
//        }
    }


    private void updateRawHistory(EntityMessage message, DataList dataList, DataList rawDatalist) {
        if (dataList.getCount() > 0) {
            History historyLast = new History(dataList.getData(dataList.getCount() - 1));
            mLog.Error(getClass(), historyLast.getDateTime().getBCD() +
                    "type:" + historyLast.getEvent().getEvent() +
                    "index: " + historyLast.getEvent().getIndex() +
                    "value" + historyLast.getStatus().getShortValue1());
        }


        List<DbRawHistory> dbRawHistoryList = new ArrayList<>();
        for (int i = 0; i < dataList.getCount(); i++) {
            History history = new History(dataList.getData(i));
            RawData rawData = new RawData(rawDatalist.getData(i));
            DbRawHistory rawHistory = new DbRawHistory();
            int type = history.getEvent().getEvent();
            rawHistory.setDevice_id(mRFAddress);
            rawHistory.setDate_time(history.getDateTime().getBCD());
            rawHistory.setSensorIndex(history.getEvent().getSensorIndex());
            rawHistory.setEvent_index(history.getEvent().getIndex());
            rawHistory.setEvent_type(history.getEvent().getEvent());
            rawHistory.setValue(history.getStatus().getShortValue1());
            rawHistory.setSplit(0);
            rawHistory.setP1(rawData.getP1());
//            if (type == IMPENDANCE) {
//                rawHistory.setP2((int) ((short) rawData.getP2()));
//                rawHistory.setP3((int) ((short) rawData.getP3()));
//            } else {
            rawHistory.setP2(rawData.getP2());
            rawHistory.setP3(rawData.getP3());
//            }
            rawHistory.setP4(rawData.getP4());
            dbRawHistoryList.add(rawHistory);
            if (type == GLUCOSE || type == GLUCOSE_RECOMMEND_CAL || type == GLUCOSE_INVALID) {
                rawHistory = new DbRawHistory();
                rawHistory.setDevice_id(mRFAddress);
                rawHistory.setDate_time(history.getDateTime().getBCD());
                rawHistory.setSensorIndex(history.getEvent().getSensorIndex());
                rawHistory.setEvent_index(history.getEvent().getIndex());
                rawHistory.setEvent_type(history.getEvent().getEvent());
                rawHistory.setSplit(1);
                rawHistory.setValue(rawData.getP5());
                rawHistory.setP1(rawData.getP6());
                rawHistory.setP2(rawData.getP7());
                rawHistory.setP3(rawData.getP8());
                rawHistory.setP4(rawData.getP9());
                dbRawHistoryList.add(rawHistory);
            }

        }
        app.getBoxStore().boxFor(DbRawHistory.class).put(dbRawHistoryList);

        message.setOperation(EntityMessage.OPERATION_NOTIFY);
        message.setParameter(ParameterMonitor.RAW_SAVED);
        message.setTargetAddress(ParameterGlobal.ADDRESS_LOCAL_VIEW);
        handleMessage(message);
    }

    private void updateHistory(final EntityMessage message,
                               final DataList dataList) {
        message.setOperation(EntityMessage.OPERATION_NOTIFY);
        message.setParameter(ParameterMonitor.PARAM_HISTORY);
        message.setData(dataList.getByteArray());
        message.setTargetAddress(ParameterGlobal.ADDRESS_LOCAL_VIEW);
        handleMessage(message);
        List<DbHistory> dbHistoryList = new ArrayList<>();

        if (dataList.getCount() > 0) {
            History historyLast = new History(dataList.getData(dataList.getCount() - 1));
            mLog.Error(getClass(), historyLast.getDateTime().getBCD() +
                    "type:" + historyLast.getEvent().getEvent() +
                    "index: " + historyLast.getEvent().getIndex() +
                    "value" + historyLast.getStatus().getShortValue1());
        }
        for (int i = 0; i < dataList.getCount(); i++) {
            History history = new History(dataList.getData(i));
            DbHistory dbHistory;
            dbHistory = new DbHistory();
            dbHistory.setRf_address(mRFAddress);
            dbHistory.setDate_time(history.getDateTime().getDateTimeLong());
            dbHistory.setSensorIndex(history.getEvent().getSensorIndex());
            dbHistory.setEvent_index(history.getEvent().getIndex());
            dbHistory.setEvent_type(history.getEvent().getEvent());
            dbHistory.setValue(history.getStatus().getShortValue1());
            dbHistoryList.add(dbHistory);
        }
        app.getBoxStore().boxFor(DbHistory.class).put(dbHistoryList);

    }

    private void synchronizeHistoryManual() {
        mLog.Error(getClass(), "同步完成：" + synchronizeDone);
        int modelIndex = (int) SPUtils.get(mService, MEVENT_INDEX_MODEL, 0);
        if (modelIndex < mEventIndexRemote) {
            if (synchronizeDone) {
                synchronizeDone = false;
                synchronizeHistory(modelIndex + 1);
            }
        }
    }

    private void synchronizeRawHistory() {
        int modelIndex = (int) SPUtils.get(mService, MEVENT_INDEX_RAW_MODEL, 0);
        mLog.Error(getClass(), "保存的原始index：" + modelIndex);
        if (modelIndex < mEventIndexRemote) {
            synchronizeRawHistory(modelIndex + 1);
            rawSynchronizeFlag = true;
        }
    }


    private void synchronizeHistory(int index) {
        mLog.Debug(getClass(), "Get history remote: " + index);

        ValueShort value = new ValueShort((short) index);

        handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                ParameterGlobal.ADDRESS_REMOTE_MASTER, ParameterGlobal.PORT_MONITOR,
                ParameterGlobal.PORT_MONITOR, EntityMessage.OPERATION_GET,
                ParameterMonitor.PARAM_HISTORIES, value.getByteArray()));
    }

    private void synchronizeRawHistory(int index) {

        ValueShort value = new ValueShort((short) index);

        handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                ParameterGlobal.ADDRESS_REMOTE_MASTER, ParameterGlobal.PORT_MONITOR,
                ParameterGlobal.PORT_MONITOR, EntityMessage.OPERATION_GET,
                ParameterMonitor.PARAM_HISTORIES_RAW, value.getByteArray()));
    }


    private void synchronizeDateTime(History history) {
        final long DATE_TIME_ERROR_MAX = 60 * 1000;
        final int YEAR_MIN = 2017;

        long systemDateTime = System.currentTimeMillis();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(systemDateTime);

        if (calendar.get(Calendar.YEAR) < YEAR_MIN) {
            return;
        } else {
            if ((calendar.get(Calendar.YEAR) == YEAR_MIN) &&
                    (calendar.get(Calendar.MONTH) <= Calendar.JANUARY) &&
                    (calendar.get(Calendar.DAY_OF_MONTH) <= 1)) {
                return;
            }
        }

        long historyDateTime =
                history.getDateTime().getCalendar().getTimeInMillis()
                        + history.getBattery().getElapsedtime() * 10 * 1000;
        long dateTimeError;

        if ((history.getDateTime().getMonth() == 0) ||
                (history.getDateTime().getDay() == 0)) {
            dateTimeError = DATE_TIME_ERROR_MAX;
        } else {
            if (systemDateTime > historyDateTime) {
                dateTimeError = systemDateTime - historyDateTime;
            } else {

                dateTimeError = historyDateTime - systemDateTime;
            }
        }

        if (dateTimeError >= DATE_TIME_ERROR_MAX) {
            mLog.Error(getClass(), "校准时间");

            final DateTime dateTime = new DateTime(calendar);
            long nowTime = systemDateTime - DateTime.BASE_TIME;
            handleMessage(
                    new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                            ParameterGlobal.ADDRESS_REMOTE_MASTER,
                            ParameterGlobal.PORT_MONITOR, ParameterGlobal.PORT_MONITOR,
                            EntityMessage.OPERATION_SET,
                            ParameterMonitor.PARAM_DATETIME, ByteUtil.intToBytes((int) (nowTime / 1000))));
            history.setDateTime(dateTime);
        } else {
            forceSynchronizeFlag = false;
        }
    }
}
