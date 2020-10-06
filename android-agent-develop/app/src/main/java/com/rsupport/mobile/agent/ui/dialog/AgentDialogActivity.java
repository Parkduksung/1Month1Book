package com.rsupport.mobile.agent.ui.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.rsupport.mobile.agent.api.ApiService;
import com.rsupport.mobile.agent.api.model.ConnectAgreeResult;
import com.rsupport.mobile.agent.extension.ThrowableKt;
import com.rsupport.mobile.agent.utils.Result;
import com.rsupport.mobile.agent.R;
import com.rsupport.rscommon.exception.RSException;

import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.util.log.RLog;

import org.koin.java.KoinJavaComponent;

public class AgentDialogActivity extends AppCompatActivity {

    public final static String AGREE_DATA_LOGIN_ID = "AGREE_DATA_LOGIN_ID";
    public final static String AGREE_DATA_LOGIN_KEY = "AGREE_DATA_LOGIN_KEY";
    public final static String AGREE_DATA_LOGIN_RESULT_PAGE = "AGREE_DATA_LOGIN_RESULT_PAGE";
    public final static String AGREE_DATA_LOGIN_WAIT_TIME = "AGREE_DATA_LOGIN_WAIT_TIME";

    String loginID;
    String logKey;
    String resultPage;
    int waitTime;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 이 부분이 바로 화면을 깨우는 부분 되시겠다.
        // 화면이 잠겨있을 때 보여주기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                // 키잠금 해제하기
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                // 화면 켜기
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.notitle_fullscreen);
        Intent agreeData = getIntent();
        loginID = agreeData.getStringExtra(AGREE_DATA_LOGIN_ID);
        logKey = agreeData.getStringExtra(AGREE_DATA_LOGIN_KEY);
        resultPage = agreeData.getStringExtra(AGREE_DATA_LOGIN_RESULT_PAGE);
        waitTime = agreeData.getIntExtra(AGREE_DATA_LOGIN_WAIT_TIME, 0);
        // FIXME 다국어 처리 해야한다.
        AgentDialog.showDialog(this, null, loginID + "님으로부터 원격제어요청이 왔습니다.\n수락하시겠습니까?", R.string.computer_active_ok, new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Result<ConnectAgreeResult> connectAgreeResult = KoinJavaComponent.get(ApiService.class).agentConnectAgreeResult(AgentBasicInfo.getAgentGuid(Global.getInstance().getAppContext()), logKey, "1", resultPage);
                        ;
                        if (connectAgreeResult instanceof Result.Failure) {
                            Throwable throwable = ((Result.Failure<ConnectAgreeResult>) connectAgreeResult).getThrowable();
                            RLog.d("call ok SessionResult = " + connectAgreeResult + ", " + ThrowableKt.getErrorCode(throwable));
                        }
                    }
                }).start();
                finish();
            }
        }, R.string.computer_active_cancel, new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Result<ConnectAgreeResult> connectAgreeResult = KoinJavaComponent.get(ApiService.class).agentConnectAgreeResult(AgentBasicInfo.getAgentGuid(Global.getInstance().getAppContext()), logKey, "0", resultPage);
                        ;
                        if (connectAgreeResult instanceof Result.Failure) {
                            Throwable throwable = ((Result.Failure<ConnectAgreeResult>) connectAgreeResult).getThrowable();
                            RLog.d("call ok SessionResult = " + connectAgreeResult + ", " + ThrowableKt.getErrorCode(throwable));
                        }
                        finish();
                    }
                }).start();
                finish();
            }
        }, RVDialog.STYLE_NOTICE, waitTime * 1000, timeOutHandle);
    }

    Handler timeOutHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            finish();
            super.handleMessage(msg);
        }
    };
}
