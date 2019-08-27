package com.microtechmd.pda_app;


import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;
import android.util.Log;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.monitor.History;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.LogPDA;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.entity.LoginEntity;
import com.microtechmd.pda_app.entity.MyObjectBox;
import com.microtechmd.pda_app.network.okhttp_util.OkHttpUtils;
import com.microtechmd.pda_app.service.service_control.ServiceControl;
import com.microtechmd.pda_app.util.notificationUtil.NotifyUtil;
import com.mob.MobSDK;
import com.vise.xsnow.http.ViseHttp;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import okhttp3.OkHttpClient;

import static android.provider.MediaStore.Video.VideoColumns.LANGUAGE;
import static com.microtechmd.pda_app.constant.Constant.APISERVICE_VERSION;
import static com.microtechmd.pda_app.constant.Constant.HOST;
import static com.microtechmd.pda_app.constant.Constant.REQUESTTIMEOUTINMILLIS;


public class ApplicationPDA extends MultiDexApplication {
    public final static int KEY_CODE_BOLUS = 111;

    private static ApplicationPDA instance;

    public static ApplicationPDA getInstance() {
        return instance;
    }

    private static LogPDA sLog = null;
    private static Messenger sMessengerView = null;
    private static Messenger sMessengerControl = null;
    private static ControlConnection sControlConnection = null;
    private static MessageListenerInternal[] sMessageListenerInternal = null;

    private LoginEntity loginEntity;

    public LoginEntity getLoginEntity() {
        return loginEntity;
    }

    public void setLoginEntity(LoginEntity loginEntity) {
        this.loginEntity = loginEntity;
    }

    private BoxStore boxStore;

    public BoxStore getBoxStore() {
        return boxStore;
    }


    private ArrayList<History> dataListAll;

    public ArrayList<History> getDataListAll() {
        return dataListAll;
    }

    public void setDataListAll(ArrayList<History> dataListAll) {
        this.dataListAll = dataListAll;
    }
    // Inner class definition

    private static class MessageHandler extends Handler {
        private final WeakReference<ApplicationPDA> mApplication;


        private MessageHandler(ApplicationPDA application) {
            super();
            mApplication = new WeakReference<>(application);
        }


        @Override
        public void handleMessage(Message msg) {
            EntityMessage message = new EntityMessage();
            message.setAll(msg.getData());
            ApplicationPDA application = mApplication.get();
            application.handleMessage(message);
        }
    }

    private class ControlConnection
            implements
            ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sLog.Debug(getClass(), "Connect control service");
            sMessengerControl = new Messenger(service);
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            sLog.Debug(getClass(), "Disconnect control service");
            sMessengerControl = null;
        }
    }

    private class MessageListenerInternal extends ArrayList<EntityMessage.Listener> {
        private static final long serialVersionUID = 1L;
    }


    // Method definition
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        String language = (String) SPUtils.get(this, LANGUAGE, Locale.getDefault().getLanguage());
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        assert language != null;
        if (language.equals("zh")) {
            configuration.setLocale(Locale.SIMPLIFIED_CHINESE);
        } else {
            configuration.setLocale(Locale.ENGLISH);
        }
        resources.updateConfiguration(configuration, metrics);

        boxStore = MyObjectBox.builder().androidContext(this).build();
        if (BuildConfig.DEBUG) {
            boolean started = new AndroidObjectBrowser(boxStore).start(this);
            Log.e("ObjectBrowser", "Started: " + started);
        }

        MobSDK.init(this);
        NotifyUtil.init(this);
        ViseHttp.init(this);
        ViseHttp.CONFIG()
                //配置请求主机地址
                .baseUrl(HOST + APISERVICE_VERSION + "/")
                .connectTimeout(REQUESTTIMEOUTINMILLIS, TimeUnit.MILLISECONDS)
                .setCookie(true);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
        if (sLog == null) {
            sLog = new LogPDA();
        }

        if (sMessengerView == null) {
            sMessengerView =
                    new Messenger(new MessageHandler(ApplicationPDA.this));
        }

        if (sControlConnection == null) {
            sControlConnection = new ControlConnection();
            bindService(new Intent(this, ServiceControl.class),
                    sControlConnection, Context.BIND_AUTO_CREATE);
        }

        if (sMessageListenerInternal == null) {
            sMessageListenerInternal =
                    new MessageListenerInternal[ParameterGlobal.COUNT_PORT];

            for (int i = 0; i < ParameterGlobal.COUNT_PORT; i++) {
                sMessageListenerInternal[i] = new MessageListenerInternal();
            }
        }
    }


    public void registerMessageListener(int port,
                                        final EntityMessage.Listener listener) {
        if (port < ParameterGlobal.COUNT_PORT) {
            sMessageListenerInternal[port].add(listener);
        }
    }


    public void unregisterMessageListener(int port,
                                          final EntityMessage.Listener listener) {
        if (port < ParameterGlobal.COUNT_PORT) {
            sMessageListenerInternal[port].remove(listener);
        }
    }


    public void clearMessageListener(int port) {
        if (port < ParameterGlobal.COUNT_PORT) {
            sMessageListenerInternal[port].clear();
        }
    }


    public void handleMessage(final EntityMessage message) {
        switch (message.getTargetAddress()) {
            case ParameterGlobal.ADDRESS_LOCAL_VIEW:
                handleMessageInternal(message);
                break;

            default:
                sendRemoteMessage(sMessengerControl, message);
                break;
        }
    }


    private void handleMessageInternal(final EntityMessage message) {
        int port;

        port = message.getTargetPort();

        if (port < ParameterGlobal.COUNT_PORT) {
            for (EntityMessage.Listener listener : sMessageListenerInternal[port]) {
                listener.onReceive(message);
            }
        }
    }


    private void sendRemoteMessage(final Messenger messenger,
                                   final EntityMessage message) {
        if (messenger != null) {
            Message messageRemote = Message.obtain();
            messageRemote.setData(message.getAll());
            messageRemote.replyTo = sMessengerView;

            try {
                messenger.send(messageRemote);
            } catch (RemoteException e) {
                sLog.Debug(getClass(), "Send remote message fail");
                e.printStackTrace();
            }
        }
    }
}
