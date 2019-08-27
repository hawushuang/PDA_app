/**
 * CoreUtil.java
 *
 * @Title:
 * @Description:
 * @Copyright:微泰医疗器械(杭州)有限公司 Copyright (c) 2015
 * @Company:微泰医疗器械(杭州)有限公司
 * @Created on 2015-5-18 上午9:31:25
 * @author lijianhang
 * @version 1.0
 */

package com.microtechmd.pda_app.util;

import static com.microtechmd.pda_app.constant.Constant.APISERVICE;
import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;

/**
 * @author lijianhang
 */
public final class CoreUtil {

    private CoreUtil() {
    }

    public static String getApiServiceHost(String server) {

        if (StringUtils.isEmpty(server)) {
            server = APISERVICE_VERSION;
        }

        return HOST + server.toLowerCase() + APISERVICE;
    }

    public static String dealPwd(String pwd) {

        pwd = MD5Utils.md5d(pwd).toLowerCase();

        return pwd;
    }

}
