package com.rsupport.mobile.agent.service;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.text.TextUtils;

import com.rsupport.mobile.agent.BuildConfig;
import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.api.ApiService;
import com.rsupport.mobile.agent.api.model.AgentLoginResult;
import com.rsupport.mobile.agent.api.model.AgentLogoutResult;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.constant.legacy.AgentLoginResultUpdater;
import com.rsupport.mobile.agent.extension.ThrowableKt;
import com.rsupport.mobile.agent.modules.push.IAgentPushCallBack;
import com.rsupport.mobile.agent.modules.push.IPushMessaging;
import com.rsupport.mobile.agent.modules.push.RSPushMessaging;
import com.rsupport.mobile.agent.receiver.AgentPushReceiver;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;
import com.rsupport.mobile.agent.status.AgentStatus;
import com.rsupport.mobile.agent.utils.AgentLogManager;
import com.rsupport.mobile.agent.utils.Result;
import com.rsupport.rscommon.errorlog.RSErrorLog;
import com.rsupport.rscommon.exception.RSException;
import com.rsupport.util.log.RLog;

import org.koin.java.KoinJavaComponent;

import kotlin.Lazy;

public class AgentLoginManager {
    private Context context;
    private static AgentLoginManager instance = null;
    public static Thread AgentLoginThread = null;
    public static boolean isLoginThread = true;

    private Lazy<ConfigRepository> configRepositoryLazy = KoinJavaComponent.inject(ConfigRepository.class);
    private Lazy<AgentStatus> agentStatusLazy = KoinJavaComponent.inject(AgentStatus.class);

    public synchronized static AgentLoginManager getInstence() {
        if (instance == null) {
            instance = new AgentLoginManager();
        }
        return instance;
    }

    private AgentLoginManager() {
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    public void startMQTTPushService(final IAgentPushCallBack callback) {
        // load instens......
        isLoginThread = true;
        if (AgentLoginThread != null) {
            AgentLoginThread.interrupt();
            RLog.d("AgentLoginThread is Running!!!!");
        }

        RLog.d("startMQTTPushService!!!!");
        AgentLoginThread = new Thread(() -> {
            Looper.prepare();
            Result<AgentLoginResult> agentLoginResult;

            String guid = AgentBasicInfo.getAgentGuid(context);

            GlobalStatic.loadSettingInfo(context);

            AgentLogManager agentLogManager = KoinJavaComponent.get(AgentLogManager.class);

            agentLogManager.addAgentLog(context, String.format(context.getString(R.string.agent_log_agent_login), configRepositoryLazy.getValue().getServerInfo().getUrl()));

            agentLoginResult = KoinJavaComponent.get(ApiService.class).agentLogin(guid);

            if (agentLoginResult instanceof Result.Failure) {
                int errorCode = ThrowableKt.getErrorCode(((Result.Failure<AgentLoginResult>) agentLoginResult).getThrowable());
                RLog.i("agentLoginError" + errorCode + ", ");
                agentLogManager.addAgentLog(context, String.format(context.getString(R.string.agent_log_agent_login_faile), "false", String.valueOf(errorCode)));
                if (errorCode == 212) { //삭제된 컴퓨터
                    RLog.d("deleteComputer!!! nostart demon!");
                } else if (errorCode == 113) { //등록되지 않은 컴퓨터
                    RLog.d("noResisterComputer!!! nostart demon!");
                } else if (errorCode == 215) { // 만료된 에이전트
                    RLog.d("expireComputer!!! nostart demon!");
                }
                Intent intent = new Intent(context, AgentPushReceiver.class);
                intent.setAction(IPushMessaging.ACTION_PUSH_MESSAGING);
                intent.putExtra(IPushMessaging.EXTRA_KEY_TYPE, IPushMessaging.TYPE_WEB_LOGIN_ERROR);
                intent.putExtra(IPushMessaging.EXTRA_KEY_VALUE, ("" + errorCode).getBytes());
                context.sendBroadcast(intent);


                Throwable throwable = ((Result.Failure<AgentLoginResult>) agentLoginResult).getThrowable();
                if (throwable instanceof RSException) {
                    RSException e = (RSException) throwable;
                    RLog.w(e);
                    RSErrorLog.report(String.valueOf(e.getErrorCode()), "AgentLoginManager Login", e.getDisplayMessage());
                }
            } else {
                AgentLoginResult agentLoginResultValue = ((Result.Success<AgentLoginResult>) agentLoginResult).getValue();
                new AgentLoginResultUpdater().update(agentLoginResultValue);

                if (!TextUtils.isEmpty(AgentBasicInfo.RV_AGENT_PUSHSERVER_PORT)) {
                    try {
                        RSPushMessaging messaging = RSPushMessaging.getInstance();
                        // 로그인을 하면 topic 를 다시 subscribe 한다.
                        messaging.setServerInfo(AgentBasicInfo.RV_AGENT_PUSHSERVER_ADDRESS, Integer.parseInt(AgentBasicInfo.RV_AGENT_PUSHSERVER_PORT));
                        //강제 업데이트 토픽추가 버젼 네임.
                        messaging.register(BuildConfig.VERSION_NAME);
                        messaging.register(guid + "/agent");
                    } catch (Exception e) {
                        RLog.e(e);
                    }
                }
            }
            if (callback != null) {
                callback.agentCallback(0);
            }
            AgentLoginThread = null;
        });

        AgentLoginThread.start();
    }

    public void stopMQTTPushService() {
        isLoginThread = false;
        if (AgentLoginThread != null) {
            try {
                AgentLoginThread.interrupt();
            } catch (Exception e) {
                RLog.e(e.getMessage());
            }

            RLog.d("AgentLoginThread is Running!!!!");
        }

        RLog.d("stopMQTTPushService!!!!");
        AgentLoginThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                String guid = AgentBasicInfo.getAgentGuid(context);

                if (guid.equals(""))
                    return;

                RSPushMessaging messaging = RSPushMessaging.getInstance();
                messaging.unregister(BuildConfig.VERSION_NAME);
                messaging.unregister(guid + "/agent");

                Result<AgentLogoutResult> ret = KoinJavaComponent.get(ApiService.class).agentLogout(guid);

                RLog.d("call manager agentLogout : " + ret);

                // Web 쿼리 오류가 발생되면 UI가 바뀌면 안됨
                if (ret.isSuccess()) {
                    agentStatusLazy.getValue().setLogOut();
                }
                RSPushMessaging.getInstance().clear();
                AgentLoginThread = null;
            }
        });
        AgentLoginThread.start();
    }

}
