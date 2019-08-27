package com.microtechmd.pda_app.entity;

import java.util.List;

/**
 * Created by Administrator on 2018/5/2.
 */

public class UserDevicesEntity {

    /**
     * info : {"code":100000,"msg":"成功","logno":"75dfb508b79c7c1e6de1e8927a60b35b"}
     * content : {"datalist":[{"recordno":"f823773cade8cb6220db6947f89d6e6b","recordtime":"20180502101850","devicetype":"1","devicename":"","deviceid":"1234567","endiantype":"0","model":"123","version":"1.1","capacity":"32","status":"1","desc":"","remark":"123456789"},{"recordno":"61754b3bdd5ad06751ee4339ed4c734f","recordtime":"20180502095533","devicetype":"1","devicename":"","deviceid":"123456","endiantype":"0","model":"123","version":"1.1","capacity":"32","status":"1","desc":"","remark":"abcdefg"}]}
     */

    private InfoBean info;
    private ContentBean content;

    public InfoBean getInfo() {
        return info;
    }

    public void setInfo(InfoBean info) {
        this.info = info;
    }

    public ContentBean getContent() {
        return content;
    }

    public void setContent(ContentBean content) {
        this.content = content;
    }

    public static class InfoBean {
        /**
         * code : 100000
         * msg : 成功
         * logno : 75dfb508b79c7c1e6de1e8927a60b35b
         */

        private int code;
        private String msg;
        private String logno;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getLogno() {
            return logno;
        }

        public void setLogno(String logno) {
            this.logno = logno;
        }
    }

    public static class ContentBean {
        private List<DatalistBean> datalist;

        public List<DatalistBean> getDatalist() {
            return datalist;
        }

        public void setDatalist(List<DatalistBean> datalist) {
            this.datalist = datalist;
        }

        public static class DatalistBean {
            /**
             * recordno : f823773cade8cb6220db6947f89d6e6b
             * recordtime : 20180502101850
             * devicetype : 1
             * devicename :
             * deviceid : 1234567
             * endiantype : 0
             * model : 123
             * version : 1.1
             * capacity : 32
             * status : 1
             * desc :
             * remark : 123456789
             */

            private String recordno;
            private String recordtime;
            private String devicetype;
            private String devicename;
            private String deviceid;
            private String endiantype;
            private String model;
            private String version;
            private String capacity;
            private String status;
            private String desc;
            private String remark;

            public String getRecordno() {
                return recordno;
            }

            public void setRecordno(String recordno) {
                this.recordno = recordno;
            }

            public String getRecordtime() {
                return recordtime;
            }

            public void setRecordtime(String recordtime) {
                this.recordtime = recordtime;
            }

            public String getDevicetype() {
                return devicetype;
            }

            public void setDevicetype(String devicetype) {
                this.devicetype = devicetype;
            }

            public String getDevicename() {
                return devicename;
            }

            public void setDevicename(String devicename) {
                this.devicename = devicename;
            }

            public String getDeviceid() {
                return deviceid;
            }

            public void setDeviceid(String deviceid) {
                this.deviceid = deviceid;
            }

            public String getEndiantype() {
                return endiantype;
            }

            public void setEndiantype(String endiantype) {
                this.endiantype = endiantype;
            }

            public String getModel() {
                return model;
            }

            public void setModel(String model) {
                this.model = model;
            }

            public String getVersion() {
                return version;
            }

            public void setVersion(String version) {
                this.version = version;
            }

            public String getCapacity() {
                return capacity;
            }

            public void setCapacity(String capacity) {
                this.capacity = capacity;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getDesc() {
                return desc;
            }

            public void setDesc(String desc) {
                this.desc = desc;
            }

            public String getRemark() {
                return remark;
            }

            public void setRemark(String remark) {
                this.remark = remark;
            }
        }
    }
}
