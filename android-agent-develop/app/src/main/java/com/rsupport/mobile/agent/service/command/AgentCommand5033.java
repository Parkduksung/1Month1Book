package com.rsupport.mobile.agent.service.command;

import com.rsupport.mobile.agent.constant.Global;

/**
 * Created by 현구 on 2015-07-28.
 * 원격제어 강제종료
 */
public class AgentCommand5033 extends AgentCommandBasic {


    @Override
    public int agentCommandexe(byte[] data, int index) {

        if (Global.getInstance().getAgentThread() != null) {
            Global.getInstance().getAgentThread().releaseAll();
            Global.getInstance().setAgentThread(null);
        }

        return 0;
    }
}
