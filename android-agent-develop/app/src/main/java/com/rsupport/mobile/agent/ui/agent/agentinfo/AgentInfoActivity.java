/********************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 ********************************************************************************
 *
 * Copyright (c) 2013 RSUPPORT Co., Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.
 *
 * FileName: AgentDetailActivity.java
 * Author  : "kyeom@rsupport.com"
 * Date    : 2013. 1. 9.
 * Purpose : Remote control menu selection
 *
 * [History]
 *
 * 2013. 1. 9. -Initialize
 *
 */
package com.rsupport.mobile.agent.ui.agent.agentinfo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.ComConstant;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.databinding.AgentinfoNewBinding;
import com.rsupport.mobile.agent.error.ErrorCode;
import com.rsupport.mobile.agent.service.command.AgentCommand3033;
import com.rsupport.mobile.agent.ui.adapter.InfoItemAdapter;
import com.rsupport.mobile.agent.ui.adapter.InfoListItemAdapter;
import com.rsupport.mobile.agent.ui.agent.EngineActivationViewModel;
import com.rsupport.mobile.agent.ui.base.InfoListItem;
import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.ui.base.ViewState;
import com.rsupport.mobile.agent.ui.dialog.RVDialog;
import com.rsupport.mobile.agent.ui.login.LoginActivity;
import com.rsupport.mobile.agent.ui.permission.AllowPermissionActivity;
import com.rsupport.mobile.agent.ui.settings.SettingPageActivity;
import com.rsupport.mobile.agent.utils.SdkVersion;
import com.rsupport.util.log.RLog;

import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.List;

import kotlin.Lazy;

public class AgentInfoActivity extends RVCommonActivity {

    private static final int PERMISSIONCODE = 10001;

    private final static int EVENT_ID_NO = 0;
    private final static int EVENT_ID_UPDATE = 1;
    private final static int EVENT_ID_SETTING_PAGE = 2; // 설정페이지 이동
    private final static int EVENT_ID_AGENT_DELETE = 3; // 에이전트 삭제
    private final static int EVENT_ID_UNINSTALL = 4; // 에이전트 삭제 확보

    private InfoItemAdapter agentAdapter1 = null;
    private ListView agentList1 = null;
    private ArrayList<InfoListItem> agentArrayList1 = null;

    private Handler uiHandler = null;
    private AgentInfoViewModel agentInfoViewModel = null;
    private Lazy<SdkVersion> sdkVersionLazy = KoinJavaComponent.inject(SdkVersion.class);
    private Lazy<EngineActivationViewModel> engineActivationViewModel = KoinJavaComponent.inject(EngineActivationViewModel.class);

    /**
     * 디바이스 등록되고 최초 실행
     */
    private static String EXTRA_KEY_FIRST = "";

    public static Intent forIntent(Context context) {
        Intent intent = new Intent(context, AgentInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent forFirstIntent(Context context) {
        Intent intent = forIntent(context);
        intent.putExtra(EXTRA_KEY_FIRST, true);
        return intent;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHandler = new Handler();
        agentInfoViewModel = createViewModel();
        AgentinfoNewBinding agentinfoNewBinding = DataBindingUtil.setContentView(this, R.layout.agentinfo_new);
        agentinfoNewBinding.setViewModel(agentInfoViewModel);
        agentinfoNewBinding.setLifecycleOwner(this);

        initValues();

        setTitle(getResources().getString(R.string.agent_control_title), true, true);
        setLeftButtonBackground(R.drawable.button_headerback);
        findViewById(R.id.left_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.right_button).setVisibility(View.INVISIBLE);

        agentList1 = findViewById(R.id.agentinfo_list1);
        if (savedInstanceState == null) {
            agentInfoViewModel.updateLoginIfLoginStatus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkpermission();

        if (getIntent().hasExtra(AgentCommand3033.CALL_UPDATE_DIALOG)) {
            showUpdateDialogHandler(true, GlobalStatic.g_loginInfo.userid, GlobalStatic.g_loginInfo.userpasswd, GlobalStatic.connectionInfo.ctype);
        }

        if (GlobalStatic.isFirstAgentStart) {
            GlobalStatic.isFirstAgentStart = false;
            AgentBasicInfo.setIsAgentStart(this, GlobalStatic.isFirstAgentStart);
            if (GlobalStatic.IS_HCI_BUILD) {
                finish();
            }
        }
    }


    private AgentInfoViewModel createViewModel() {
        AgentInfoViewModel agentInfoViewModel = new ViewModelProvider(this).get(AgentInfoViewModel.class);
        agentInfoViewModel.getBatteryOptimized().observe(this, packageName -> {
            if (!TextUtils.isEmpty(packageName)) {
                Intent intent = new Intent(this, AllowPermissionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        agentInfoViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                showProgressHandler(getString(R.string.remotepc_loading_waiting_message));
            } else {
                hideProgressHandler();
            }
        });

        agentInfoViewModel.getViewState().observe(this, viewState -> {
            if (viewState != null) onChangedViewState(viewState);
        });

        // Agent 등록후 최초 자동 로그인
        agentInfoViewModel.isLoggedIn().observe(this, loggedIn -> {
            if (loggedIn) {
                getIntent().removeExtra(EXTRA_KEY_FIRST);
                return;
            }

            if (getIntent().hasExtra(EXTRA_KEY_FIRST)) {
                getIntent().removeExtra(EXTRA_KEY_FIRST);
                agentInfoViewModel.toggleLogin();
            }
        });

        agentInfoViewModel.getAgentInfo().observe(this, agentInfo -> {
            if (agentInfo != null) {
                makeFirstListView(agentInfo.name, agentInfo.localip);
            }
        });

        getLifecycle().addObserver(agentInfoViewModel);
        return agentInfoViewModel;
    }

    /**
     * agentViewModel 에서 viewModel 의 상태가 변경될때 호출 된다.
     *
     * @param viewState
     */
    private void onChangedViewState(ViewState viewState) {
        if (viewState instanceof AgentInfoViewState.EmptyGuid) {
            showAlertDialog(null, getString(R.string.agent_popup_agent_remove), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_UNINSTALL);
        } else if (viewState instanceof AgentInfoViewState.OffLineState) {
            showAlertDialog(null, getString(R.string.cmderr_agentconnect_unable), RVDialog.STYLE_NOTICE, R.string.common_ok, 0);
        } else if (viewState instanceof AgentInfoViewState.LoginFail) {
            int errorCode = ((AgentInfoViewState.LoginFail) viewState).getErrorCode();
            failEvent(errorCode);
        } else if (viewState instanceof AgentInfoViewState.LogoutFail) {
            int errorCode = ((AgentInfoViewState.LogoutFail) viewState).getErrorCode();
            failEvent(errorCode);
        } else if (viewState instanceof AgentInfoViewState.FirstLaunched) {
            showAlertDialog(null, getString(R.string.agentinfo_first_message), RVDialog.STYLE_NOTICE, R.string.re_common_ok, EVENT_ID_NO, false);
        } else if (viewState instanceof AgentInfoViewState.NeedEngineActivate) {
            engineActivationViewModel.getValue().activate(this);
            engineActivationViewModel.getValue().isLoading().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (aBoolean.equals(true)) {
                        showProgressHandler(getString(R.string.remotepc_loading_waiting_message));
                    } else {
                        hideProgressHandler();
                    }
                }
            });
        } else {
            RLog.w("not defined viewState." + viewState.getClass().getName());
        }
    }

    private void makeFirstListView(String agentName, String agentLocalIp) {
        agentArrayList1 = new ArrayList<InfoListItem>();
        agentArrayList1.add(new InfoListItem(EVENT_ID_NO, getString(R.string.mobile_name), agentName));
        agentArrayList1.add(new InfoListItem(EVENT_ID_NO, getString(R.string.cinfo_address), agentLocalIp));
        agentArrayList1.add(new InfoListItem(EVENT_ID_SETTING_PAGE, null, getString(R.string.setting), InfoListItem.RIGHT_BUTTON));
        agentAdapter1 = new InfoItemAdapter(this, agentArrayList1, false);
        agentList1.setAdapter(agentAdapter1);
        InfoListItemAdapter.getListViewSize(agentList1);
    }

    private void showUninstallGuideDialog() {
        String message = getResources().getString(R.string.computer_uninstall_msg);
        showAlertDialog(null, message, RVDialog.STYLE_NOTICE, R.string.computer_active_ok, EVENT_ID_UNINSTALL, R.string.computer_active_cancel, EVENT_ID_NO);
    }

    private void startLoginActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    public void eventDelivery(int event) {
        if (dialog != null) {
            dialog.dismiss();
        }
        switch (event) {
            case EVENT_ID_AGENT_DELETE:
                showUninstallGuideDialog();
                break;
            case EVENT_ID_UNINSTALL:
                startLoginActivity(this);
                break;
            case EVENT_ID_UPDATE:
                startUpdate(getPackageName());
                finish();
                break;
            case EVENT_ID_SETTING_PAGE:
                startSettingPage();
                break;
        }
    }

    public void startSettingPage() {
        startActivity(new Intent(this, SettingPageActivity.class));
    }

    private void failEvent(int errorCode) {
        switch (errorCode) {
            case AgentInfoInteractor.UPDATE_FORCED:
                showUpdateDialogHandler(true, GlobalStatic.g_loginInfo.userid, GlobalStatic.g_loginInfo.userpasswd, GlobalStatic.connectionInfo.ctype);
                break;
            case AgentInfoInteractor.UPDATED:
                showUpdateDialogHandler(false, GlobalStatic.g_loginInfo.userid, GlobalStatic.g_loginInfo.userpasswd, GlobalStatic.connectionInfo.ctype);
                break;
            case AgentInfoInteractor.REMOVED_AGENT:
                showAlertDialog(null, getString(R.string.agent_popup_agent_remove), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_UNINSTALL);
                break;
            // TODO 만료된 Agent 처리확인 필요
            case AgentInfoInteractor.EXPIRED_AGENT:
            default:
                String message = GlobalStatic.errMessageProc(this);
                if (errorCode != -1 && errorCode != ErrorCode.UNKNOWN_ERROR && !message.contains("(" + errorCode + ")")) {
                    message += " (" + errorCode + ")";
                }
                if (errorCode == ComConstant.NET_ERR_PROXYINFO_NULL) {
                    showAlertDialog(null, message, RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_SETTING_PAGE, R.string.rv_cancel, EVENT_ID_NO);
                } else {
                    showAlertDialog(null, message, RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_NO);
                }
                break;
        }

    }

    public void startUpdate(String packageName) {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        startActivity(market);
    }

    protected void showUpdateDialogHandler(boolean isForce, String id, String passwd, String type) {
        uiHandler.post(updateDlgRunnable.setInfo(isForce, id, passwd, type));
    }

    private UpdateDlgRunnable updateDlgRunnable = new UpdateDlgRunnable();

    private class UpdateDlgRunnable implements Runnable {

        public boolean isForce;
        String id;
        String passwd;
        String type;

        UpdateDlgRunnable setInfo(boolean isForce, String id, String passwd, String type) {
            this.isForce = isForce;
            this.id = id;
            this.passwd = passwd;
            this.type = type;
            return this;
        }

        public void run() {
            hideProgressHandler();
            showUpdateDlg(AgentInfoActivity.this, isForce, id, passwd, type);
        }
    }

    private void showUpdateDlg(Context context, boolean isForce, final String id, final String passwd, final String type) {

        String msgTitle = getString(R.string.update_title);

        if (!isForce) {
            String msgContent = getString(R.string.weberr_update_force);
            showAlertDialog(msgTitle, msgContent, RVDialog.STYLE_NOTICE, R.string.update_btn_update, EVENT_ID_UPDATE, R.string.update_btn_later,
                    EVENT_ID_NO);
        } else {
            String msgContent = getString(R.string.weberr_update_recommendation);
            showAlertDialog(msgTitle, msgContent, RVDialog.STYLE_NOTICE, R.string.update_btn_update,
                    EVENT_ID_UPDATE, false);
        }
    }

    private void initValues() {
        GlobalStatic.isFirstAgentStart = AgentBasicInfo.getIsAgentStart(this);
        GlobalStatic.loadAppInfo(getApplicationContext());
        GlobalStatic.loadResource(getApplicationContext());
    }

    private void checkpermission() {
        if (!sdkVersionLazy.getValue().greaterThan23()) return;

        if (!checkAndRequestPermission(PERMISSIONCODE, new String[]{Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE})
                || (!Settings.canDrawOverlays(this))
                || (!Settings.System.canWrite(this))
        ) {

            Intent intent = new Intent(this, AllowPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    public boolean checkAndRequestPermission(int permissionRequestCode, String... permissions) {
        String[] requiredPermissions = getRequiredPermissions(this, permissions);
        if (requiredPermissions.length > 0) {
            return false;
        } else {
            return true;
        }
    }


    public String[] getRequiredPermissions(Context context, String... permissions) {
        List<String> requiredPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(permission);
            }
        }

        return requiredPermissions.toArray(new String[requiredPermissions.size()]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
