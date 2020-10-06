package com.rsupport.mobile.agent.service.command;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import com.rsupport.mobile.agent.BuildConfig;
import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoActivity;

import com.rsupport.mobile.agent.constant.AgentNotificationBar;
import com.rsupport.mobile.agent.constant.Global;

/**
 * 강제업데이트 알림
 */
public class AgentCommand3033 extends AgentCommandBasic {

    public static final int AGENT_UPDATE_NOTIFICATION_ID = 1000;
    public static final String CALL_UPDATE_DIALOG = "force_update";

    @Override
    //data null 값으로 넘어옴.
    public int agentCommandexe(byte[] data, int index) {
        Context context = Global.getInstance().getAppContext();
        createNotification(context);
        startAgentinfoActivity(context);
        return 0;
    }

    private void startAgentinfoActivity(Context context) {

        Intent intent = AgentInfoActivity.forIntent(context);
        intent.putExtra(CALL_UPDATE_DIALOG, true);
        context.startActivity(intent);
    }

    private void createNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, market, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AgentNotificationBar.AGENT_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.weberr_update_force))
                .setSmallIcon(R.drawable.statusicon)
                .setTicker(context.getString(R.string.weberr_update_force))
                .setAutoCancel(true)
                .setContentIntent(contentIntent);
        Notification notification = builder.build();
        notificationManager.notify(AGENT_UPDATE_NOTIFICATION_ID, notification);
    }
}
