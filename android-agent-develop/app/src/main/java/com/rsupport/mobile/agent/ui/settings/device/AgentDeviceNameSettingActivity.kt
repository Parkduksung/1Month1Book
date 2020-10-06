package com.rsupport.mobile.agent.ui.settings.device

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import com.rsupport.mobile.agent.ui.dialog.RVDialog
import com.rsupport.mobile.agent.ui.base.RVCommonActivity
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.constant.GlobalStatic

class AgentDeviceNameSettingActivity : RVCommonActivity() {
    private lateinit var deviceName: EditText
    private val EVENT_ID_NONE = 0
    private val EVENT_ID_CHECK_WORD = 1
    private lateinit var errorHandler: Handler
    private val apiService by inject(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        errorHandler = Handler(callBack)
        setContentView(R.layout.activity_agent_device_name_setting, R.layout.layout_common_bg_margin)
        setBottomTitle(R.string.save, null)
        setTitle(R.string.agent_display_name, true, false)
        setLeftButtonBackground(R.drawable.button_headerback)
        deviceName = findViewById<View>(R.id.device_name) as EditText
        deviceName.setText(AgentBasicInfo.getAgentName(applicationContext))
        deviceName.setSelection(deviceName.length())
        val btnTitleLeft = findViewById<View>(R.id.left_button) as ImageButton
        btnTitleLeft.setOnClickListener { v: View? ->
            hideKeyboard(deviceName)
            finish()
        }
    }

    private fun save() {
        if (checkRull()) {
            requestNameChange()
        }
    }

    /**
     * 하단 타이틀바 (클릭버튼용) 기능 *
     */
    fun Click_ButtomTitle(v: View?) {
        save()
    }

    private fun requestNameChange() {
        CoroutineScope(Dispatchers.Main).launch {
            val guid = AgentBasicInfo.getAgentGuid(applicationContext)
            val displayName = deviceName.text.toString()
            val ssl = AgentBasicInfo.RV_AGENT_SSL
            val agentIP = GlobalStatic.getLocalIP()
            val macAddrss = GlobalStatic.getMacAddress(this@AgentDeviceNameSettingActivity)

            withContext(Dispatchers.IO) {
                val changeDeviceNameResult = apiService.deviceNameChange(guid, displayName, ssl, agentIP, macAddrss)

                if (changeDeviceNameResult.isSuccess) {
                    AgentBasicInfo.setAgentName(applicationContext, displayName)
                    val interactor by inject(AgentInfoInteractor::class.java)
                    interactor.updateAgentInfo()
                    interactor.release()
                    finish()
                } else errorHandler.sendEmptyMessage(0)
            }
        }
    }


    private val callBack = Handler.Callback { msg ->
        hideProgressHandler()
        if (msg?.what == 0) {
            var errorMsg: String? = ""
            errorMsg = when (GlobalStatic.g_errNumber) {
                111 -> getString(R.string.msg_inputlogininfo)
                113 -> getString(R.string.msg_inputlogininfo)
                911 -> GlobalStatic.g_err
                else -> GlobalStatic.g_err
            }
            showAlertDialog(null, errorMsg, RVDialog.STYLE_NOTICE, R.string.common_ok, 0)
        } else if (msg?.what == 1) {
            finish()
        }
        false
    }

    private fun checkRull(): Boolean {
        if (deviceName.text.toString().trim { it <= ' ' } == "") {
            showErrDialog(R.string.devicename_notice)
            return false
        }
        if (deviceName.length() > 50) {
            showErrDialog(R.string.devicename_langth_notice)
            return false
        }
        if (!invalidWordCheck(deviceName.text.toString())) {
            showErrDialog(R.string.re_following_characters)
            return false
        }
        return true
    }

    private fun showErrDialog(stringID: Int) {
        showAlertDialog(null, getString(stringID), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENT_ID_NONE, false)
    }

    private fun invalidWordCheck(str: String): Boolean {
        val err_char = charArrayOf('\\', '/', ':', '*', '?', '\"', '<', '>', '|', '%', '+', ';')
        for (i in 0 until str.length) {
            for (j in err_char.indices) {
                if (str[i] == err_char[j]) {
                    return false
                }
            }
        }
        return true
    }


    override fun eventDelivery(event: Int) {
        if (dialog != null) {
            dialog.dismiss()
        }
    }
}