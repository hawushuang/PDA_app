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

package com.microtechmd.pda_app.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author lijianhang
 *
 */
public final class JSONUtils {

	private JSONUtils() {
    }

	public static String toJSON(Object object) throws Exception {

		if (object == null) {
			return "";
		}

		return JSON.toJSONString(object, 
				SerializerFeature.DisableCircularReferenceDetect, 
				SerializerFeature.WriteMapNullValue, 
				SerializerFeature.WriteNullStringAsEmpty).trim();
	}


}
