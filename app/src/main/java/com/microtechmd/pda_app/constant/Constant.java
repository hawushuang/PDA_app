/**
 * Constant.java
 *
 * @Title:
 * @Description:
 * @Copyright:微泰医疗器械(杭州)有限公司 Copyright (c) 2015
 * @Company:微泰医疗器械(杭州)有限公司
 * @Created on 2015-5-18 上午9:24:33
 * @version 1.0
 */

package com.microtechmd.pda_app.constant;

public final class Constant {

    // public static final String HOST = "http://127.0.0.1:8088/core/";

    // 外网地址
//    public static final String HOST = "http://www.microtechmd.com/core/";
    public static final String HOST = "http://api.microtechmd.com/cgms/";
//    public static final String HOST = "http://120.26.72.93:80/cgms/";
    //        public static final String HOST = "http://120.26.72.93:80/core/";
    public static final String APISERVICE_VERSION = "v1"; //服务版本v1
    public static final String APISERVICE = "http/APIService.json";

    public static String HTTPVERSION = "1.1";
    public static int REQUESTTIMEOUTINMILLIS = 60000;
    public static final String HTTPMETHOD_POST = "POST";
    public static final String HTTPMETHOD_PUT = "PUT";

    public static final String CHARSET_ENCODING = "UTF-8";
    public static final String CONTENTTYPE_JSON = "application/json";

    public static final String X_API_SOURCE = "11"; // 请求来源
    public static final String X_API_CHANNEL = ""; // 渠道
    public static final String X_API_TERMVER = ""; // 终端版本号
    public static final String X_API_CLIENTID = ""; // 客户端ID
    public static final String X_API_USERIP = ""; // 用户IP地址

    /**
     * 内网
     */

    // protected static final String ACCESSID_0 =
    // "985a07e9eadc163093d32b540ac02154";
    // protected static final String ACCESSKEY_0 =
    // "MzA5M2M5NDdiZDAyMDI1MzhkNTVlMzRiZGVjZmEwZDg=";
    //
    // protected static final String ACCESSID_1 =
    // "d328f8ce519d262ebb9b3e84c07010ec";
    // protected static final String ACCESSKEY_1 =
    // "Zjg1YWYwYTQyMjZlNjFkNzkyYjc2OTg3ZTFlNGU4NWY=";


    public static final String ACTVER_V1 = "v1"; // 接口版本
    /**
     * 外网
     */
    // 公用
    public static final String ACCESSID_PUBLIC = "35099ed4cfe0e6b0fdeb6b7e5949f904";
    public static final String ACCESSKEY_PUBLIC = "NzE3ZDdmNmY3MzdkZTA3NjNiYWQyZGY5MWM0MTViYWE=";

    // 用户
    public static final String ACCESSID_USER = "be2b80eed2a30fc5672a881467e99d84";
    public static final String ACCESSKEY_USER = "OTJkMDQ5YzkwNTlkYTIzMWJlMTkxYzQ5MWYzYzg3YjE=";

}
