package com.rsupport.mobile.agent.ui.launcher

import android.content.Intent
import android.os.Bundle
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoActivity
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import com.rsupport.mobile.agent.ui.login.LoginActivity
import org.koin.java.KoinJavaComponent.inject
import com.rsupport.mobile.agent.ui.base.RVCommonActivity

class LauncherActivity : RVCommonActivity() {

    private val agentInfoInteractor: AgentInfoInteractor by inject(AgentInfoInteractor::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (agentInfoInteractor.isAgentInstalled()) {
            startActivity(AgentInfoActivity.forIntent(this))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agentInfoInteractor.release()
    }
}