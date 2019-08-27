package com.microtechmd.pda_app.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microtechmd.pda.library.entity.DataList;
import com.microtechmd.pda.library.entity.monitor.History;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.ApplicationPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.activity.SafetyWarningActivity;
import com.microtechmd.pda_app.activity.UserProfileActivity;
import com.microtechmd.pda_app.constant.Constant;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.BaseMsgEntity;
import com.microtechmd.pda_app.entity.DbRawHistory;
import com.microtechmd.pda_app.entity.UserDataEntity;
import com.microtechmd.pda_app.network.HttpHeader;
import com.microtechmd.pda_app.network.okhttp_util.OkHttpUtils;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;

import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE_INVALID;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE_RECOMMEND_CAL;
import static com.microtechmd.pda_app.ActivityPDA.IMPENDANCE;
import static com.microtechmd.pda_app.ActivityPDA.SETTING_RF_ADDRESS;
import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;

/**
 * Created by Administrator on 2017/12/25.
 */

public class UploadRawDataUtil {

    private static String bytestoString(byte[] arr) {
        //1,定义字符串变量。
        StringBuilder temp = new StringBuilder();
        //2,遍历数组。将每一个数组的元素和字符串相连接。
        for (int x = 0; x < arr.length; x++) {
            //判断，不是最后一个元素，后面连接逗号，是最后一个元素，后面不连接逗号。
            if (x != arr.length - 1) {
                temp.append(arr[x]).append(",");
            } else {
                temp.append(arr[x]);
            }
        }
        //3，将连接后的字符串返回。
        return temp.toString();
    }

    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static String getAddress(byte[] addressByte) {
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

    public static void addUserData(final Context context, List<DbRawHistory> rawHistoryList) {
        try {
            String accessId = (String) SPUtils.get(context, StringConstant.ACCESSID, "");
            String signKey = (String) SPUtils.get(context, StringConstant.SIGNKEY, "");
            List<Map<String, Object>> datalist = new ArrayList<>();
            Map<String, Object> reqcontent_list = new LinkedHashMap<>();

            for (int i = 0; i < rawHistoryList.size(); i++) {
                DbRawHistory rawHistory = rawHistoryList.get(i);
                Map<String, Object> reqcontent = new LinkedHashMap<>();
                reqcontent.put("devicetype", "1");
                reqcontent.put("deviceid", rawHistory.getDevice_id());
                reqcontent.put("recordid", rawHistory.getId());
                reqcontent.put("recordtime", TimeUtil.getSysTimeL());
                reqcontent.put("eventindex", rawHistory.getEvent_index());
                reqcontent.put("eventport", rawHistory.getSensorIndex());
                reqcontent.put("eventtype", rawHistory.getEvent_type());
                reqcontent.put("devicetime", rawHistory.getDate_time());
                reqcontent.put("eventlevel", rawHistory.getSplit());
                reqcontent.put("eventdata", rawHistory.getValue());
                reqcontent.put("parameter1", rawHistory.getP1());
                reqcontent.put("parameter2", rawHistory.getP2());
                reqcontent.put("parameter3", rawHistory.getP3());
                reqcontent.put("parameter4", rawHistory.getP4());
                datalist.add(reqcontent);
            }
            reqcontent_list.put("datalist", datalist);
            String reqMd5 = MD5Utils.md5(JSONUtils.toJSON(reqcontent_list).trim());
            String headerData = HttpHeader.getHeaderData(accessId, "userdataAdd", reqMd5);
            String headerSign = HttpHeader.getHeaderSign(headerData, signKey);
            String json = JSONUtils.toJSON(reqcontent_list).trim();

            OkHttpUtils
                    .postString()
                    .url(HOST + APISERVICE_VERSION + "/" + Constant.APISERVICE)
                    .addHeader("x-api-data", headerData)
                    .addHeader("x-api-sign", headerSign)
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .content(json)
                    .build()
                    .execute(new MyStringCallback((ActivityPDA) context) {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(context, R.string.toast_network_connection_failed, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            super.onResponse(response, id);
                            if (TextUtils.isEmpty(response)) {
                                return;
                            }
                            UserDataEntity authorModel = new Gson().fromJson(response, UserDataEntity.class);
                            if (authorModel == null) {
                                return;
                            }
                            if (authorModel.getInfo().getCode() != 100000) {
                                Toast.makeText(context, authorModel.getInfo().getMsg(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            for (UserDataEntity.ContentBean.DatalistBean bean : authorModel.getContent().getDatalist()) {
                                if (bean.getCode() == 100000) {
                                    try {
                                        ApplicationPDA app = (ApplicationPDA) (((Activity) context).getApplication());
                                        DbRawHistory dbData = app.getBoxStore().boxFor(DbRawHistory.class).get(Long.parseLong(bean.getRecordid()));
                                        dbData.setRecord_no(bean.getRecordno());
                                        app.getBoxStore().boxFor(DbRawHistory.class).put(dbData);
                                    } catch (Exception e) {
                                        Log.e("IMPENDANCE", e.getMessage());
                                    }
                                }
                            }
                        }
                    });
//            ViseHttp.POST(Constant.APISERVICE)
//                    .addHeader("x-api-data", headerData)
//                    .addHeader("x-api-sign", headerSign)
//                    .setJson(JSONUtils.toJSON(reqcontent_list).trim())
//                    .request(new ACallback<UserDataEntity>() {
//                        @Override
//                        public void onSuccess(UserDataEntity authorModel) {
//                            if (authorModel == null) {
//                                return;
//                            }
//                            if (authorModel.getInfo().getCode() != 100000) {
//                                Toast.makeText(context, authorModel.getInfo().getMsg(), Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                            for (UserDataEntity.ContentBean.DatalistBean bean : authorModel.getContent().getDatalist()) {
//                                if (bean.getCode() == 100000) {
//                                    try {
//                                        ApplicationPDA app = (ApplicationPDA) (((Activity) context).getApplication());
//                                        DbRawHistory dbData = app.getBoxStore().boxFor(DbRawHistory.class).get(Long.parseLong(bean.getRecordid()));
//                                        dbData.setRecord_no(bean.getRecordno());
//                                        app.getBoxStore().boxFor(DbRawHistory.class).put(dbData);
//                                    } catch (Exception e) {
//                                        Log.e("IMPENDANCE", e.getMessage());
//                                    }
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onFail(int errCode, String errMsg) {
//                            Toast.makeText(context, R.string.toast_network_connection_failed, Toast.LENGTH_SHORT).show();
//                        }
//                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
