package com.microtechmd.pda_app.entity;

import java.util.List;

/**
 * Created by Administrator on 2018/6/13.
 */

public class Friend {

    /**
     * info : {"code":100000,"msg":"成功","logno":"58e1217fb12ce37e97faa56141543c84"}
     * content : {"pageinfo":{"totalcount":1,"totalpage":1,"pagesize":20,"currentpage":1},"friendlist":[{"friuserno":"5dcc43ebdb776045682e93178336c715","userid":"test001","name":"哈哈","sex":"2","birthday":"20000601","avatarno":"","avatarsuburl":"","bgflag1":"2","ipflag1":"2","bgflag2":"2","ipflag2":"2","remark":"aaaaaaaa"}]}
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
         * logno : 58e1217fb12ce37e97faa56141543c84
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
         * pageinfo : {"totalcount":1,"totalpage":1,"pagesize":20,"currentpage":1}
         * friendlist : [{"friuserno":"5dcc43ebdb776045682e93178336c715","userid":"test001","name":"哈哈","sex":"2","birthday":"20000601","avatarno":"","avatarsuburl":"","bgflag1":"2","ipflag1":"2","bgflag2":"2","ipflag2":"2","remark":"aaaaaaaa"}]
         */

        private PageinfoBean pageinfo;
        private List<FriendlistBean> friendlist;

        public PageinfoBean getPageinfo() {
            return pageinfo;
        }

        public void setPageinfo(PageinfoBean pageinfo) {
            this.pageinfo = pageinfo;
        }

        public List<FriendlistBean> getFriendlist() {
            return friendlist;
        }

        public void setFriendlist(List<FriendlistBean> friendlist) {
            this.friendlist = friendlist;
        }

        public static class PageinfoBean {
            /**
             * totalcount : 1
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

        public static class FriendlistBean {
            /**
             * friuserno : 5dcc43ebdb776045682e93178336c715
             * userid : test001
             * name : 哈哈
             * sex : 2
             * birthday : 20000601
             * avatarno :
             * avatarsuburl :
             * bgflag1 : 2
             * ipflag1 : 2
             * bgflag2 : 2
             * ipflag2 : 2
             * remark : aaaaaaaa
             */

            private String friuserno;
            private String userid;
            private String name;
            private String sex;
            private String birthday;
            private String avatarno;
            private String avatarsuburl;
            private String bgflag1;
            private String ipflag1;
            private String bgflag2;
            private String ipflag2;
            private String remark;

            public String getFriuserno() {
                return friuserno;
            }

            public void setFriuserno(String friuserno) {
                this.friuserno = friuserno;
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

            public String getAvatarsuburl() {
                return avatarsuburl;
            }

            public void setAvatarsuburl(String avatarsuburl) {
                this.avatarsuburl = avatarsuburl;
            }

            public String getBgflag1() {
                return bgflag1;
            }

            public void setBgflag1(String bgflag1) {
                this.bgflag1 = bgflag1;
            }

            public String getIpflag1() {
                return ipflag1;
            }

            public void setIpflag1(String ipflag1) {
                this.ipflag1 = ipflag1;
            }

            public String getBgflag2() {
                return bgflag2;
            }

            public void setBgflag2(String bgflag2) {
                this.bgflag2 = bgflag2;
            }

            public String getIpflag2() {
                return ipflag2;
            }

            public void setIpflag2(String ipflag2) {
                this.ipflag2 = ipflag2;
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
