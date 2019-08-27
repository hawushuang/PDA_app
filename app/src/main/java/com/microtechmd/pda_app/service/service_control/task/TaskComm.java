package com.microtechmd.pda_app.service.service_control.task;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.microtechmd.pda.library.entity.DataBundle;
import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterMonitor;
import com.microtechmd.pda.library.entity.comm.RFAddress;
import com.microtechmd.pda.library.entity.comm.UserId;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.service.service_control.service.ServiceBase;
import com.microtechmd.pda_app.service.service_control.service.TaskBase;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.service.service_control.platform.DeviceBLE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import static com.microtechmd.pda_app.ActivityPDA.SETTING_RF_ADDRESS;

public final class TaskComm extends TaskBase {

    private static final String SETTING_BROADCAST_SWITCH = "broadcast_switch";
    public static final String RF_ADDRESS = "control_address";
    public static final String BONDID = "bondId";
    public static final String BONDKEY = "bondKey";
    private static final int REMOTE_DEVICE_COUNT = 1;
    private static final int INTERVAL_LINK_CHECK = 100;
    private static final int INTERVAL_CONNECTION_TIMEOUT_SHORT = 200;
    private static final int INTERVAL_CONNECTION_TIMEOUT_LONG = 10000;

    private Context context;
    @SuppressLint("StaticFieldLeak")
    private static TaskComm sInstance = null;
    private static DeviceBLE sDeviceBLE = null;
    private EntityMessage mEntityMessage;

    private boolean mBroadcastSwitch = false;
    private boolean pairFlag = false;
    private byte mRFState = ParameterComm.COUNT_RF_STATE;
    private int mConnectionTimeout = 0;
    private int mConnectionTimer = 0;
    private MessageList[] mMessageList = null;
    private EntityMessage[] mMessageRequest = null;
    private EntityMessage[] mMessageAcknowledge = null;
    private Handler[] mHandlerLink = null;
    private Runnable[] mRunnableLink = null;

    private boolean bondedFlag = false;
    private boolean bondingFlag = false;

    private boolean connFlag = false;

    private int bondCount = 0;
    private boolean sendPairRepeat = false;
    private Handler mainHandler;

    private final class MessageList extends LinkedList<EntityMessage> {
        private static final long serialVersionUID = 1L;
    }

    private TaskComm(ServiceBase service) {
        super(service);
        context = service;
        mLog.Debug(getClass(), "Initialization");

        mConnectionTimeout = INTERVAL_CONNECTION_TIMEOUT_SHORT;
        mConnectionTimer = 0;

        if (mMessageList == null) {
            mMessageList = new MessageList[REMOTE_DEVICE_COUNT];
        }

        if (mMessageRequest == null) {
            mMessageRequest = new EntityMessage[REMOTE_DEVICE_COUNT];
        }

        if (mMessageAcknowledge == null) {
            mMessageAcknowledge = new EntityMessage[REMOTE_DEVICE_COUNT];
        }

        if (mHandlerLink == null) {
            mHandlerLink = new Handler[REMOTE_DEVICE_COUNT];
        }

        mainHandler = new Handler();
        if (mRunnableLink == null) {
            mRunnableLink = new Runnable[REMOTE_DEVICE_COUNT];
        }

        for (int i = 0; i < REMOTE_DEVICE_COUNT; i++) {
            mMessageList[i] = new MessageList();
            mMessageRequest[i] = null;
            mMessageAcknowledge[i] = null;
            mHandlerLink[i] = new Handler();
            mRunnableLink[i] = null;
        }

        sDeviceBLE.setCallback(new DeviceBLE.BleCallback() {
            @Override
            public void onStateChange(boolean connectFlag) {

                if (connectFlag) {
                    //连接成功
                    mLog.Error(getClass(), "连接成功后发送");
                    sDeviceBLE.stopScan();
                    final EntityMessage messageRequest = mMessageList[0].peek();
                    if ((messageRequest != null) && (sDeviceBLE
                            .query(0) == EntityMessage.FUNCTION_OK)) {
                        mainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendRemoteMessage(messageRequest);
                            }
                        }, 200);
                    }
//                    if ((messageRequest != null) && (sDeviceBLE
//                            .query(0) == EntityMessage.FUNCTION_OK)) {
//                        sendRemoteMessage(messageRequest);
//                    }
                } else {
                    //连接失败
                    sDeviceBLE.startScan();

                    EntityMessage messageRequest =
                            mMessageList[ParameterGlobal.ADDRESS_REMOTE_MASTER]
                                    .poll();
                    while (messageRequest != null) {
                        sendFailEvent(messageRequest);
                        messageRequest =
                                mMessageList[ParameterGlobal.ADDRESS_REMOTE_MASTER]
                                        .poll();
                    }
//                    messageRequest = mMessageList[0].peek();
//                    if ((messageRequest != null) && (sDeviceBLE
//                            .query(0) == EntityMessage.FUNCTION_OK)) {
//                        mainHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                sendFailEvent(mEntityMessage);
//                            }
//                        }, 0);
//                    }
//                    if (mEntityMessage != null) {
//                        sendFailEvent(mEntityMessage);
//                        mEntityMessage = null;
//                    }
//                    clearMessageList(0);
                    checkLink(0);
                }
                connFlag = connectFlag;
            }

            @Override
            public void onDisconnect() {
                sDeviceBLE.startScan();
                EntityMessage messageRequest =
                        mMessageList[ParameterGlobal.ADDRESS_REMOTE_MASTER]
                                .poll();
                while (messageRequest != null) {
                    sendFailEvent(messageRequest);
                    messageRequest =
                            mMessageList[ParameterGlobal.ADDRESS_REMOTE_MASTER]
                                    .poll();
                }
                checkLink(0);
            }
        });
    }


    public static synchronized TaskComm getInstance(final ServiceBase service) {
        sDeviceBLE = DeviceBLE.getInstance(service);
        if (sInstance == null) {
            sInstance = new TaskComm(service);
        }
        return sInstance;
    }


    @Override
    public void handleMessage(final EntityMessage message) {
        switch (message.getTargetAddress()) {
            case ParameterGlobal.ADDRESS_REMOTE_MASTER:
                handleMessageRemote(message);
                break;
            case ParameterGlobal.ADDRESS_REMOTE_SLAVE:
                if (message.getParameter() == ParameterComm.PARAM_RF_ADDRESS) {
                    if (message.getData() != null) {
                        RFAddress address = new RFAddress(message.getData());
                        if (address.getAddress().equals(RFAddress.RF_ADDRESS_UNPAIR)) {
                            onRFSignalChanged((byte) 0);
                            sDeviceBLE.stopScan();
                        } else {
                            sDeviceBLE.startScan();
                            handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                                    ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_COMM,
                                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_ACKNOWLEDGE,
                                    ParameterComm.PARAM_RF_ADDRESS, new byte[]{EntityMessage.FUNCTION_OK}
                            ));
                        }
                    }
                }
                break;
            case ParameterGlobal.ADDRESS_LOCAL_CONTROL:
                if (message.getTargetPort() == ParameterGlobal.PORT_COMM) {
                    handleMessageLocal(message);
                } else {
                    mService.onReceive(message);
                }

                break;

            default:
                mService.onReceive(message);
                break;
        }
    }


    private void handleMessageRemote(final EntityMessage message) {
        // Check if the message is received from remote device or required
        // sending to remote device
        if (message.getSourceAddress() == message.getTargetAddress()) {

//            if (message.getSourcePort() == ParameterGlobal.PORT_MONITOR) {
//                if (message.getParameter() != ParameterMonitor.PARAM_HISTORIES &&
//                        message.getParameter() != ParameterMonitor.PARAM_HISTORIES_RAW) {
//                    sDeviceBLE.disconnect();
//                }
//            }
            if (message.getOperation() == EntityMessage.OPERATION_PAIR &&
                    message.getParameter() == ParameterComm.PARAM_USERID &&
                    message.getSourcePort() == ParameterGlobal.PORT_COMM) {
                new Handler(mService.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        sDeviceBLE.disconnect();
                    }
                });
                byte[] data = message.getData();
                if (data.length >= 23) {
                    byte[] bondId = new byte[6];
                    byte[] bondKey = new byte[16];
                    System.arraycopy(data, 1, bondId, 0, 6);
                    System.arraycopy(data, 7, bondKey, 0, bondKey.length);

                    SPUtils.putbytes(mService, BONDID, bondId);
                    SPUtils.putbytes(mService, BONDKEY, bondKey);
                }
                if (sendPairRepeat) {
                    sendRemoteMessage(mEntityMessage);
                    return;
                }
            }
            if ((message.getSourceAddress() == ParameterGlobal.ADDRESS_REMOTE_MASTER) && bondingFlag &&
                    (message.getOperation() != EntityMessage.OPERATION_EVENT) &&
                    (message.getOperation() != EntityMessage.OPERATION_PAIR)) {
                bondingFlag = false;
                if (message.getOperation() == EntityMessage.OPERATION_BOND &&
                        message.getParameter() == ParameterComm.PARAM_USERID &&
                        message.getSourcePort() == ParameterGlobal.PORT_COMM) {
                    if (message.getData()[0] == EntityMessage.FUNCTION_OK) {
                        bondedFlag = true;
                        handleMessage(mEntityMessage);
                    } else {
                        mLog.Error(getClass(), "绑定返回，参数位失败");
                        SPUtils.putbytes(mService, BONDID, null);
                        SPUtils.putbytes(mService, BONDKEY, null);
                        bondedFlag = false;
                    }
                } else {
                    mLog.Error(getClass(), "绑定失败");
                    SPUtils.putbytes(mService, BONDID, null);
                    SPUtils.putbytes(mService, BONDKEY, null);
                    bondedFlag = false;
                }
                return;
            }
            receiveRemoteMessage(message);
        } else {
            if ((message.getOperation() == EntityMessage.OPERATION_SET) ||
                    (message.getOperation() == EntityMessage.OPERATION_GET) ||
                    (message.getOperation() == EntityMessage.OPERATION_NOTIFY) ||
                    (message.getOperation() == EntityMessage.OPERATION_ACKNOWLEDGE) ||
                    (message.getOperation() == EntityMessage.OPERATION_PAIR) ||
                    (message.getOperation() == EntityMessage.OPERATION_UNPAIR) ||
                    (message.getOperation() == EntityMessage.OPERATION_BOND)) {
                message.setMode(EntityMessage.MODE_ACKNOWLEDGE);
            } else if (message.getOperation() == EntityMessage.OPERATION_EVENT) {
                message.setMode(EntityMessage.MODE_NO_ACKNOWLEDGE);
            } else {
                return;
            }

            final MessageList messageList =
                    mMessageList[message.getTargetAddress()];
            messageList.add(new EntityMessage(message.toByteArray()));
            mLog.Debug(getClass(), "Add message list: " + messageList.size() +
                    ", TargetAddress: " + message.getTargetAddress() + "sourceAddress:" + message.getSourceAddress());

//            sendRemoteMessage(message);
            if (messageList.size() <= 1) {
                sendRemoteMessage(message);
            }
        }
    }


    private void handleMessageLocal(final EntityMessage message) {
        switch (message.getOperation()) {
            case EntityMessage.OPERATION_SET:
                setParameter(message);
                break;

            case EntityMessage.OPERATION_GET:
                getParameter(message);
                break;

            case EntityMessage.OPERATION_NOTIFY:
                handleNotification(message);
                break;

            case EntityMessage.OPERATION_ACKNOWLEDGE:
                handleAcknowledgement(message);
                break;

            case EntityMessage.OPERATION_EVENT:
                handleEvent(message);
                break;

            default:
                break;
        }
    }


    private void setParameter(final EntityMessage message) {
        int acknowledge = EntityMessage.FUNCTION_OK;
        if (message.getData() == null) {
            return;
        }
        switch (message.getParameter()) {
            case ParameterComm.PARAM_RF_STATE:
                mLog.Debug(getClass(), "Set RF state: " + message.getData()[0]);

                if (message.getData()[0] == ParameterComm.RF_STATE_CONNECTED) {
                    mConnectionTimeout = INTERVAL_CONNECTION_TIMEOUT_LONG;
                } else {
                    mConnectionTimeout = INTERVAL_CONNECTION_TIMEOUT_SHORT;
                }

                break;

            case ParameterComm.PARAM_RF_BROADCAST_SWITCH:
                mLog.Debug(getClass(), "Set broadcast switch: " +
                        message.getData()[0]);

                mBroadcastSwitch = message.getData()[0] != 0;
                if (mBroadcastSwitch) {
                    sDeviceBLE.startScan();
                } else {
                    sDeviceBLE.stopScan();
                }
                break;
            case ParameterComm.PAIRFLAG:
                pairFlag = message.getData()[0] != 0;
                break;
            case ParameterComm.CLEAN_DATABASES:
                SPUtils.clear(context);
                break;
            default:
                acknowledge = EntityMessage.FUNCTION_FAIL;
                break;
        }

        reverseMessagePath(message);
        message.setOperation(EntityMessage.OPERATION_ACKNOWLEDGE);
        message.setData(new byte[]
                {
                        (byte) acknowledge
                });
        handleMessage(message);
    }


    private void getParameter(final EntityMessage message) {
        int acknowledge = EntityMessage.FUNCTION_OK;
        byte[] value = null;


        switch (message.getParameter()) {
            case ParameterComm.PARAM_RF_STATE:
                mLog.Debug(getClass(), "Get RF state: " + mRFState);

                value = new byte[1];
                value[0] = mRFState;
                break;

            default:
                acknowledge = EntityMessage.FUNCTION_FAIL;
        }

        reverseMessagePath(message);

        if (acknowledge == EntityMessage.FUNCTION_OK) {
            message.setOperation(EntityMessage.OPERATION_NOTIFY);
            message.setData(value);
        } else {
            message.setOperation(EntityMessage.OPERATION_ACKNOWLEDGE);
            message.setData(new byte[]
                    {
                            (byte) acknowledge
                    });
        }

        handleMessage(message);
    }


    private void handleNotification(final EntityMessage message) {
        switch (message.getParameter()) {
            case ParameterComm.PARAM_BROADCAST_DATA:
                if (pairFlag) {
                    handleMessage(new EntityMessage(
                            ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                            ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                            ParameterGlobal.PORT_MONITOR,
                            ParameterGlobal.PORT_MONITOR,
                            EntityMessage.OPERATION_NOTIFY,
                            ParameterMonitor.PARAM_BROADCAST_HISTORY,
                            message.getData()));
                }
                break;
            default:
                break;
        }
    }


    private void handleAcknowledgement(final EntityMessage message) {
    }


    private void handleEvent(final EntityMessage message) {
        switch (message.getEvent()) {
            case EntityMessage.EVENT_SEND_DONE:
                break;

            case EntityMessage.EVENT_ACKNOWLEDGE:
                break;

            case EntityMessage.EVENT_TIMEOUT:
                mLog.Debug(getClass(), "Command timeout");
                break;

            default:
                break;
        }
    }


    private EntityMessage updateMessageList(int address) {
        EntityMessage message;
        EntityMessage messageRequest;


        mLog.Debug(getClass(), "Update message list: " +
                mMessageList[address].size() + ", Address: " + address);

        messageRequest = mMessageList[address].poll();
        message = mMessageList[address].peek();

        if (message != null) {
            sendRemoteMessage(message);
            return messageRequest;
        }

        checkLink(address);

        return messageRequest;
    }


    private void sendRemoteMessage(final EntityMessage message) {
        byte[] addressByte = SPUtils.getbytes(mService, SETTING_RF_ADDRESS);
        if (addressByte == null) {
            updateMessageList(message.getTargetAddress());
            sendFailEvent(message);
            return;
        }
        if (!sDeviceBLE.isConnected()) {
            new Handler(mService.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    sDeviceBLE.connect();
                }
            });
            bondedFlag = false;
            sDeviceBLE.turnOff();
            return;
        }
        if ((message.getOperation() != EntityMessage.OPERATION_PAIR) &&
                (message.getOperation() != EntityMessage.OPERATION_EVENT)) {
            mEntityMessage = message;
            byte[] bondId = SPUtils.getbytes(mService, BONDID);
            byte[] bondKey = SPUtils.getbytes(mService, BONDKEY);
            sendPairRepeat = false;
            if (bondId == null) {
                //重新配对
                bondCount++;
                mLog.Error(getClass(), "绑定次数：" + bondCount +
                        "sendParameter：" + message.getParameter());
                if (bondCount >= 3) {
                    //界面提示重新配对
                    bondCount = 0;
                    sendPairRepeat = false;
                    handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                            ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.PORT_COMM,
                            ParameterGlobal.PORT_COMM,
                            EntityMessage.OPERATION_SET,
                            ParameterComm.PAIRAGAIN,
                            null));

                } else {
                    sendPairRepeat = true;
                    String userId = (String) SPUtils.get(mService, StringConstant.USERNO, "");
                    byte[] id = new UserId(userId).getByteArray();
                    sDeviceBLE.send(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                            ParameterGlobal.ADDRESS_REMOTE_MASTER,
                            ParameterGlobal.PORT_COMM,
                            ParameterGlobal.PORT_COMM,
                            EntityMessage.OPERATION_PAIR,
                            ParameterComm.PARAM_USERID,
                            id));
                }
                return;
            }

            if (!bondedFlag) {
                sDeviceBLE.send(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                        ParameterGlobal.ADDRESS_REMOTE_MASTER,
                        ParameterGlobal.PORT_COMM,
                        ParameterGlobal.PORT_COMM,
                        EntityMessage.OPERATION_BOND,
                        ParameterComm.PARAM_USERID,
                        bondId));
                bondingFlag = true;
                sDeviceBLE.ready(bondKey);
                return;
            }
        }
        if (sDeviceBLE.send(message) != EntityMessage.FUNCTION_OK) {
            mLog.Debug(getClass(), "Send remote fail");
        }
    }


    private void receiveRemoteMessage(final EntityMessage message) {
        message.setTargetAddress(ParameterGlobal.ADDRESS_LOCAL_CONTROL);

        if (((message.getOperation() == EntityMessage.OPERATION_NOTIFY) ||
                (message.getOperation() == EntityMessage.OPERATION_ACKNOWLEDGE) ||
                (message.getOperation() == EntityMessage.OPERATION_PAIR) ||
                (message.getOperation() == EntityMessage.OPERATION_UNPAIR) ||
                (message.getOperation() == EntityMessage.OPERATION_BOND)) &&
                (message.getMode() == EntityMessage.MODE_ACKNOWLEDGE)) {
            mMessageAcknowledge[message.getSourceAddress()] =
                    new EntityMessage(message.getTargetAddress(),
                            message.getSourceAddress(), message.getTargetPort(),
                            message.getSourcePort(), EntityMessage.OPERATION_EVENT,
                            message.getParameter(), new byte[]
                            {
                                    EntityMessage.EVENT_ACKNOWLEDGE
                            });
            checkLink(message.getSourceAddress());
        }

        EntityMessage messageRequest;

        if ((message.getOperation() == EntityMessage.OPERATION_EVENT) &&
                (message.getEvent() < EntityMessage.COUNT_EVENT)) {
//			获取消息列表中的第一个
            messageRequest = mMessageList[message.getSourceAddress()].peek();

            if (messageRequest != null) {
                message.setTargetAddress(messageRequest.getSourceAddress());
                message.setParameter(messageRequest.getParameter());

                if (messageRequest.getMode() == EntityMessage.MODE_ACKNOWLEDGE) {
                    if (message.getEvent() == EntityMessage.EVENT_ACKNOWLEDGE) {
                        mMessageRequest[message.getSourceAddress()] =
                                updateMessageList(message.getSourceAddress());
                    } else if (message.getEvent() == EntityMessage.EVENT_TIMEOUT) {
                        mMessageRequest[message.getSourceAddress()] = null;
                        updateMessageList(message.getSourceAddress());
                    }
                } else {
                    if (message.getEvent() == EntityMessage.EVENT_SEND_DONE) {
                        mMessageRequest[message.getSourceAddress()] = null;
                        updateMessageList(message.getSourceAddress());
                    }
                }
            }
        } else {
            messageRequest = mMessageRequest[message.getSourceAddress()];

            if (messageRequest != null) {
                if ((message.getSourcePort() == messageRequest
                        .getTargetPort()) &&
                        (message.getTargetPort() == messageRequest
                                .getSourcePort()) &&
                        (message.getParameter() == messageRequest.getParameter()) &&
                        ((message.getOperation() == EntityMessage.OPERATION_NOTIFY) ||
                                (message.getOperation() == EntityMessage.OPERATION_ACKNOWLEDGE) ||
                                (message.getOperation() == EntityMessage.OPERATION_PAIR) ||
                                (message.getOperation() == EntityMessage.OPERATION_UNPAIR) ||
                                (message.getOperation() == EntityMessage.OPERATION_BOND))) {
                    message.setTargetAddress(messageRequest.getSourceAddress());
                }

                mMessageRequest[message.getSourceAddress()] = null;
            }
        }

        handleMessage(message);
    }


    private void reverseMessagePath(final EntityMessage message) {
        message.setTargetAddress(message.getSourceAddress());
        message.setSourceAddress(ParameterGlobal.ADDRESS_LOCAL_CONTROL);
        message.setTargetPort(message.getSourcePort());
        message.setSourcePort(ParameterGlobal.PORT_COMM);
    }


    private void sendFailEvent(final EntityMessage message) {
        mLog.Debug(getClass(), "Send fail event");

        EntityMessage messageEvent =
                new EntityMessage(message.getTargetAddress(),
                        message.getSourceAddress(), message.getTargetPort(),
                        message.getSourcePort(), EntityMessage.EVENT_SEND_DONE);
        messageEvent.setParameter(message.getParameter());
        if (messageEvent.getTargetAddress() != ParameterGlobal.ADDRESS_LOCAL_VIEW) {
            handleMessage(messageEvent);
        }

        if ((message.getOperation() == EntityMessage.OPERATION_SET) ||
                (message.getOperation() == EntityMessage.OPERATION_GET) ||
                (message.getOperation() == EntityMessage.OPERATION_UNPAIR) ||
                (message.getOperation() == EntityMessage.OPERATION_PAIR)) {
            messageEvent.setEvent(EntityMessage.EVENT_TIMEOUT);
        }

        handleMessage(messageEvent);
    }


    private void changeRFState(byte state) {
        mLog.Debug(getClass(), "Change RF state: " + state);

        mRFState = ParameterComm.COUNT_RF_STATE;
    }


    private void onRFSignalChanged(byte signal) {
        handleMessage(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_COMM,
                ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_NOTIFY,
                ParameterComm.PARAM_RF_SIGNAL,
                new byte[]{signal}));
    }

    private void checkLink(final int address) {
        mLog.Debug(getClass(), "Check link: " + address);

        if (mRunnableLink[address] == null) {
            mRunnableLink[address] = new Runnable() {
                @Override
                public void run() {
                    if ((mMessageList[address].size() == 0) && (sDeviceBLE
                            .query(address) == EntityMessage.FUNCTION_OK)) {
                        if (mMessageAcknowledge[address] == null) {
                            mLog.Debug(TaskComm.class, "Link idle: " + address);

                            if (address == ParameterGlobal.ADDRESS_REMOTE_MASTER) {
                                mConnectionTimer += INTERVAL_LINK_CHECK;

                                if (mConnectionTimer >= mConnectionTimeout) {
                                    mConnectionTimer = 0;

                                    if (sDeviceBLE.isConnected()) {
                                        sDeviceBLE.disconnect();
                                    }
                                    sDeviceBLE.switchLink(
                                            ParameterGlobal.ADDRESS_REMOTE_MASTER,
                                            0);
                                    sDeviceBLE.switchLink(
                                            ParameterGlobal.ADDRESS_REMOTE_MASTER,
                                            1);
                                } else {
                                    mHandlerLink[address].postDelayed(this,
                                            INTERVAL_LINK_CHECK);
                                }
                            }

                            return;
                        } else {
                            handleMessage(mMessageAcknowledge[address]);
                            mMessageAcknowledge[address] = null;
                        }
                    }

                    mLog.Debug(TaskComm.class, "Link busy: " + address);

                    if (address == ParameterGlobal.ADDRESS_REMOTE_MASTER) {
                        mConnectionTimer = 0;
                    }

                    mHandlerLink[address].postDelayed(this,
                            INTERVAL_LINK_CHECK);
                }
            };
        }

        if (address == ParameterGlobal.ADDRESS_REMOTE_MASTER) {
            mConnectionTimer = 0;
        }

        mHandlerLink[address].removeCallbacks(mRunnableLink[address]);
        mHandlerLink[address].postDelayed(mRunnableLink[address],
                INTERVAL_LINK_CHECK);
    }
}
