package com.microtechmd.pda_app.entity;

import com.microtechmd.pda.library.entity.EntityMessage;

/**
 * Created by Administrator on 2017/12/22.
 */

public class MedicineEntity {
    private String name;
    private String count;

    public MedicineEntity() {
    }

    public MedicineEntity(String name, String count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
