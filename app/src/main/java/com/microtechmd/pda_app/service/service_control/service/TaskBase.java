package com.microtechmd.pda_app.service.service_control.service;


import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.utility.LogPDA;
import com.microtechmd.pda_app.ApplicationPDA;


public abstract class TaskBase {
    protected ServiceBase mService = null;
    protected LogPDA mLog = null;
    protected ApplicationPDA app;

    public abstract void handleMessage(EntityMessage message);


    protected TaskBase(ServiceBase service) {
        if (mLog == null) {
            mLog = new LogPDA();
        }

        mService = service;
        app = (ApplicationPDA) service.getApplication();
    }
}
