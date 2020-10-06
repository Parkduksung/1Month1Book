package com.rsupport.mobile.agent.ui.settings.delete

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.databinding.ActivityAgentDeleteBinding
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.ui.base.RVCommonActivity
import com.rsupport.mobile.agent.ui.dialog.RVDialog
import com.rsupport.mobile.agent.ui.login.LoginActivity
import com.rsupport.mobile.agent.ui.settings.AgentSettingActivity
import com.rsupport.mobile.agent.utils.Collector
import com.rsupport.mobile.agent.utils.ErrorData
import kotlinx.android.synthetic.main.layout_title.*

class AgentDeleteActivity : RVCommonActivity() {
    private val EVENT_ID_CANCEL = 0
    private val EVENT_ID_SHOW_PROXY_HELP = 1

    private val agentDeleteVieModel: AgentDeleteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityAgentDeleteBinding>(this, R.layout.activity_agent_delete).apply {
            viewModel = agentDeleteVieModel
        }

        agentDeleteVieModel.viewState.observe(this, Observer {
            (it as? AgentDeleteViewState)?.let { agentViewState ->
                onViewStateChanged(agentViewState)
            }
        })

        setTitle(resources.getString(R.string.agentlist_menu_agentremove), true, true)
        val btnTitleRight = findViewById<View>(R.id.right_button) as ImageButton
        btnTitleRight.visibility = View.INVISIBLE

        setLeftButtonBackground(R.drawable.button_headerback)
        left_button.setOnClickListener {
            finish()
        }
    }


    private fun showProxyVerifyDialog() {
        val errorData = ErrorData(ErrorCode.SETTING_NET_ERR_PROXY_VERIFY, context.getString(R.string.proxyverifyerr_msg))
        Collector.push(errorData)
        showAlertDialog(null, errorData.getDisplayedMessage(), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_SHOW_PROXY_HELP, R.string.rv_cancel, EVENT_ID_CANCEL)
    }

    private fun onViewStateChanged(viewState: AgentDeleteViewState) = when (viewState) {
        AgentDeleteViewState.InvalidProxy -> showProxyVerifyDialog()
        AgentDeleteViewState.ShowProgress -> showProgressHandler(getString(R.string.remotepc_loading_waiting_message))
        AgentDeleteViewState.HideProgress -> hideProgressHandler()
        AgentDeleteViewState.InvalidAccount -> {
            ErrorData(ErrorCode.SETTING_INVALID_ACCOUNT_OR_PWD, getString(R.string.weberr_invalid_user_account)).run {
                Collector.push(this)
                showErrorDialog(getDisplayedMessage())
            }
        }
        AgentDeleteViewState.EmptyIdAndPwd -> showErrorDialog(R.string.msg_inputlogininfo)
        AgentDeleteViewState.EmptyId -> showErrorDialog(R.string.msg_inputloginid)
        AgentDeleteViewState.EmptyPwd -> showErrorDialog(R.string.msg_inputloginpwd)
        AgentDeleteViewState.DeletedSuccess -> startBackLoginActivity()
        AgentDeleteViewState.AlreadyDeletedAgent -> {
            ErrorData(ErrorCode.SETTING_ALREADY_DELETE_AGENT, getString(R.string.agent_popup_agent_remove)).run {
                Collector.push(this)
                showErrorDialog(getDisplayedMessage())
            }
        }
        is AgentDeleteViewState.NotDefinedError -> {
            val errorData = ErrorData(viewState.errorCode, getString(R.string.msg_unableserver))
            Collector.push(errorData)
            showErrorDialog(errorData.getDisplayedMessage())
        }
    }

    private fun showErrorDialog(resourceId: Int) {
        showAlertDialog(null, getString(resourceId), RVDialog.STYLE_NOTICE, R.string.common_ok, 0)
    }

    private fun showErrorDialog(message: String) {
        showAlertDialog(null, message, RVDialog.STYLE_NOTICE, R.string.common_ok, 0)
    }

    private fun startBackLoginActivity() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        startLeftSlideAnimation()
    }

    override fun eventDelivery(event: Int) {
        dialog?.dismiss()
        when (event) {
            EVENT_ID_SHOW_PROXY_HELP -> startSettingActivity(this)
        }
    }

    private fun startSettingActivity(context: Context) {
        val intent = Intent(context, AgentSettingActivity::class.java)
        context.startActivity(intent)
    }
}