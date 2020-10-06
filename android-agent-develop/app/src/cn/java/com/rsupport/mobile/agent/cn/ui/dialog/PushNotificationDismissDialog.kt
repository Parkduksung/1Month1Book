package com.rsupport.mobile.agent.cn.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.service.AgentLoginManager
import com.rsupport.mobile.agent.service.AgentMainService
import com.rsupport.mobile.agent.ui.base.CommonActivity
import com.rsupport.mobile.agent.ui.dialog.RVDialog

class PushNotificationDismissDialog : CommonActivity() {

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog = RVDialog.Builder(this)
                .setStyle(RVDialog.STYLE_NOTICE)
                .setNegative(getString(R.string.re_common_no), 0)
                .setPositive(getString(R.string.re_common_yes), 0)
                .setMessage(getString(R.string.push_notification_dialog_desc))
                .create(
                        RVDialog.TYPE_TWO_BUTTON,
                        // ok
                        {
                            stopService()
                        },
                        // cancel
                        {}
                ).apply {
                    setOnDismissListener {
                        finish()
                    }
                }
        dialog?.show();
    }

    override fun onDestroy() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        dialog = null;
        super.onDestroy()
    }

    private fun stopService() {
        AgentLoginManager.getInstence().stopMQTTPushService()

        val intent = Intent(this, AgentMainService::class.java)
        intent.putExtra(AgentMainService.AGENT_SERVICE_NOTI_CLOSE, "OK")
        startService(intent)
    }
}