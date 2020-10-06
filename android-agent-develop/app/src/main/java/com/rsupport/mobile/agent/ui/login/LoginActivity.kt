package com.rsupport.mobile.agent.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.constant.PreferenceConstant
import com.rsupport.mobile.agent.databinding.LoginBinding
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoActivity
import com.rsupport.mobile.agent.ui.agent.install.AgentInstallActivity
import com.rsupport.mobile.agent.ui.base.RVCommonActivity
import com.rsupport.mobile.agent.ui.base.ViewState
import com.rsupport.mobile.agent.ui.dialog.RVDialog
import com.rsupport.mobile.agent.ui.settings.basic.BasicSettingActivity
import com.rsupport.mobile.agent.ui.tutorial.TutorialActivity
import com.rsupport.mobile.agent.ui.views.SoftKeyboardDectectorView
import com.rsupport.mobile.agent.utils.Collector
import com.rsupport.mobile.agent.utils.ErrorData
import com.rsupport.rscommon.exception.RSException
import kotlinx.android.synthetic.main.layout_title.*
import kotlinx.android.synthetic.main.login.*
import kotlinx.coroutines.launch

class LoginActivity : RVCommonActivity() {
    private val EVENT_ID_FAIL_LOGIN = 0
    private val EVENT_ID_SHOW_URL = 1
    private val EVENT_ID_EXPIRED = 2
    private val EVENT_ID_EXPIRED_IN_DAY = 10
    private val EVENT_ID_UPDATE = 3
    private val EVENT_ID_UPDATE_LATE = 4
    private val EVENT_ID_CHANGE_NEW_JOIN = 5
    private val EVENT_ID_SHOW_PROXY_HELP = 6
    private val EVENT_ID_INSTALL_RSPERM_CONFIRM = 7
    private val EVENT_ID_INSTALL_RSPERM_CANCLE = 8
    private val EVENT_ID_NOT_FOUND_RSPERM = 9

    private val loginViewModel: LoginViewModel by viewModels()

    private var enginePackageName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        initApp()
        initViewModel(savedInstanceState)
        setSoftkeyboardDetector()
    }

    private fun initUI() {
        setContentViewBinding<LoginBinding>(R.layout.login, R.layout.layout_common_bg_no_margin).apply {
            viewModel = loginViewModel
        }
        setTitle("", true, false)
        setLeftButtonBackground(R.drawable.button_headermenu)
        hideShadowLine()
        left_button.setOnClickListener { startPreSetting(this@LoginActivity) }
    }

    private fun initViewModel(savedInstanceState: Bundle?) {
        loginViewModel.viewState.observe(this, Observer { viewState: ViewState? ->
            (viewState as? MainViewState)?.let { onChangedViewState(it) }
        })
        savedInstanceState ?: loginViewModel.updateViewState()
    }

    private fun setSoftkeyboardDetector() {
        val softKeyboardDecector = SoftKeyboardDectectorView(this)
        addContentView(softKeyboardDecector, LinearLayout.LayoutParams(-1, -1))
        softKeyboardDecector.setOnShownKeyboard {
            lifecycleScope.launch { onChangedSoftkeyboardStatus(true) }
        }
        softKeyboardDecector.setOnHiddenKeyboard {
            lifecycleScope.launch { onChangedSoftkeyboardStatus(false) }
        }
    }

    private fun initApp() {
        GlobalStatic.g_deviceMacAddr = GlobalStatic.getMacAddress(this)
        GlobalStatic.g_deviceIP = GlobalStatic.getLocalIP()
        GlobalStatic.loadSettingURLInfo(this)
        GlobalStatic.loadResource(this)
        GlobalStatic.loadAppInfo(this@LoginActivity)
        GlobalStatic.clearAgentInstallPageInfo()
    }

    private fun onChangedViewState(viewState: MainViewState) = when (viewState) {
        MainViewState.ShowTutorialViewState -> startTutorial()
        MainViewState.StartAgentInfoViewState -> startAgentInfoActivity()
        MainViewState.ShowProgress -> showProgressHandler(getString(R.string.remotepc_loading_waiting_message))
        MainViewState.HideProgress -> hideProgressHandler()
        MainViewState.InvalidServerURL -> showInvalidURLDialog()
        MainViewState.EmptyUserId -> showEmptyIdDialog()
        MainViewState.EmptyUserPwd -> showEmptyPwdDialog()
        MainViewState.HideSoftKeyboard -> hideKeyboards()
        MainViewState.LoginSuccess -> startAgentInstall(this)
        is MainViewState.LoginFailure -> showLoginFailDialog(viewState.exception)
        MainViewState.NotSupportDevice -> showNotSupportEngineDialog()
        MainViewState.BlackListDevice -> showBlacklistDeviceDialog()
        MainViewState.NeedDeviceUpdate -> showDeviceUpdateDialog()
        MainViewState.BlackListQueryError -> showBlacklistQuryErrorDialog()
        MainViewState.AppForceUpdate -> showForceUpdateDialog()
        MainViewState.UpgradeMember -> showUpgradeMemberDialog()
        MainViewState.ProxyVerify -> showProxyVerifyDialog()
        MainViewState.UpdataAvailable -> showUpdateDialog()
        is MainViewState.PassExpireDay -> showPasswordExpireDayDialog(viewState.day, viewState.serverURL)
        is MainViewState.RspermInstallRequest -> showRspermDownDlg(viewState.packageName)
    }

    private fun showPasswordExpireDayDialog(day: String, serverURL: String) {
        hideProgressHandler()
        showAlertDialog(null, String.format(getString(R.string.weberr_password_limit_days), day, serverURL), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_EXPIRED_IN_DAY)
    }

    private fun showDeviceUpdateDialog() {
        val errorData = ErrorData(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_854, message = getString(R.string.knox_blacklist_osupdate))
        Collector.push(errorData)
        showAlertDialog(null, errorData.getDisplayedMessage(), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_NOT_FOUND_RSPERM, false)
    }

    private fun showBlacklistDeviceDialog() {
        val errorData = ErrorData(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_855, message = getString(R.string.knox_blacklist_not_support))
        Collector.push(errorData)
        showAlertDialog(null, errorData.getDisplayedMessage(), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_NOT_FOUND_RSPERM, false)
    }

    private fun showBlacklistQuryErrorDialog() {
        val errorData = ErrorData(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_111, message = getString(R.string.msg_unableserver))
        Collector.push(errorData)
        showAlertDialog(null, errorData.getDisplayedMessage(), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_NOT_FOUND_RSPERM, false)
    }

    private fun showNotSupportEngineDialog() {
        val errorData = ErrorData(ErrorCode.ENGINE_NOT_SUPPORTED, message = getString(R.string.agent_popup_rsperm_notsupport))
        Collector.push(errorData)
        showAlertDialog(null, errorData.getDisplayedMessage(), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_NOT_FOUND_RSPERM, false)
    }

    private fun showLoginFailDialog(throwable: Throwable) {
        when (throwable) {
            is RSException -> {
                val loginExceptionInfo = getLoginExceptionMessage(throwable.errorCode)
                val errorData = ErrorData(codeInt = throwable.errorCode, message = loginExceptionInfo.message
                        ?: "")
                Collector.push(errorData)

                showAlertDialog(null, errorData.getDisplayedMessage(), RVDialog.STYLE_NOTICE, R.string.common_ok, loginExceptionInfo.confirmEventId, loginExceptionInfo.cancelable)
            }
        }
    }

    private fun hideKeyboards() {
        hideKeyboard(userid)
        hideKeyboard(userpasswd)
    }

    private fun showEmptyPwdDialog() {
        val errorData = ErrorData(ErrorCode.LOGIN_EMPTY_PWD, getString(R.string.msg_inputloginpwd))
        Collector.push(errorData)
        showAlertDialog(null, errorData.getDisplayedMessage(), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_FAIL_LOGIN)
    }

    private fun showEmptyIdDialog() {
        val errorData = ErrorData(ErrorCode.LOGIN_EMPTY_ID, getString(R.string.msg_inputloginid))
        Collector.push(errorData)
        showAlertDialog(null, errorData.getDisplayedMessage(), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_FAIL_LOGIN)
    }

    private fun showInvalidURLDialog() {
        val errorData = ErrorData(ErrorCode.LOGIN_INVALID_URL, getString(R.string.msg_unableserver).toString() + "\n" + getString(R.string.msg_checkagain))
        Collector.push(errorData)
        showAlertDialog(null, errorData.getDisplayedMessage(), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_SHOW_URL)
    }

    override fun onResume() {
        super.onResume()
        loadSettingInfo()
        hideProgressHandler()
    }

    private fun loadSettingInfo() {
        var pref = getSharedPreferences(PreferenceConstant.RV_PREF_SETTING_INIT, Activity.MODE_PRIVATE)
        pref = getSharedPreferences(PreferenceConstant.RV_PREF_GUIDE_VIEW, Activity.MODE_PRIVATE)
        GlobalStatic.ISTOUCHVIEWGUIDE = pref.getBoolean("istouchviewguide", true)
        GlobalStatic.ISCURSORVIEWGUIDE = pref.getBoolean("iscursorviewguide", true)
        GlobalStatic.g_setinfoLanguage = GlobalStatic.getSystemLanguage(this)
        GlobalStatic.loadSettingURLInfo(this)
    }

    private fun getLoginExceptionMessage(errorCode: Int): LoginExceptionInfo {
        var message = GlobalStatic.errMessageProc(context).let {
            if (TextUtils.isEmpty(it)) getString(R.string.msg_unableserver)
            else it
        }

        var confirmEventId = 0
        var cancelable = true
        when (errorCode) {
            ErrorCode.ENGINE_NOT_SUPPORTED -> {
                message = getString(R.string.agent_popup_rsperm_notsupport)
                confirmEventId = EVENT_ID_NOT_FOUND_RSPERM
                cancelable = false
            }
            ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_854 -> {
                message = getString(R.string.knox_blacklist_osupdate)
                confirmEventId = EVENT_ID_NOT_FOUND_RSPERM
                cancelable = false
            }
            ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_855 -> {
                message = getString(R.string.knox_blacklist_not_support)
                confirmEventId = EVENT_ID_NOT_FOUND_RSPERM
                cancelable = false
            }
            ErrorCode.LOGIN_EXPIRED_PWD -> {
                message = getString(R.string.weberr_password_expired)
                confirmEventId = EVENT_ID_EXPIRED
            }
            ErrorCode.LOGIN_INVALID_ID_AES, ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD -> {
                message = getString(R.string.weberr_invalid_user_account)
                confirmEventId = EVENT_ID_FAIL_LOGIN
            }
        }
        return LoginExceptionInfo(message, confirmEventId, cancelable)
    }

    private fun showProxyVerifyDialog() {
        val errorData = ErrorData(ErrorCode.LOGIN_NET_ERR_PROXY_VERIFY, context.getString(R.string.proxyverifyerr_msg))
        Collector.push(errorData)
        showAlertDialog(null, errorData.getDisplayedMessage(), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_SHOW_PROXY_HELP, R.string.rv_cancel, EVENT_ID_FAIL_LOGIN)
    }

    private fun showUpgradeMemberDialog() {
        showAlertDialog(null, getString(R.string.weberr_need_update_member), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_CHANGE_NEW_JOIN)
    }

    private fun startAgentInstall(context: Context) {
        hideProgressHandler()
        val intent = Intent(context, AgentInstallActivity::class.java)
        context.startActivity(intent)
    }

    private fun startAgentInfoActivity() {
        startActivity(AgentInfoActivity.forFirstIntent(this@LoginActivity))
    }

    private fun startPreSetting(context: Context) {
        val intent = Intent(context, BasicSettingActivity::class.java)
        context.startActivity(intent)
    }

    private fun showRspermDownDlg(packageName: String) {
        enginePackageName = packageName
        val msgContent = getString(R.string.agent_popup_rsperm_install_guide)
        showAlertDialog(null, msgContent, RVDialog.STYLE_NOTICE, R.string.computer_active_ok, EVENT_ID_INSTALL_RSPERM_CONFIRM, R.string.computer_active_cancel, EVENT_ID_INSTALL_RSPERM_CANCLE, false)
    }

    private fun showUpdateDialog() {
        val msgTitle = getString(R.string.update_title)
        val msgContent = getString(R.string.weberr_update_recommendation)
        showAlertDialog(msgTitle, msgContent, RVDialog.STYLE_NOTICE, R.string.update_btn_update, EVENT_ID_UPDATE, R.string.update_btn_later, EVENT_ID_UPDATE_LATE)
    }

    private fun showForceUpdateDialog() {
        val msgTitle = getString(R.string.update_title)
        val msgContent = getString(R.string.weberr_update_force)
        showAlertDialog(msgTitle, msgContent, RVDialog.STYLE_NOTICE, R.string.update_btn_update, EVENT_ID_UPDATE)
    }

    private fun startUpdate(packageName: String) {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        startActivity(market)
    }

    private fun onChangedSoftkeyboardStatus(isShown: Boolean) {
        val headLogoParam = headlogo.layoutParams as RelativeLayout.LayoutParams
        val inputidParam = inputlayout.layoutParams as RelativeLayout.LayoutParams
        val saveidParam = login_btn_layout.layoutParams as RelativeLayout.LayoutParams
        if (isShown) {
            title_layout.visibility = View.GONE
            headLogoParam.bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_2)
            inputidParam.topMargin = resources.getDimensionPixelSize(R.dimen.margin_4)
            saveidParam.topMargin = resources.getDimensionPixelSize(R.dimen.margin_10)
        } else {
            title_layout.visibility = View.VISIBLE
            headLogoParam.bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_12)
            inputidParam.topMargin = resources.getDimensionPixelSize(R.dimen.margin_16)
            saveidParam.topMargin = resources.getDimensionPixelSize(R.dimen.margin_52)
        }
        login_btn_layout.layoutParams = saveidParam
        headlogo.layoutParams = headLogoParam
        inputlayout.layoutParams = inputidParam
    }

    override fun eventDelivery(event: Int) {
        if (dialog != null) {
            dialog.dismiss()
        }
        when (event) {
            EVENT_ID_SHOW_URL -> startPreSetting(this@LoginActivity)
            EVENT_ID_EXPIRED_IN_DAY -> startAgentInstall(this@LoginActivity)
            EVENT_ID_UPDATE -> {
                startUpdate(this@LoginActivity.packageName)
                finish()
            }
            EVENT_ID_UPDATE_LATE -> startAgentInstall(this@LoginActivity)
            EVENT_ID_SHOW_PROXY_HELP -> startPreSetting(this@LoginActivity)
            EVENT_ID_INSTALL_RSPERM_CONFIRM -> startUpdate(enginePackageName)
            EVENT_ID_NOT_FOUND_RSPERM -> finish()
            EVENT_ID_INSTALL_RSPERM_CANCLE -> {
            }
            EVENT_ID_CHANGE_NEW_JOIN -> {
            }
            EVENT_ID_FAIL_LOGIN -> {
            }
            EVENT_ID_EXPIRED -> {
            }
            else -> {
            }
        }
    }

    private fun startTutorial() {
        val intent = Intent(this, TutorialActivity::class.java)
        startActivity(intent)
    }

    data class LoginExceptionInfo(var message: String?, var confirmEventId: Int, var cancelable: Boolean)
}