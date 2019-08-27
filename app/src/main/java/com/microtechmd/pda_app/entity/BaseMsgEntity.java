package com.microtechmd.pda_app.entity;

/**
 * Created by Administrator on 2018/4/12.
 */

public class BaseMsgEntity {

    /**
     * info : {"code":100000,"msg":"成功","logno":"aa0ff68916f0d39ea07cd398bc7f5848"}
     */

    private InfoBean info;

    public InfoBean getInfo() {
        return info;
    }

    public void setInfo(InfoBean info) {
        this.info = info;
    }

    public static class InfoBean {
        /**
         * code : 100000
         * msg : 成功
         * logno : aa0ff68916f0d39ea07cd398bc7f5848
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
}
