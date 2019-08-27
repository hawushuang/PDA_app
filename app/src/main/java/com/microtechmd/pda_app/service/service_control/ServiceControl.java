package com.microtechmd.pda_app.service.service_control;


import android.os.Messenger;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda_app.service.service_control.service.ServiceBase;
import com.microtechmd.pda_app.service.service_control.blemanager.BluetoothDeviceManager;
import com.microtechmd.pda_app.service.service_control.task.TaskComm;
import com.microtechmd.pda_app.service.service_control.task.TaskGlucose;
import com.microtechmd.pda_app.service.service_control.task.TaskMonitor;
import com.vise.baseble.ViseBle;


public final class ServiceControl extends ServiceBase {
    private Messenger mMessengerView = null;

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothDeviceManager.getInstance().init(this);
        setExternalMessageHandler(new ExternalMessageHandler() {
            @Override
            public void handleMessage(final EntityMessage message,
                                      Messenger messengerReply) {
                if (messengerReply != null) {
                    if (message
                            .getSourceAddress() == ParameterGlobal.ADDRESS_LOCAL_VIEW) {
                        mMessengerView = messengerReply;
                    }
                }

                handleMessageExternal(message);
            }
        });
    }


    @Override
    public void onDestroy() {
        mMessengerView = null;
        super.onDestroy();
        ViseBle.getInstance().clear();
    }


    private synchronized void handleMessageExternal(final EntityMessage message) {
        mLog.Debug(getClass(), "Handle message: " + "Source Address:" +
                message.getSourceAddress() + " Target Address:" +
                message.getTargetAddress() + " Source Port:" +
                message.getSourcePort() + " Target Port:" +
                message.getTargetPort() + " Operation:" + message.getOperation() +
                " Parameter:" + message.getParameter());

        switch (message.getTargetAddress()) {
            case ParameterGlobal.ADDRESS_REMOTE_MASTER:
                TaskComm taskComm = TaskComm.getInstance(ServiceControl.this);
                taskComm.handleMessage(message);
                break;
//				将消息返回界面
            case ParameterGlobal.ADDRESS_LOCAL_VIEW:
                sendRemoteMessage(mMessengerView, message);
                break;
            default:
                handleMessageInternal(message);
                break;
        }

        mLog.Debug(getClass(), "Handle message end");
    }


    private void handleMessageInternal(final EntityMessage message) {
        switch (message.getTargetPort()) {

            case ParameterGlobal.PORT_COMM:
                TaskComm taskComm = TaskComm.getInstance(ServiceControl.this);
                taskComm.handleMessage(message);
                break;
            case ParameterGlobal.PORT_MONITOR:
                TaskMonitor taskMonitor =
                        TaskMonitor.getInstance(ServiceControl.this);
                taskMonitor.handleMessage(message);
                break;
            case ParameterGlobal.PORT_GLUCOSE:
                TaskGlucose taskGlucose =
                        TaskGlucose.getInstance(ServiceControl.this);
                taskGlucose.handleMessage(message);
                break;
            default:
                break;
        }
    }
}
