package com.microtechmd.pda_app.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Administrator on 2018/6/14.
 */

public class FriendRequest {

    /**
     * info : {"code":100000,"msg":"成功","logno":"5161379d0ef8155db9cdd773fd34a24c"}
     * content : {"pageinfo":{"totalcount":5,"totalpage":1,"pagesize":20,"currentpage":1},"reqlist":[{"recordno":"cee8ffdf6baf0815f388bdd155686cff","friuserno":"3fc0d2ce6493d1ebfc356a97203fda72","reqtime":"20180613152400","status":"1","userid":"qwerty","name":"小明","sex":"2","birthday":"19970808","avatarno":"","verinfo":""},{"recordno":"56732a07c68f206f87a33d5fe22ac091","friuserno":"3fc0d2ce6493d1ebfc356a97203fda72","reqtime":"20180613151257","status":"1","userid":"qwerty","name":"小明","sex":"2","birthday":"19970808","avatarno":"","verinfo":"啦啦"},{"recordno":"9d1f7fa4a486e0631f1dc53b87a50dba","friuserno":"3fc0d2ce6493d1ebfc356a97203fda72","reqtime":"20180613150513","status":"1","userid":"qwerty","name":"小明","sex":"2","birthday":"19970808","avatarno":"","verinfo":"测试+++"},{"recordno":"228fb10f1d754460ab84b4111cd9ceaf","friuserno":"3fc0d2ce6493d1ebfc356a97203fda72","reqtime":"20180613150321","status":"1","userid":"qwerty","name":"小明","sex":"2","birthday":"19970808","avatarno":"","verinfo":"测试+++"},{"recordno":"83ea8adb1f9ed9d79b47a6e3824f2447","friuserno":"3fc0d2ce6493d1ebfc356a97203fda72","reqtime":"20180613140007","status":"1","userid":"qwerty","name":"小明","sex":"2","birthday":"19970808","avatarno":"","verinfo":"测试+++"}]}
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
         * logno : 5161379d0ef8155db9cdd773fd34a24c
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
        /**
         * pageinfo : {"totalcount":5,"totalpage":1,"pagesize":20,"currentpage":1}
         * reqlist : [{"recordno":"cee8ffdf6baf0815f388bdd155686cff","friuserno":"3fc0d2ce6493d1ebfc356a97203fda72","reqtime":"20180613152400","status":"1","userid":"qwerty","name":"小明","sex":"2","birthday":"19970808","avatarno":"","verinfo":""},{"recordno":"56732a07c68f206f87a33d5fe22ac091","friuserno":"3fc0d2ce6493d1ebfc356a97203fda72","reqtime":"20180613151257","status":"1","userid":"qwerty","name":"小明","sex":"2","birthday":"19970808","avatarno":"","verinfo":"啦啦"},{"recordno":"9d1f7fa4a486e0631f1dc53b87a50dba","friuserno":"3fc0d2ce6493d1ebfc356a97203fda72","reqtime":"20180613150513","status":"1","userid":"qwerty","name":"小明","sex":"2","birthday":"19970808","avatarno":"","verinfo":"测试+++"},{"recordno":"228fb10f1d754460ab84b4111cd9ceaf","friuserno":"3fc0d2ce6493d1ebfc356a97203fda72","reqtime":"20180613150321","status":"1","userid":"qwerty","name":"小明","sex":"2","birthday":"19970808","avatarno":"","verinfo":"测试+++"},{"recordno":"83ea8adb1f9ed9d79b47a6e3824f2447","friuserno":"3fc0d2ce6493d1ebfc356a97203fda72","reqtime":"20180613140007","status":"1","userid":"qwerty","name":"小明","sex":"2","birthday":"19970808","avatarno":"","verinfo":"测试+++"}]
         */

        private PageinfoBean pageinfo;
        private List<ReqlistBean> reqlist;

        public PageinfoBean getPageinfo() {
            return pageinfo;
        }

        public void setPageinfo(PageinfoBean pageinfo) {
            this.pageinfo = pageinfo;
        }

        public List<ReqlistBean> getReqlist() {
            return reqlist;
        }

        public void setReqlist(List<ReqlistBean> reqlist) {
            this.reqlist = reqlist;
        }

        public static class PageinfoBean {
            /**
             * totalcount : 5
             * totalpage : 1
             * pagesize : 20
             * currentpage : 1
             */

            private int totalcount;
            private int totalpage;
            private int pagesize;
            private int currentpage;

            public int getTotalcount() {
                return totalcount;
            }

            public void setTotalcount(int totalcount) {
                this.totalcount = totalcount;
            }

            public int getTotalpage() {
                return totalpage;
            }

            public void setTotalpage(int totalpage) {
                this.totalpage = totalpage;
            }

            public int getPagesize() {
                return pagesize;
            }

            public void setPagesize(int pagesize) {
                this.pagesize = pagesize;
            }

            public int getCurrentpage() {
                return currentpage;
            }

            public void setCurrentpage(int currentpage) {
                this.currentpage = currentpage;
            }
        }

        public static class ReqlistBean implements Parcelable {
            /**
             * recordno : cee8ffdf6baf0815f388bdd155686cff
             * friuserno : 3fc0d2ce6493d1ebfc356a97203fda72
             * reqtime : 20180613152400
             * status : 1
             * userid : qwerty
             * name : 小明
             * sex : 2
             * birthday : 19970808
             * avatarno :
             * verinfo :
             */

            private String recordno;
            private String friuserno;
            private String reqtime;
            private String status;
            private String userid;
            private String name;
            private String sex;
            private String birthday;
            private String avatarno;
            private String verinfo;

            public String getRecordno() {
                return recordno;
            }

            public void setRecordno(String recordno) {
                this.recordno = recordno;
            }

            public String getFriuserno() {
                return friuserno;
            }

            public void setFriuserno(String friuserno) {
                this.friuserno = friuserno;
            }

            public String getReqtime() {
                return reqtime;
            }

            public void setReqtime(String reqtime) {
                this.reqtime = reqtime;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getUserid() {
                return userid;
            }

            public void setUserid(String userid) {
                this.userid = userid;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSex() {
                return sex;
            }

            public void setSex(String sex) {
                this.sex = sex;
            }

            public String getBirthday() {
                return birthday;
            }

            public void setBirthday(String birthday) {
                this.birthday = birthday;
            }

            public String getAvatarno() {
                return avatarno;
            }

            public void setAvatarno(String avatarno) {
                this.avatarno = avatarno;
            }

            public String getVerinfo() {
                return verinfo;
            }

            public void setVerinfo(String verinfo) {
                this.verinfo = verinfo;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.recordno);
                dest.writeString(this.friuserno);
                dest.writeString(this.reqtime);
                dest.writeString(this.status);
                dest.writeString(this.userid);
                dest.writeString(this.name);
                dest.writeString(this.sex);
                dest.writeString(this.birthday);
                dest.writeString(this.avatarno);
                dest.writeString(this.verinfo);
            }

            public ReqlistBean() {
            }

            protected ReqlistBean(Parcel in) {
                this.recordno = in.readString();
                this.friuserno = in.readString();
                this.reqtime = in.readString();
                this.status = in.readString();
                this.userid = in.readString();
                this.name = in.readString();
                this.sex = in.readString();
                this.birthday = in.readString();
                this.avatarno = in.readString();
                this.verinfo = in.readString();
            }

            public static final Parcelable.Creator<ReqlistBean> CREATOR = new Parcelable.Creator<ReqlistBean>() {
                @Override
                public ReqlistBean createFromParcel(Parcel source) {
                    return new ReqlistBean(source);
                }

                @Override
                public ReqlistBean[] newArray(int size) {
                    return new ReqlistBean[size];
                }
            };
        }
    }
}
