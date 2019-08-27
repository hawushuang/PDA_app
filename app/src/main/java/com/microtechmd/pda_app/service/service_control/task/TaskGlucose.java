package com.microtechmd.pda_app.service.service_control.task;


import com.microtechmd.pda.library.entity.DataList;
import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterGlucose;
import com.microtechmd.pda.library.entity.ParameterMonitor;
import com.microtechmd.pda.library.entity.glucose.StatusGlucose;
import com.microtechmd.pda.library.entity.monitor.Event;
import com.microtechmd.pda.library.entity.monitor.History;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda_app.service.service_control.service.ServiceBase;
import com.microtechmd.pda_app.service.service_control.service.TaskBase;


public class TaskGlucose extends TaskBase {
    private static TaskGlucose sInstance = null;
    private static StatusGlucose sStatusGlucose = null;


    private TaskGlucose(ServiceBase service) {
        super(service);
    }


    public static synchronized TaskGlucose getInstance(
            final ServiceBase service) {
        if (sInstance == null) {
            sInstance = new TaskGlucose(service);
        }

        return sInstance;
    }


    @Override
    public void handleMessage(EntityMessage message) {
        if ((message
                .getTargetAddress() == ParameterGlobal.ADDRESS_LOCAL_CONTROL) &&
                (message.getTargetPort() == ParameterGlobal.PORT_GLUCOSE)) {
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
    }


    private void getParameter(final EntityMessage message) {
        int acknowledge = EntityMessage.FUNCTION_OK;
        byte[] value = null;


        switch (message.getParameter()) {
            case ParameterGlucose.PARAM_STATUS:
                mLog.Debug(getClass(), "Get status");

                if (sStatusGlucose == null) {
                    sStatusGlucose = new StatusGlucose();
                }

                value = sStatusGlucose.getByteArray();
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


    private void handleEvent(final EntityMessage message) {
    }


    private void handleNotification(final EntityMessage message) {
    }


    private void handleAcknowledgement(final EntityMessage message) {
        if ((message
                .getSourceAddress() == ParameterGlobal.ADDRESS_REMOTE_MASTER) &&
                (message.getSourcePort() == ParameterGlobal.PORT_GLUCOSE)) {

            message.setTargetAddress(ParameterGlobal.ADDRESS_LOCAL_VIEW);
            handleMessage(message);
        }
    }


    private void reverseMessagePath(EntityMessage message) {
        message.setTargetAddress(message.getSourceAddress());
        message.setSourceAddress(ParameterGlobal.ADDRESS_LOCAL_CONTROL);
        message.setTargetPort(message.getSourcePort());
        message.setSourcePort(ParameterGlobal.PORT_GLUCOSE);
    }


    private void updateStatusGlucose(final EntityMessage message,
                                     final StatusGlucose status) {
        mLog.Debug(getClass(), "Update status glucose");

        message.setTargetAddress(ParameterGlobal.ADDRESS_LOCAL_VIEW);
        message.setTargetPort(ParameterGlobal.PORT_MONITOR);
        message.setParameter(ParameterGlucose.PARAM_STATUS);
        message.setData(status.getByteArray());
        handleMessage(message);
    }
}