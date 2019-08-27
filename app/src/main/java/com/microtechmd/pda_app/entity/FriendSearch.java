package com.microtechmd.pda_app.entity;

import java.util.List;

/**
 * Created by Administrator on 2018/6/13.
 */

public class FriendSearch {

    /**
     * info : {"code":100000,"msg":"成功","logno":"485f554e7a8f3b095811f1a9aa19b93e"}
     * content : {"pageinfo":{"totalcount":2,"totalpage":1,"pagesize":20,"currentpage":1},"userlist":[{"userno":"5dcc43ebdb776045682e93178336c715","userid":"test001","name":"哈哈","sex":"2","birthday":"20000601","avatarno":""},{"userno":"1173b1ffc92dd7af76ffe75103d9b409","userid":"testjhon666","name":"","sex":"","birthday":"","avatarno":""}]}
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
         * logno : 485f554e7a8f3b095811f1a9aa19b93e
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
         * pageinfo : {"totalcount":2,"totalpage":1,"pagesize":20,"currentpage":1}
         * userlist : [{"userno":"5dcc43ebdb776045682e93178336c715","userid":"test001","name":"哈哈","sex":"2","birthday":"20000601","avatarno":""},{"userno":"1173b1ffc92dd7af76ffe75103d9b409","userid":"testjhon666","name":"","sex":"","birthday":"","avatarno":""}]
         */

        private PageinfoBean pageinfo;
        private List<UserlistBean> userlist;

        public PageinfoBean getPageinfo() {
            return pageinfo;
        }

        public void setPageinfo(PageinfoBean pageinfo) {
            this.pageinfo = pageinfo;
        }

        public List<UserlistBean> getUserlist() {
            return userlist;
        }

        public void setUserlist(List<UserlistBean> userlist) {
            this.userlist = userlist;
        }

        public static class PageinfoBean {
            /**
             * totalcount : 2
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

        public static class UserlistBean {
            /**
             * userno : 5dcc43ebdb776045682e93178336c715
             * userid : test001
             * name : 哈哈
             * sex : 2
             * birthday : 20000601
             * avatarno :
             */

            private String userno;
            private String userid;
            private String name;
            private String sex;
            private String birthday;
            private String avatarno;

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
        }
    }
}
