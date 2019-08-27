package com.microtechmd.pda_app.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2018/4/12.
 */

public class LoginEntity implements Parcelable {

    /**
     * info : {"code":100000,"msg":"成功","logno":"edab7379dac98c1e6e652ea14b645616"}
     * content : {"accessid":"fadb282be82a7bd4697ae289bf745e44","accesskey":"OWJkZWEzMGZiN2IxMzEzOWQ1ZjQ5YWRhNmIyOWIzZGI=","userno":"5dcc43ebdb776045682e93178336c715","userid":"test001","mobile":"18561002802","name":"","sex":"","birthday":"","height":"","weight":"","isadmin":"2","avatarno":"","avatarsuburl":"","pwdmodflag":"2","regsource":"11","regip":"60.176.31.219","regtime":"20180411145949","loginsource":"11","loginip":"223.93.170.175","logintime":"20180412102454","imtoken":"8mBT4gaEosl7sox3+nPtI9UHwY2A90PgcC/SQR6xNQFIpEVAqmp3FQLdTD1msW+pqFl3H2OvUqs9od6J6qEpcQINTWqe+p4c1ho1pC74cJMVd4wm2pNr/wJgKhRpF42hDU+Lsx9d+60="}
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

    public static class InfoBean implements Parcelable {

        /**
         * code : 100000
         * msg : 成功
         * logno : edab7379dac98c1e6e652ea14b645616
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.code);
            dest.writeString(this.msg);
            dest.writeString(this.logno);
        }

        public InfoBean() {
        }

        protected InfoBean(Parcel in) {
            this.code = in.readInt();
            this.msg = in.readString();
            this.logno = in.readString();
        }

        public static final Parcelable.Creator<InfoBean> CREATOR = new Parcelable.Creator<InfoBean>() {
            @Override
            public InfoBean createFromParcel(Parcel source) {
                return new InfoBean(source);
            }

            @Override
            public InfoBean[] newArray(int size) {
                return new InfoBean[size];
            }
        };
    }

    public static class ContentBean implements Parcelable {
        /**
         * accessid : fadb282be82a7bd4697ae289bf745e44
         * accesskey : OWJkZWEzMGZiN2IxMzEzOWQ1ZjQ5YWRhNmIyOWIzZGI=
         * userno : 5dcc43ebdb776045682e93178336c715
         * userid : test001
         * mobile : 18561002802
         * name :
         * sex :
         * birthday :
         * height :
         * weight :
         * isadmin : 2
         * avatarno :
         * avatarsuburl :
         * pwdmodflag : 2
         * regsource : 11
         * regip : 60.176.31.219
         * regtime : 20180411145949
         * loginsource : 11
         * loginip : 223.93.170.175
         * logintime : 20180412102454
         * imtoken : 8mBT4gaEosl7sox3+nPtI9UHwY2A90PgcC/SQR6xNQFIpEVAqmp3FQLdTD1msW+pqFl3H2OvUqs9od6J6qEpcQINTWqe+p4c1ho1pC74cJMVd4wm2pNr/wJgKhRpF42hDU+Lsx9d+60=
         */

        private String accessid;
        private String accesskey;
        private String userno;
        private String userid;
        private String mobile;
        private String name;
        private String sex;
        private String birthday;
        private String height;
        private String weight;
        private String isadmin;
        private String avatarno;
        private String avatarsuburl;
        private String pwdmodflag;
        private String regsource;
        private String regip;
        private String regtime;
        private String loginsource;
        private String loginip;
        private String logintime;
        private String imtoken;

        public String getAccessid() {
            return accessid;
        }

        public void setAccessid(String accessid) {
            this.accessid = accessid;
        }

        public String getAccesskey() {
            return accesskey;
        }

        public void setAccesskey(String accesskey) {
            this.accesskey = accesskey;
        }

        public String getUserno() {
            return userno;
        }

        public void setUserno(String userno) {
            this.userno = userno;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
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

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getIsadmin() {
            return isadmin;
        }

        public void setIsadmin(String isadmin) {
            this.isadmin = isadmin;
        }

        public String getAvatarno() {
            return avatarno;
        }

        public void setAvatarno(String avatarno) {
            this.avatarno = avatarno;
        }

        public String getAvatarsuburl() {
            return avatarsuburl;
        }

        public void setAvatarsuburl(String avatarsuburl) {
            this.avatarsuburl = avatarsuburl;
        }

        public String getPwdmodflag() {
            return pwdmodflag;
        }

        public void setPwdmodflag(String pwdmodflag) {
            this.pwdmodflag = pwdmodflag;
        }

        public String getRegsource() {
            return regsource;
        }

        public void setRegsource(String regsource) {
            this.regsource = regsource;
        }

        public String getRegip() {
            return regip;
        }

        public void setRegip(String regip) {
            this.regip = regip;
        }

        public String getRegtime() {
            return regtime;
        }

        public void setRegtime(String regtime) {
            this.regtime = regtime;
        }

        public String getLoginsource() {
            return loginsource;
        }

        public void setLoginsource(String loginsource) {
            this.loginsource = loginsource;
        }

        public String getLoginip() {
            return loginip;
        }

        public void setLoginip(String loginip) {
            this.loginip = loginip;
        }

        public String getLogintime() {
            return logintime;
        }

        public void setLogintime(String logintime) {
            this.logintime = logintime;
        }

        public String getImtoken() {
            return imtoken;
        }

        public void setImtoken(String imtoken) {
            this.imtoken = imtoken;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.accessid);
            dest.writeString(this.accesskey);
            dest.writeString(this.userno);
            dest.writeString(this.userid);
            dest.writeString(this.mobile);
            dest.writeString(this.name);
            dest.writeString(this.sex);
            dest.writeString(this.birthday);
            dest.writeString(this.height);
            dest.writeString(this.weight);
            dest.writeString(this.isadmin);
            dest.writeString(this.avatarno);
            dest.writeString(this.avatarsuburl);
            dest.writeString(this.pwdmodflag);
            dest.writeString(this.regsource);
            dest.writeString(this.regip);
            dest.writeString(this.regtime);
            dest.writeString(this.loginsource);
            dest.writeString(this.loginip);
            dest.writeString(this.logintime);
            dest.writeString(this.imtoken);
        }

        public ContentBean() {
        }

        protected ContentBean(Parcel in) {
            this.accessid = in.readString();
            this.accesskey = in.readString();
            this.userno = in.readString();
            this.userid = in.readString();
            this.mobile = in.readString();
            this.name = in.readString();
            this.sex = in.readString();
            this.birthday = in.readString();
            this.height = in.readString();
            this.weight = in.readString();
            this.isadmin = in.readString();
            this.avatarno = in.readString();
            this.avatarsuburl = in.readString();
            this.pwdmodflag = in.readString();
            this.regsource = in.readString();
            this.regip = in.readString();
            this.regtime = in.readString();
            this.loginsource = in.readString();
            this.loginip = in.readString();
            this.logintime = in.readString();
            this.imtoken = in.readString();
        }

        public static final Parcelable.Creator<ContentBean> CREATOR = new Parcelable.Creator<ContentBean>() {
            @Override
            public ContentBean createFromParcel(Parcel source) {
                return new ContentBean(source);
            }

            @Override
            public ContentBean[] newArray(int size) {
                return new ContentBean[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.info, flags);
        dest.writeParcelable(this.content, flags);
    }

    public LoginEntity() {
    }

    protected LoginEntity(Parcel in) {
        this.info = in.readParcelable(InfoBean.class.getClassLoader());
        this.content = in.readParcelable(ContentBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<LoginEntity> CREATOR = new Parcelable.Creator<LoginEntity>() {
        @Override
        public LoginEntity createFromParcel(Parcel source) {
            return new LoginEntity(source);
        }

        @Override
        public LoginEntity[] newArray(int size) {
            return new LoginEntity[size];
        }
    };
}
