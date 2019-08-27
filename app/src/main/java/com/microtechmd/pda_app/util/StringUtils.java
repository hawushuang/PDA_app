/**
 * StringUtils.java
 *
 * @Title:
 * @Description:
 * @Copyright:微泰医疗器械(杭州)有限公司 Copyright (c) 2015
 * @Company:微泰医疗器械(杭州)有限公司
 * @Created on 2015-5-18 下午5:22:06
 * @author lijianhang
 * @version 1.0
 */

package com.microtechmd.pda_app.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static com.microtechmd.pda_app.constant.Constant.CHARSET_ENCODING;

/**
 * @author lijianhang
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isEmpty(String str) {

        return str == null || str.trim().length() == 0;
    }

    public static boolean isNotEmpty(String str) {

        return str != null && str.trim().length() > 0;
    }

    public static String nullToStrTrim(String str) {

        if (str == null) {
            str = "";
        }

        return str.trim();
    }

    public static String encode(String str) {

        String strEncode = "";

        try {
            strEncode = URLEncoder.encode(str, CHARSET_ENCODING);
        } catch (UnsupportedEncodingException e) {
        }

        return strEncode;
    }

    public static String decode(String str) {

        String strDecode = "";

        try {
            if (str != null)
                strDecode = URLDecoder.decode(str, CHARSET_ENCODING);
        } catch (UnsupportedEncodingException e) {
        }

        return strDecode;
    }


    public static String unitFormat(int i) {
        String retStr;
        if (i >= 0 && i < 10) {
            retStr = "0" + Integer.toString(i);
        } else {
            retStr = "" + i;
        }
        return retStr;
    }
}
