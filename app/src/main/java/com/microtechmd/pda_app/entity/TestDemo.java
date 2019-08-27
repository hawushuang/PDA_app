package com.microtechmd.pda_app.entity;

import java.util.List;

public class TestDemo {

    private List<ListBean> list;

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * Time : 63707268960
         * SG : 2.75
         */

        private long Time;
        private double SG;

        public long getTime() {
            return Time;
        }

        public void setTime(long Time) {
            this.Time = Time;
        }

        public double getSG() {
            return SG;
        }

        public void setSG(double SG) {
            this.SG = SG;
        }
    }
}
