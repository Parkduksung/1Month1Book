package com.rsupport.mobile.agent.service.command;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.api.ApiService;
import com.rsupport.mobile.agent.api.model.ConnectAgreeResult;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.extension.ThrowableKt;
import com.rsupport.mobile.agent.ui.dialog.AgentDialog;
import com.rsupport.mobile.agent.ui.dialog.AgentDialogActivity;
import com.rsupport.mobile.agent.ui.dialog.RVDialog;
import com.rsupport.mobile.agent.utils.Result;
import com.rsupport.util.log.RLog;

import org.koin.java.KoinJavaComponent;

import config.EngineConfigSetting;
import control.Converter;


public class AgentCommand5048 extends AgentCommandBasic {

    RemoteControlAgreeData agreeData;

    class RemoteControlAgreeData {
        String loginID;
        String logKey;
        int waitTime;
        String resultPage;
    }

    @Override
    public int agentCommandexe(byte[] data, int index) {

        readRemoteControlAgreeData(data, index);

        remoteAccessDialogShow();
        return 0;
    }

    private void readRemoteControlAgreeData(byte[] data, int startIndex) {
        int index = startIndex;
        int size = 0;

        agreeData = new RemoteControlAgreeData();

        // LoginID
        size = Converter.readIntLittleEndian(data, index);
        index += 4;
        agreeData.loginID = new String(data, index, size, EngineConfigSetting.UTF_8);
        index += size;

        // Logkey
        size = Converter.readIntLittleEndian(data, index);
        index += 4;
        agreeData.logKey = new String(data, index, size, EngineConfigSetting.UTF_8);
        index += size;

        // WaitTime
        agreeData.waitTime = Converter.readIntLittleEndian(data, index);
        index += 4;
        // Agree Result Page
        size = Converter.readIntLittleEndian(data, index);
        index += 4;
        agreeData.resultPage = new String(data, index, size, EngineConfigSetting.UTF_8);
        index += size;

    }

    public void remoteAccessDialogShow() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
//				AgentCommandFunction.acquireWakeLock(AgentMainService.service);
                KeyguardManager km = (KeyguardManager) Global.getInstance().getAppContext().getSystemService(Context.KEYGUARD_SERVICE);

                Boolean isScreen = km.inKeyguardRestrictedInputMode();
                if (!isScreen) {
                    AgentDialog.showDialog(Global.getInstance().getAppContext(), null, R.string.rmoteview_agree_dialog, R.string.computer_active_ok, new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Result<ConnectAgreeResult> agentConnectAgreeResult = KoinJavaComponent.get(ApiService.class).agentConnectAgreeResult(AgentBasicInfo.getAgentGuid(Global.getInstance().getAppContext()), agreeData.logKey, "1", agreeData.resultPage);
                                    if (!agentConnectAgreeResult.isSuccess()) {
                                        int errorCode = ThrowableKt.getErrorCode(((Result.Failure<ConnectAgreeResult>) agentConnectAgreeResult).getThrowable());
                                        RLog.d("call ok SessionResult = " + agentConnectAgreeResult + ", " + errorCode);
                                    }
//									AgentCommandFunction.releaseWakeLock();
                                }
                            }).start();
                        }
                    }, R.string.computer_active_cancel, new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Result<ConnectAgreeResult> connectAgreeResult = KoinJavaComponent.get(ApiService.class).agentConnectAgreeResult(AgentBasicInfo.getAgentGuid(Global.getInstance().getAppContext()), agreeData.logKey, "0", agreeData.resultPage);
                                    ;
                                    if (!connectAgreeResult.isSuccess()) {
                                        int errorCode = ThrowableKt.getErrorCode(((Result.Failure<ConnectAgreeResult>) connectAgreeResult).getThrowable());
                                        RLog.d("call ok SessionResult = " + connectAgreeResult + ", " + errorCode);
                                    }
//									AgentCommandFunction.releaseWakeLock();
                                }
                            }).start();
                        }
                    }, RVDialog.STYLE_NOTICE, agreeData.waitTime * 1000, null);
                } else {
                    Intent popupIntent = new Intent(Global.getInstance().getAppContext(), AgentDialogActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 그리고 호출한다.
                    popupIntent.putExtra(AgentDialogActivity.AGREE_DATA_LOGIN_ID, agreeData.loginID);
                    popupIntent.putExtra(AgentDialogActivity.AGREE_DATA_LOGIN_KEY, agreeData.logKey);
                    popupIntent.putExtra(AgentDialogActivity.AGREE_DATA_LOGIN_RESULT_PAGE, agreeData.resultPage);
                    popupIntent.putExtra(AgentDialogActivity.AGREE_DATA_LOGIN_WAIT_TIME, agreeData.waitTime);
                    Global.getInstance().getAppContext().startActivity(popupIntent);
                }

            }
        });
    }
}
