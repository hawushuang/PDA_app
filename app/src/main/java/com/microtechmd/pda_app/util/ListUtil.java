package com.microtechmd.pda_app.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/4/24.
 */

public class ListUtil {
    public static String listToString(List<String> list) {
        if (list == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean first = true;
        //第一个前面不拼接","
        for (String string : list) {
            if (first) {
                first = false;
            } else {
                result.append(",");
            }
            result.append(string);
        }
        return result.toString();
    }

    public static List<String> stringToList(String str) {
        return Arrays.asList(str.split(","));
    }
}
