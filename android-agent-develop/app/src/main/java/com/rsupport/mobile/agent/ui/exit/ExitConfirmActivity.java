package com.rsupport.mobile.agent.ui.exit;

import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.rsupport.mobile.agent.R;

import com.rsupport.mobile.agent.ui.dialog.RVDialog;
import com.rsupport.mobile.agent.ui.base.CommonActivity;
import com.rsupport.mobile.agent.constant.Global;

public class ExitConfirmActivity extends CommonActivity {

    final int CONFIRM_OK = 1;
    final int CONFIRM_NO = 2;
    protected Handler alertHandler = new Handler();
    protected RVDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        setContentView(R.layout.activity_exit_confirm);
        showAlertDialog(null, getString(R.string.msg_endsession), RVDialog.STYLE_NOTICE, R.string.common_ok, CONFIRM_OK, R.string.re_common_no, CONFIRM_NO);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public void eventDelivery(int event) {
        if (event == CONFIRM_OK) {
            dialog.dismiss();
            Global.getInstance().getAgentThread().releaseAll();
        }
        if (event == CONFIRM_NO) {
            dialog.dismiss();
        }
        finish();

    }

    public void showAlertDialog(final String title, final String message, final int style,
                                final int positiveText, final int positiveEventID, final int negativeText, final int btnNegatEventID) {
        alertHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    RVDialog.Builder builder = createDialogBuilder(title, message, style);

                    builder.setPositive(getString(positiveText), positiveEventID);
                    builder.setNegative(getString(negativeText), btnNegatEventID);
                    if (dialog != null) dialog.dismiss();
                    dialog = builder.create(RVDialog.TYPE_TWO_BUTTON);
                    dialog.setOnDismissListener(dialog -> {
                        finish();
                    });
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private RVDialog.Builder createDialogBuilder(String title, String message, int style) {
        RVDialog.Builder builder = new RVDialog.Builder(this);
        builder.setStyle(RVDialog.STYLE_NOTICE);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setStyle(style);

        return builder;
    }
}
