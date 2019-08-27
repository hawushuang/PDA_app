package com.microtechmd.pda_app.entity;

import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Administrator on 2017/12/29.
 */
@SmartTable(name = "原始数据", count = true)
public class DbRawHistoryDisplay {
    @SmartColumn(id = 1, name = "deviceid")
    private String device_id;

    @SmartColumn(id = 2, name = "sensorid")
    private int sensorIndex;

    @SmartColumn(id = 3, name = "devicetime", autoMerge = true)
    private String date_time;

    @SmartColumn(id = 4, name = "eventindex", autoMerge = true)
    private int event_index;

    @SmartColumn(id = 5, name = "eventtype")
    private int event_type;

    @SmartColumn(id = 6, name = "split")
    private int split;

    @SmartColumn(id = 7, name = "eventdata")
    private float value;

    @SmartColumn(id = 8, name = "P1")
    private float p1;

    @SmartColumn(id = 9, name = "P2")
    private float p2;

    @SmartColumn(id = 10, name = "P3")
    private float p3;

    @SmartColumn(id = 11, name = "P4")
    private float p4;

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public int getSensorIndex() {
        return sensorIndex;
    }

    public void setSensorIndex(int sensorIndex) {
        this.sensorIndex = sensorIndex;
    }

    public int getEvent_index() {
        return event_index;
    }

    public void setEvent_index(int event_index) {
        this.event_index = event_index;
    }

    public int getEvent_type() {
        return event_type;
    }

    public void setEvent_type(int event_type) {
        this.event_type = event_type;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getSplit() {
        return split;
    }

    public void setSplit(int split) {
        this.split = split;
    }

    public float getP1() {
        return p1;
    }

    public void setP1(float p1) {
        this.p1 = p1;
    }

    public float getP2() {
        return p2;
    }

    public void setP2(float p2) {
        this.p2 = p2;
    }

    public float getP3() {
        return p3;
    }

    public void setP3(float p3) {
        this.p3 = p3;
    }

    public float getP4() {
        return p4;
    }

    public void setP4(float p4) {
        this.p4 = p4;
    }

}
