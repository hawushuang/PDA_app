package com.microtechmd.pda_app.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/4/24.
 */

public class RfAddressUtil {
    public static String getAddress(byte[] addressByte) {
        if (addressByte != null) {
            for (int i = 0; i < addressByte.length; i++) {
                if (addressByte[i] < 10) {
                    addressByte[i] += '0';
                } else {
                    addressByte[i] -= 10;
                    addressByte[i] += 'A';
                }
            }

            return new String(addressByte);
        } else {
            return "";
        }
    }
}
