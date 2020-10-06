package com.rsupport.mobile.agent.ui.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.api.ApiService;
import com.rsupport.mobile.agent.api.model.AccountChangeResult;
import com.rsupport.mobile.agent.api.model.CheckAccessIDValidateResult;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.ui.dialog.RVDialog;
import com.rsupport.mobile.agent.utils.Result;
import com.rsupport.mobile.agent.utils.Utility;
import com.rsupport.util.log.RLog;

import org.koin.java.KoinJavaComponent;

import java.lang.ref.WeakReference;

public class AgentAccessAcountChangeActivity extends RVCommonActivity {


    EditText oldpasswd;
    EditText oldID;
    EditText newID;
    EditText newpasswd;
    EditText newpasswdConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_access_acount_change, R.layout.layout_common_bg_margin);
        setTitle(R.string.agent_access_account, true, false);
        setBottomTitle(R.string.save, null);

        setLeftButtonBackground(R.drawable.button_headerback);
        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        oldID = (EditText) findViewById(R.id.old_id);
        oldpasswd = (EditText) findViewById(R.id.old_passwd);
        newID = (EditText) findViewById(R.id.new_id);
        newpasswd = (EditText) findViewById(R.id.new_passwd);
        newpasswdConfirm = (EditText) findViewById(R.id.new_passwd_check);

        btnTitleLeft.setOnClickListener(v -> {
            hideKeyboard(oldID);
            finish();
        });
    }


    private boolean checkInputData() {
        //id 공백 체크
        if (oldID.getText().toString().trim().equals("") || oldID.length() == 0) {
            createAletDialog(getString(R.string.account_change_old_id_input_notice));
            return false;
        }

        // passwd 공백체크
        if (oldpasswd.getText().toString().trim().equals("") || oldpasswd.length() == 0) {
            createAletDialog(getString(R.string.account_change_old_passwd_input_notice));
            return false;
        }

        // newId 공백체크
        if (newID.getText().toString().trim().equals("") || newID.length() == 0) {
            createAletDialog(getString(R.string.account_change_new_account_des));
            return false;
        }

        //접근 아이디 정규식 체크
        if (!Utility.checkAcccountValidate(newID.getText().toString())) {
            createAletDialog(getString(R.string.account_change_new_id_wrong));
            return false;
        }

        // 접근 비밀번호 공백 체크
        if (newpasswd.getText().toString().trim().equals("") || newpasswd.length() == 0 ||
                newpasswdConfirm.getText().toString().trim().equals("") || newpasswdConfirm.length() == 0) {
            createAletDialog(getString(R.string.account_change_new_passwd_input_notice));
            return false;
        }

        // 접근 비밀번호 정규식 체크
        if (!invalidWordCheck(newpasswd.getText().toString())) {
            createAletDialog(getString(R.string.account_change_new_passwd_wrong));
            return false;
        }

        //비밀번호 불일치
        if (!newpasswd.getText().toString().equals(newpasswdConfirm.getText().toString())) {
            createAletDialog(getString(R.string.account_change_new_passwd_dont_same));
            return false;
        }
        //비밀번호 아이디와 동일
        if (newID.getText().equals(newpasswd.getText())) {
            createAletDialog(getString(R.string.account_change_new_passwd_id_same));
            return false;
        }
        return true;
    }

    private boolean invalidWordCheck(String str) {
        if (str.length() < 6 || str.length() > 24) {
            return false;
        }
        char[] err_char = new char[]{'\\', '/', ':', '*', '?', '\"', '<', '>', '|', '%', '+', ';'};
        for (int i = 0; i < str.length(); i++) {
            for (int j = 0; j < err_char.length; j++) {
                if (str.charAt(i) == err_char[j]) {
                    return false;
                }
            }

        }
        return true;
    }

    private void createAletDialog(String str) {
        showAlertDialog(null, str, RVDialog.STYLE_NOTICE, R.string.common_ok, 0, false);
    }

    @Override
    public void eventDelivery(int event) {
        if (event == 0) {
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    /**
     * 하단 타이틀바 (클릭버튼용) 기능 *
     */
    public void Click_ButtomTitle(View v) {
        if (checkInputData()) {
            installAgentPageCall(oldID.getText().toString(), oldpasswd.getText().toString(),
                    newID.getText().toString(), newpasswd.getText().toString());
        }


    }

    private void installAgentPageCall(final String oldId, final String oldPassWd, final String newid, final String newpassWd) {

        showProgressHandler(getString(R.string.remotepc_loading_waiting_message));
        new Thread(() -> {
            ApiService apiService = KoinJavaComponent.get(ApiService.class);
            Result<CheckAccessIDValidateResult> accessIDValidateResult = apiService.checkAccessIDValidate(newid, newpassWd, AgentBasicInfo.getAgentBizID(AgentAccessAcountChangeActivity.this));
            ;
            RLog.d("call checkAccessIDValidate : " + accessIDValidateResult);

            boolean ret = false;
            if (accessIDValidateResult.isSuccess()) {
                Result<AccountChangeResult> accountChangeResult = apiService.agentAccountChange(AgentBasicInfo.getAgentGuid(getApplicationContext()), oldId, oldPassWd, newid, newpassWd);
                ret = accountChangeResult.isSuccess();
                RLog.d("call agentInstall : " + accessIDValidateResult);
            }
            AccountChangeHandler accountChangeHandler = new AccountChangeHandler(AgentAccessAcountChangeActivity.this);
            accountChangeHandler.sendEmptyMessage(ret ? 1 : 0);
        }).start();
    }

    private static class AccountChangeHandler extends Handler {
        private WeakReference<AgentAccessAcountChangeActivity> weakReference;

        public AccountChangeHandler(AgentAccessAcountChangeActivity activity) {
            super(Looper.getMainLooper());
            weakReference = new WeakReference<>(activity);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void handleMessage(Message msg) {
            AgentAccessAcountChangeActivity activity = weakReference.get();
            if (activity == null) {
                return;
            }

            activity.hideProgressHandler();
            if (msg.what == 0) {
                String errorMsg = "";
                switch (GlobalStatic.g_errNumber) {
                    case 111: // 파라미터 부족
                        errorMsg = activity.getString(R.string.msg_inputlogininfo);
                        break;
                    case 113: // 등록되지 않은 컴퓨터
                        errorMsg = activity.getString(R.string.msg_inputlogininfo);
                        break;
                    case 114: // 잘못된 AgentID 또는 패스워드
                        errorMsg = activity.getString(R.string.account_change_old_passwd_wrong);
                        break;
                    case 400: // Agent설치, Agent접근ID가 유효성검사에 실패
                        errorMsg = activity.getString(R.string.agent_install_id) + " :\n" + activity.getString(R.string.agent_install_rule_id);
                        break;
                    case 401: // Agent설치, Agent접근ID암호가 유효성검사에 실패
                        errorMsg = activity.getString(R.string.agent_install_pass) + " :\n" + activity.getString(R.string.account_change_new_passwd_wrong);
                        break;
                    case 402: // Agent설치, 웹 ID와 동일한 비밀번호 설정 불가
                        errorMsg = activity.getString(R.string.agent_install_rule_equal);
                        break;
                    case 901: // 접속패스워드는 8~24자, 영문+숫자 조합입니다.
                        errorMsg = activity.getString(R.string.agent_install_pass) + " :\n" + activity.getString(R.string.account_change_new_passwd_wrong);
                        break;
                    case 911: // web DB 오류
                        errorMsg = GlobalStatic.g_err;
                        break;
                    default:
                        errorMsg = String.format(activity.getString(R.string.weberr_etc_error), GlobalStatic.g_errNumber);
                }

                activity.showAlertDialog(null, errorMsg, RVDialog.STYLE_NOTICE, R.string.common_ok, 0);

            } else if (msg.what == 1) {
                activity.finish();
            }
        }
    }
}
