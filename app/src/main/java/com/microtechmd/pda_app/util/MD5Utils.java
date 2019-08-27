/**
 * MD5Utils.java
 *
 * @Title:
 * @Description:
 * @Copyright:微泰医疗器械(杭州)有限公司 Copyright (c) 2015
 * @Company:微泰医疗器械(杭州)有限公司
 * @Created on 2015-5-18 上午9:16:57
 * @author lijianhang
 * @version 1.0
 */

package com.microtechmd.pda_app.util;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author lijianhang
 */
public final class MD5Utils {

    private MD5Utils() {
    }

    public static String md5(String input) {

//        return DigestUtils.md5Hex(input);
        return new String(Hex.encodeHex(DigestUtils.md5(input)));
    }

    public static String md5d(String input) {

        return md5(md5(input).toLowerCase());
    }

    public static String md5File(String filename) throws Exception {

        InputStream inputStream = new FileInputStream(filename);

        try {
//            return DigestUtils.md5Hex(inputStream).toLowerCase();
            return (new String(Hex.encodeHex(DigestUtils.md5(inputStream)))).toLowerCase();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            WebUtils.closeInputStream(inputStream);
        }
    }

}
