package com.rsupport.mobile.agent.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.rsupport.mobile.agent.R;

import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.util.log.RLog;

/**
 * Created by Hyungu-PC on 2015-07-06.
 * 접속중 5분 주기로 토스트 메세지 출력을 위한 리시버
 */
public class ConnetingNoticeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(GlobalStatic.CONNETING_MESSAGE_ID)) {
            if (Global.getInstance().getAgentThread() == null) return;
            if (!Global.getInstance().getAgentThread().isAlive()) return;

            RLog.i("ConnectingNotice Toast");
            Toast.makeText(context, R.string.connecting_notice_message, Toast.LENGTH_LONG).show();
        }
    }
}
