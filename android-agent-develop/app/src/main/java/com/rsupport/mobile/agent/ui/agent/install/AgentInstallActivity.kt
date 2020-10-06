package com.rsupport.mobile.agent.ui.agent.install

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.rsupport.knox.KnoxParam
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.constant.AutoInstallInfo
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.databinding.AgentInstallActivityBinding
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.ui.adapter.AboutItemAdapter
import com.rsupport.mobile.agent.ui.agent.EngineActivationViewModel
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoActivity
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import com.rsupport.mobile.agent.ui.base.InfoListItem
import com.rsupport.mobile.agent.ui.base.RVCommonActivity
import com.rsupport.mobile.agent.ui.dialog.RVDialog
import com.rsupport.mobile.agent.ui.permission.AllowPermissionActivity
import com.rsupport.mobile.agent.ui.settings.GroupSelectActivity
import com.rsupport.mobile.agent.utils.GUID
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.mobile.agent.utils.Utility
import com.rsupport.rscommon.exception.RSException
import com.rsupport.util.log.RLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.java.KoinJavaComponent
import org.koin.java.KoinJavaComponent.inject
import java.util.*

class AgentInstallActivity : RVCommonActivity() {
    private val SUB_ID_AGENT_INSTALL_ID = 65
    private val SUB_ID_AGENT_INSTALL_PASS = 66
    private val SUB_ID_AGENT_INSTALL_PASS_RE = 67
    private val SUB_ID_AGENT_INSTALL_NAME = 68
    private val SUB_ID_AGENT_INSTALL_MOBILE_NAME = 69
    private val SUB_ID_AGENT_INSTALL_GROUP_NAME = 70

    private var guid: String? = null

    private var installAdapter1: AboutItemAdapter? = null
    private var installList1: ListView? = null
    private var isntallArrayList1 = ArrayList<InfoListItem>()
    private lateinit var uiHandler: Handler
    private val sdkVersion by inject(SdkVersion::class.java)
    private val configRepository by inject(ConfigRepository::class.java)
    private val apiService by inject(ApiService::class.java)

    private val engineActivationViewModel by viewModel<EngineActivationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiHandler = Handler(installHandle)

        setContentViewBinding<AgentInstallActivityBinding>(R.layout.agent_install_activity, R.layout.layout_common_bg_margin).apply {
            engineActivateViewModel = this@AgentInstallActivity.engineActivationViewModel
            lifecycleOwner = this@AgentInstallActivity
        }

        lifecycle.addObserver(engineActivationViewModel)

        setTitle(R.string.agent_install, true, false)
        setLeftButtonBackground(R.drawable.button_headerback)
        setBottomTitle(R.string.agent_install, null)
        val btnTitleLeft = findViewById<View>(R.id.left_button) as ImageButton
        btnTitleLeft.setOnClickListener { finish() }
        installList1 = findViewById<View>(R.id.about_list1) as ListView
        if (savedInstanceState == null) {
            checkGroupId()
            engineActivationViewModel.activate(this)
            engineActivationViewModel.isLoading.observe(this, Observer {
                if (it == true) showProgressHandler(getString(R.string.remotepc_loading_waiting_message))
                else hideProgressHandler()
            })
        }
        makeInstallItemLists()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        setListItem()
        if (savedInstanceState.getBoolean("isInstallThread")) {
            showProgressHandler(getString(R.string.remotepc_loading_waiting_message))
        }
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("isInstallThread", isProgressShowing)
        super.onSaveInstanceState(outState)
    }

    /** 하단 타이틀바 (클릭버튼용) 기능  */
    fun Click_ButtomTitle(v: View?) {
        if (GlobalStatic.g_agentInstallNAME.trim { it <= ' ' } == "") {
            showAlertDialog(null, getString(R.string.devicename_notice), RVDialog.STYLE_NOTICE, R.string.common_ok, 0)
            return
        }
        if (GlobalStatic.g_agentInstallNAME.length > 50) {
            showAlertDialog(null, getString(R.string.devicename_langth_notice), RVDialog.STYLE_NOTICE, R.string.common_ok, 0)
            return
        }
        if (!Utility.invalidWordCheck(GlobalStatic.g_agentInstallNAME)) {
            showAlertDialog(null, getString(R.string.re_following_characters), RVDialog.STYLE_NOTICE, R.string.common_ok, 0)
            return
        }
        if (GlobalStatic.g_agentInstallPasswdRe == GlobalStatic.g_agentInstallPasswd) {
            installAgentPageCall(GlobalStatic.g_agentInstallID, GlobalStatic.g_agentInstallPasswd, GlobalStatic.g_agentInstallNAME)
        } else {
            showAlertDialog(null, getString(R.string.agent_install_no_matching_pass), RVDialog.STYLE_NOTICE, R.string.common_ok, 0)
        }
    }

    private fun setAutoInstallInfo() {
        GlobalStatic.g_agentInstallNAME = GlobalStatic.getMacAddress(this).replace(":", "")
        GlobalStatic.g_agentInstallID = AutoInstallInfo.AGENT_ID
        GlobalStatic.g_agentInstallPasswd = AutoInstallInfo.AGENT_PASSWD
        GlobalStatic.g_agentInstallPasswdRe = AutoInstallInfo.AGENT_PASSWD
    }


    override fun onResume() {
        RLog.i("onResume")
        super.onResume()
        if (GlobalStatic.IS_HCI_BUILD) {
            setAutoInstallInfo()
            Click_ButtomTitle(null)
        }
    }

    override fun onDestroy() {
        RLog.i("onDestroy")
        super.onDestroy()
    }

    private fun installAgentPageCall(id: String, pass: String, name: String) {
        showProgressHandler(getString(R.string.remotepc_loading_waiting_message))
        RLog.i("installAgentPageCall")
        Thread(Runnable {
            try {
                var ret = KoinJavaComponent.get(ApiService::class.java).checkAccessIDValidate(id, pass, AgentBasicInfo.getAgentBizID(this@AgentInstallActivity))
                RLog.d("call checkAccessIDValidate : $ret")
                if (ret.isSuccess) {
                    guid = GUID().toString()
                    //					checkGroupId();
                    val installResult = apiService.agentInstall(guid!!, id, pass, name, AgentBasicInfo.getAgentBizID(this@AgentInstallActivity))
                    RLog.d("call agentInstall : $installResult")
                    if (installResult is Result.Failure) {
                        throw installResult.throwable
                    }
                }
                uiHandler.sendEmptyMessage(if (ret.isSuccess) 1 else 0)
            } catch (e: RSException) {
                RLog.w(e)
                uiHandler.sendEmptyMessage(0)
            }
        }).start()
    }

    private fun checkGroupId() {
        for (groupInfo in GlobalStatic.g_vecGroups) {
            if (groupInfo.pgrpid == "0") {
                GlobalStatic.g_agentInstallGroupID = groupInfo.grpid
                GlobalStatic.g_agentInstallGroupNAME = groupInfo.grpname
            }
        }
    }

    fun makeInstallItemLists() {
        setListItem()
        installAdapter1 = AboutItemAdapter(this, isntallArrayList1)
        installList1!!.adapter = installAdapter1
        AboutItemAdapter.getListViewSize(installList1)
    }

    private fun setListItem() {
        isntallArrayList1.clear()
        val installName = InfoListItem(0, getString(R.string.agent_display_name), GlobalStatic.g_agentInstallNAME, InfoListItem.EDIT_BOX)
        installName.childID = SUB_ID_AGENT_INSTALL_NAME
        installName.isDivider = false
        isntallArrayList1.add(installName)


        val installGroupName = InfoListItem(SUB_ID_AGENT_INSTALL_GROUP_NAME, getString(R.string.agent_install_group_name),
                Html.fromHtml(GlobalStatic.g_agentInstallGroupNAME).toString(), InfoListItem.RIGHT_BUTTON)
        installGroupName.childID = SUB_ID_AGENT_INSTALL_GROUP_NAME
        installGroupName.isDivider = false
        isntallArrayList1.add(installGroupName)


        val installID = InfoListItem(0, getString(R.string.agent_install_id), GlobalStatic.g_agentInstallID, InfoListItem.EDIT_BOX)
        installID.childID = SUB_ID_AGENT_INSTALL_ID
        installID.isDivider = false
        isntallArrayList1.add(installID)


        val installPass = InfoListItem(0, getString(R.string.agent_install_pass), GlobalStatic.g_agentInstallPasswd, InfoListItem.EDIT_PASS_BOX)
        installPass.childID = SUB_ID_AGENT_INSTALL_PASS
        installPass.isDivider = false
        isntallArrayList1.add(installPass)


        val installPassRe = InfoListItem(0, getString(R.string.agent_install_pass_re), GlobalStatic.g_agentInstallPasswd, InfoListItem.EDIT_PASS_BOX)
        installPassRe.childID = SUB_ID_AGENT_INSTALL_PASS_RE
        installPassRe.isDivider = false
        isntallArrayList1.add(installPassRe)


        if (installAdapter1 != null) installAdapter1!!.notifyDataSetChanged()
    }


    private val installHandle: Handler.Callback = Handler.Callback { msg ->
        if (msg.what == 0) {
            hideProgressHandler()
            val errorMsg = when (GlobalStatic.g_errNumber) {
                111 -> getString(R.string.msg_inputlogininfo)
                400 -> getString(R.string.agent_install_id).toString() + " :\n" + getString(R.string.agent_install_rule_id)
                401 -> getString(R.string.agent_install_pass).toString() + " :\n" + getString(R.string.account_change_new_passwd_wrong)
                402 -> getString(R.string.agent_install_rule_equal)
                403 -> getString(R.string.license_count_max)
                809 -> getString(R.string.group_agent_count_max)
                901 -> getString(R.string.agent_install_pass).toString() + " :\n" + getString(R.string.account_change_new_passwd_wrong)
                911 -> GlobalStatic.g_err
                else -> String.format(getString(R.string.weberr_etc_error), GlobalStatic.g_errNumber)
            }
            showAlertDialog(null, errorMsg, RVDialog.STYLE_NOTICE, R.string.common_ok, 0)
        } else if (msg.what == 1) {

            lifecycleScope.launch {
                AgentBasicInfo.setAgentGuid(this@AgentInstallActivity, guid)
                hideProgressHandler()
                GlobalStatic.isFirstAgentStart = true
                AgentBasicInfo.setAgentName(this@AgentInstallActivity, GlobalStatic.g_agentInstallNAME)

                AgentBasicInfo.setIsAgentStart(this@AgentInstallActivity, GlobalStatic.isFirstAgentStart)
                configRepository.setStartApp(true)

                val intent = if (sdkVersion.greaterThan23()) {
                    Intent(this@AgentInstallActivity, AllowPermissionActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    }
                } else {
                    AgentInfoActivity.forFirstIntent(this@AgentInstallActivity)
                }
                withContext(Dispatchers.IO) {
                    val interactor by inject(AgentInfoInteractor::class.java)
                    interactor.updateAgentInfo()
                    interactor.release()
                }
                startActivity(intent)
                startLeftSlideAnimation()
            }
        }
        false
    }

    private fun startGroupSelectActivity(groupId: String) {
        val intent = Intent(this, GroupSelectActivity::class.java)
        intent.putExtra("groupId", groupId)
        startActivityForResult(intent, SUB_ID_AGENT_INSTALL_GROUP_NAME)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        RLog.d("onActivityResult : requestCode - $requestCode")
        RLog.d("onActivityResult : resultCode - $resultCode")
        when (requestCode) {
            KnoxParam.DEVICE_ADMIN_ADD_RESULT_ENABLE -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    finish()
                } else if (resultCode == Activity.RESULT_OK) {
                    hideProgressHandler()
                }
            }
            SUB_ID_AGENT_INSTALL_GROUP_NAME -> if (resultCode == Activity.RESULT_OK) {
                RLog.d("onActivityResult : g_agentInstallGroupID - " + GlobalStatic.g_agentInstallGroupID)
                RLog.d("onActivityResult : g_agentInstallGroupNAME - " + Html.fromHtml(GlobalStatic.g_agentInstallGroupNAME).toString())
                setListItem()
            }
        }
    }

    override fun eventDelivery(event: Int) {
        if (dialog != null) {
            dialog.dismiss()
        }
        if (event == SUB_ID_AGENT_INSTALL_GROUP_NAME) {
            startGroupSelectActivity(GlobalStatic.g_agentInstallGroupID)
        }
    }
}