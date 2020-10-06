package com.rsupport.mobile.agent.ui.settings.basic

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.ui.base.RVCommonActivity

// 에이전트 관리
class BasicSettingActivity : RVCommonActivity() {

    companion object {
        private const val EVENT_ID_NO = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agent_pre_setting_activity, R.layout.layout_common_bg_margin)
        setTitle(R.string.agent_pre_setting_title, true, false)
        setLeftButtonBackground(R.drawable.button_headerback)
        val btnTitleLeft = findViewById<View>(R.id.left_button) as ImageButton
        btnTitleLeft.setOnClickListener { finish() }
    }

    override fun eventDelivery(event: Int) {
        when (event) {
            EVENT_ID_NO -> if (dialog != null) {
                dialog.dismiss()
            }
        }
    }
}