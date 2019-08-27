/**
 * HmacSHA1Utils.java
 *
 * @Title:
 * @Description:
 * @Copyright:微泰医疗器械(杭州)有限公司 Copyright (c) 2015
 * @Company:微泰医疗器械(杭州)有限公司
 * @Created on 2015-5-18 上午9:16:13
 * @author lijianhang
 * @version 1.0
 */

package com.microtechmd.pda_app.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * @author lijianhang
 */
public final class HmacSHA1Utils {

    private HmacSHA1Utils() {
    }

    private static final String ALGORITHM = "HmacSHA1";

    private static byte[] signature(String data, String key, String charsetName)
            throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {

        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(charsetName), ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(signingKey);

        return mac.doFinal(data.getBytes(charsetName));
    }

    public static String signatureString(String data, String key, String charsetName)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {

//		return Base64.encodeBase64String(signature(data, key, charsetName)).trim();
        return Base64.encodeToString(signature(data, key, charsetName), Base64.DEFAULT).trim();
    }

}
