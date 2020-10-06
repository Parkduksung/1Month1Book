package com.rsupport.mobile.agent.modules.push;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.WorkerThread;

import com.rsupport.mobile.agent.modules.push.command.IPushCommand;
import com.rsupport.mobile.agent.modules.push.command.RegisterCommand;
import com.rsupport.mobile.agent.modules.push.command.SetServerInfoCommand;
import com.rsupport.mobile.agent.modules.push.command.UnRegisterCommand;
import com.rsupport.mobile.agent.modules.push.delegate.RSPushNotificationDelegate;
import com.rsupport.mobile.agent.modules.push.service.CommandExecutor;
import com.rsupport.mobile.agent.modules.push.service.RSPushService;
import com.rsupport.util.log.RLog;

public class RSPushMessaging {
    private final int BIND_TIME_OUT = 500;
    private final Object lockObject = new Object();
    private final CommandExecutor commandExecutor = new CommandExecutor();


    private Context context = null;
    private IRSBinder binderService = null;
    private boolean isBinded = false;
    private RSPushNotificationDelegate pushDelegate;

    private static RSPushMessaging instance = null;
    private Boolean isPublisherReconnect = false;

    private RSPushMessaging() {

    }

    public synchronized static RSPushMessaging getInstance() {
        if (instance == null) {
            instance = new RSPushMessaging();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    @WorkerThread
    public boolean register(String registerID) {
        synchronized (lockObject) {
            if (!isBound()) bindService();

            if (!isBound()) {
                retryCommandExecute(new RegisterCommand(context, registerID));
                return false;
            }
            binderService.getRSPushService().register(context, registerID);
            return true;
        }
    }

    @WorkerThread
    public boolean unregister(String registerID) {
        RLog.w("unregister");
        synchronized (lockObject) {
            if (!isBound()) bindService();
            if (!isBound()) {
                retryCommandExecute(new UnRegisterCommand(context, registerID));
                return false;
            }

            binderService.getRSPushService().unregister(context, registerID);
            return true;
        }
    }

    @WorkerThread
    public boolean setServerInfo(String privateAddress, int privatePort) {
        synchronized (lockObject) {
            if (!isBound()) bindService();
            if (!isBound()) {
                retryCommandExecute(new SetServerInfoCommand(context, privateAddress, privatePort));
                return false;
            }
            binderService.getRSPushService().setServerInfo(privateAddress, privatePort);
            return true;
        }
    }

    @WorkerThread
    public void send(String topic, String message) {
        synchronized (lockObject) {
            if (!isBound()) bindService();
            if (!isBound()) {
                RLog.w("send message failure." + topic + ", message: " + message);
                return;
            }

            binderService.getRSPushService().pushNotification(topic, message);
        }
    }

    @WorkerThread
    public void send(String topic, byte[] message) {
        synchronized (lockObject) {
            if (!isBound()) bindService();
            if (!isBound()) {
                RLog.w("send message failure." + topic + ", message: " + message);
                return;
            }
            binderService.getRSPushService().pushNotification(topic, message);
        }
    }

    @WorkerThread
    public void clear() {
        synchronized (lockObject) {
            if (isBound()) {
                unbindService();
            }
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RLog.i("onServiceConnected");
            if (service instanceof IRSBinder) {
                binderService = (IRSBinder) service;
                binderService.getRSPushService().setPushDelegate(pushDelegate);
                binderService.getRSPushService().setPublisherReconnect(isPublisherReconnect);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            RLog.i("onServiceDisconnected");
            binderService = null;
            isBinded = false;
        }
    };

    private boolean waitForBind(int timeOut, String message) throws InterruptedException {
        if (binderService != null) {
            return true;
        }

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < timeOut) {
            if (binderService != null) {
                return true;
            }
            Thread.sleep(100);
        }
        RLog.w("waitForBind fail.(" + message + ")");
        return false;
    }

    private void unbindService() {
        context.unbindService(connection);
        binderService = null;
        isBinded = false;
    }

    private boolean bindService() {
        if (isBound()) return true;
        Intent intent = new Intent(context, RSPushService.class);
        isBinded = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        try {
            waitForBind(BIND_TIME_OUT, "bindService");
        } catch (InterruptedException ignored) {
        }
        return isBound();
    }

    private boolean isBound() {
        synchronized (lockObject) {
            return (isBinded && binderService != null);
        }
    }

    private void retryCommandExecute(IPushCommand command) {
        RLog.i("retryCommandExecute : " + command.getType());
        commandExecutor.execute(command);
    }

    /**
     * Application 에서 설정해야한다.
     *
     * @param pushDelegate
     */
    public void setPushDelegate(RSPushNotificationDelegate pushDelegate) {
        this.pushDelegate = pushDelegate;
    }

    /**
     * Application 에서 설정해야한다.
     *
     * @param isPublisherReconnect true 면 접속이 끊기면 재시도한다. 그렇지 않으면 종료한다.
     */
    public void setPublisherReconnect(Boolean isPublisherReconnect) {
        this.isPublisherReconnect = isPublisherReconnect;
    }
}
