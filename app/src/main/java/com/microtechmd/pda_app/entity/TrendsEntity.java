package com.microtechmd.pda_app.entity;

import android.text.SpannableString;

public class TrendsEntity {
    private SpannableString name;
    private String normal;
    private String value;

    public TrendsEntity() {
    }

    public TrendsEntity(SpannableString name, String value) {
        this.name = name;
        this.value = value;
    }

    public TrendsEntity(SpannableString name, String normal, String value) {
        this.name = name;
        this.normal = normal;
        this.value = value;
    }

    public SpannableString getName() {
        return name;
    }

    public void setName(SpannableString name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getNormal() {
        return normal;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }
}
