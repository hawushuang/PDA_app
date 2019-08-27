package com.microtechmd.pda_app.network.okhttp_util.callback;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.activity.WelcomeActivity;
import com.microtechmd.pda_app.entity.BaseMsgEntity;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by zhy on 15/12/14.
 */
public class MyStringCallback extends StringCallback {
    private ActivityPDA context;

    public MyStringCallback() {
    }

    protected MyStringCallback(ActivityPDA context) {
        this.context = context;
    }

    @Override
    public void onBefore(Request request, int id) {
//        if (context != null) {
//            context.showDialogProgress();
//        }
    }

    @Override
    public void onAfter(int id) {
//        if (context != null) {
//            context.dismissDialogProgress();
//        }
    }

    @Override
    public void onError(Call call, Exception e, int id) {
        e.printStackTrace();
        if (context != null) {
            Toast.makeText(context, R.string.toast_network_connection_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResponse(String response, int id) {
        try {

            BaseMsgEntity baseModel = new Gson().fromJson(response, BaseMsgEntity.class);
            if (baseModel == null) {
                return;
            }
            if (baseModel.getInfo().getCode() == 120014) {
                context.startActivity(new Intent(context, WelcomeActivity.class));
                context.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void inProgress(float progress, long total, int id) {
    }
}
