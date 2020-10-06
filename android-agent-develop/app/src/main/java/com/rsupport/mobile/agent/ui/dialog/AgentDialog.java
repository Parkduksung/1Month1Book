package com.rsupport.mobile.agent.ui.dialog;


import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import com.rsupport.util.log.RLog;

public class AgentDialog {

    static RVDialog dialog;

    public static void showDialog(Context context, String title, int messageID, int positiveText, View.OnClickListener okListner,
                                  int negativeText, View.OnClickListener cacleListner, int style, final int timeout, final Handler timeOutHandler) {

        showDialog(context, title, context.getString(messageID), positiveText, okListner, negativeText, cacleListner, style, timeout, timeOutHandler);
    }

    public static void showDialog(Context context, String title, String message, int positiveText, View.OnClickListener okListner,
                                  int negativeText, View.OnClickListener cacleListner, int style, final int timeout, final Handler timeOutHandler) {


        RVDialog.Builder builder = new RVDialog.Builder(context);
        builder.setStyle(RVDialog.STYLE_NOTICE);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setStyle(style);

        builder.setPositive(context.getString(positiveText), 0);
        builder.setNegative(context.getString(negativeText), 0);
        if (dialog != null)
            dialog.dismiss();
        dialog = builder.create(RVDialog.TYPE_TWO_BUTTON, okListner, cacleListner);
        dialog.setCancelable(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        } else {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }


        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        dialog.setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(DialogInterface arg0) {
                RLog.d("onShowListner");
            }
        });
        dialog.show();
        if (timeout != 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (dialog != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                if (timeOutHandler != null) {
                                    timeOutHandler.sendEmptyMessage(0);
                                }
                            }
                        });
                    }
                }
            }).start();
        }
    }
}
