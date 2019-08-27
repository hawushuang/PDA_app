/**
 * JSONUtils.java
 *
 * @Title:
 * @Description:
 * @Copyright:微泰医疗器械(杭州)有限公司 Copyright (c) 2015
 * @Company:微泰医疗器械(杭州)有限公司
 * @Created on 2015-5-18 上午9:16:29
 * @author lijianhang
 * @version 1.0
 */

package com.microtechmd.pda_app.network;

import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.util.HmacSHA1Utils;
import com.microtechmd.pda_app.util.JSONUtils;
import com.microtechmd.pda_app.util.MD5Utils;
import com.microtechmd.pda_app.util.TimeUtil;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpHeader {
    private HttpHeader() {
    }

    public static String getHeaderData(String accessid, String action, String reqmd5) throws Exception {
        String reqtime = TimeUtil.getSysTimeL();
        Map<String, Object> xapidata = new LinkedHashMap<String, Object>();
        xapidata.put("source", Constant.X_API_SOURCE);
        xapidata.put("channel", Constant.X_API_CHANNEL);
        xapidata.put("termver", Constant.X_API_TERMVER);
        xapidata.put("clientid", Constant.X_API_CLIENTID);
        xapidata.put("userip", Constant.X_API_USERIP);
        xapidata.put("reqtime", reqtime);
        xapidata.put("actver", Constant.ACTVER_V1);
        xapidata.put("accessid", accessid);
        xapidata.put("action", action);
        xapidata.put("reqmd5", reqmd5);

        return JSONUtils.toJSON(xapidata).trim().replace(" ", "");
    }

    public static String getHeaderSign(String data, String signkey) throws Exception {
        return URLEncoder.encode(signature(data, signkey).trim(), Constant.CHARSET_ENCODING);
    }

    private static String signature(String data, String key) throws Exception {

        return HmacSHA1Utils.signatureString(MD5Utils.md5(data).toLowerCase(), key, Constant.CHARSET_ENCODING).trim();
    }

}
