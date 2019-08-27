package com.microtechmd.pda_app.entity;

import android.os.Message;

import com.microtechmd.pda.library.entity.EntityMessage;

/**
 * Created by Administrator on 2017/12/22.
 */

public class ModelEventEntity {
    private EntityMessage msg;

    public ModelEventEntity(EntityMessage msg) {
        this.msg = msg;
    }

    public EntityMessage getMsg() {
        return msg;
    }

    public void setMsg(EntityMessage msg) {
        this.msg = msg;
    }
}
