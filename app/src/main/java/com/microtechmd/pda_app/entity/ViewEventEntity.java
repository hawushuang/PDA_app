package com.microtechmd.pda_app.entity;

import com.microtechmd.pda.library.entity.EntityMessage;

/**
 * Created by Administrator on 2017/12/22.
 */

public class ViewEventEntity {
    private EntityMessage msg;

    public ViewEventEntity(EntityMessage msg) {
        this.msg = msg;
    }

    public EntityMessage getMsg() {
        return msg;
    }

    public void setMsg(EntityMessage msg) {
        this.msg = msg;
    }
}
