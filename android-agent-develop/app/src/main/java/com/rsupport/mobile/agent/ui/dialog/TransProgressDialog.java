package com.rsupport.mobile.agent.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.rsupport.mobile.agent.R;
import com.rsupport.util.log.RLog;

/**
 * <pre>*******************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 * *******************************************************************************</pre>
 *
 * <b>Copyright (c) 2012 RSUPPORT Co., Ltd. All Rights Reserved.</b><p>
 *
 * <b>NOTICE</b> :  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.<p>
 * <p>
 * FileName: TransProgressDialog.java<br>
 * Author  : khkim<br>
 * Date    : 2014. 3. 5<br>
 * Purpose : 반투명 프로그래스 효과를 위한 Costum Dialog <p>
 * <p>
 * [History]<p>
 */

public class TransProgressDialog extends Dialog {

    public static TransProgressDialog show(Context context, CharSequence title, String message, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
        TransProgressDialog dialog = null;
        dialog = new TransProgressDialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.progress_dialog);

        TextView progressText = (TextView) dialog.findViewById(R.id.progress_text);
        if (progressText != null) {
            progressText.setText(message);
        }

        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);

        // dialog.setTitle(title);

        // rotation animation
        ImageView progressImage = (ImageView) dialog.findViewById(R.id.progress_image);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.progress_rotate);
        RLog.d(progressImage + ", " + animation);
        progressImage.startAnimation(animation);

        dialog.getWindow().setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);

        dialog.show();
        return dialog;
    }

    public TransProgressDialog(Context context) {
        super(context, R.style.ProgressDialog);
    }

    public TransProgressDialog(Context context, int style) {
        super(context, style);
    }
}
