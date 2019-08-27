package com.microtechmd.pda_app.entity;

import java.util.List;

/**
 * Created by Administrator on 2018/4/12.
 */

public class UserDataEntity {

    /**
     * info : {"code":100000,"msg":"成功","logno":"934d7a42b8e884a2b5a08d6b255b2224"}
     * content : {"datalist":[{"code":100000,"msg":"成功","recordno":"7b1de6d4cdf5f02b6234a4cd8213edae","devicetype":"1","deviceid":"TB0009","recordid":"1","recordtime":"20180511153603"},{"code":100000,"msg":"成功","recordno":"640eff67fc6161654b225dee16b8e218","devicetype":"1","deviceid":"TB0009","recordid":"2","recordtime":"20180511153603"},{"code":100000,"msg":"成功","recordno":"db15d4edeccc75f6b769199d52445225","devicetype":"1","deviceid":"TB0009","recordid":"3","recordtime":"20180511153603"},{"code":100000,"msg":"成功","recordno":"4d0f353f4b919cdfa7d33a10f0e2afee","devicetype":"1","deviceid":"TB0009","recordid":"4","recordtime":"20180511153603"},{"code":100000,"msg":"成功","recordno":"8068bacd085b1b99a085042155d0896f","devicetype":"1","deviceid":"TB0009","recordid":"5","recordtime":"20180511153603"}]}
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
         * logno : 934d7a42b8e884a2b5a08d6b255b2224
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
             * code : 100000
             * msg : 成功
             * recordno : 7b1de6d4cdf5f02b6234a4cd8213edae
             * devicetype : 1
             * deviceid : TB0009
             * recordid : 1
             * recordtime : 20180511153603
             */

            private int code;
            private String msg;
            private String recordno;
            private String devicetype;
            private String deviceid;
            private String recordid;
            private String recordtime;

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

            public String getRecordno() {
                return recordno;
            }

            public void setRecordno(String recordno) {
                this.recordno = recordno;
            }

            public String getDevicetype() {
                return devicetype;
            }

            public void setDevicetype(String devicetype) {
                this.devicetype = devicetype;
            }

            public String getDeviceid() {
                return deviceid;
            }

            public void setDeviceid(String deviceid) {
                this.deviceid = deviceid;
            }

            public String getRecordid() {
                return recordid;
            }

            public void setRecordid(String recordid) {
                this.recordid = recordid;
            }

            public String getRecordtime() {
                return recordtime;
            }

            public void setRecordtime(String recordtime) {
                this.recordtime = recordtime;
            }
        }
    }
}
