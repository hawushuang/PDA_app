package com.microtechmd.pda_app.network;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.microtechmd.pda_app.activity.SafetyWarningActivity;
import com.microtechmd.pda_app.activity.UserProfileActivity;
import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.entity.BaseMsgEntity;
import com.microtechmd.pda_app.network.okhttp_util.OkHttpUtils;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.util.JSONUtils;
import com.microtechmd.pda_app.util.MD5Utils;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;

import java.util.Map;

import okhttp3.MediaType;

import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;

/**
 * Created by Administrator on 2018/6/15.
 */

public class NetPostUtil {
    public static void doAction(String accessId, String signKey,
                                Map<String, Object> reqcontent, String action,
                                MyStringCallback callback) {
        try {
            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent).trim());
            String headerData = HttpHeader.getHeaderData(accessId, action, reqMd5);
            String headerSign = HttpHeader.getHeaderSign(headerData, signKey);
            String json = JSONUtils.toJSON(reqcontent).trim();

            OkHttpUtils
                    .postString()
                    .url(HOST + APISERVICE_VERSION + "/" + Constant.APISERVICE)
                    .addHeader("x-api-data", headerData)
                    .addHeader("x-api-sign", headerSign)
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .content(json)
                    .build()
                    .execute(callback);
//            ViseHttp.POST(Constant.APISERVICE)
//                    .addHeader("x-api-data", headerData)
//                    .addHeader("x-api-sign", headerSign)
//                    .setJson(JSONUtils.toJSON(reqcontent).trim())
//                    .request(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
