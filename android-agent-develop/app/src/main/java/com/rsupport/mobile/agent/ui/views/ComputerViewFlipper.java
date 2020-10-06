/**
 * @author hyunwoo-kim
 * <p>
 * 특정기기에서 가로모드에서 다이렉트로 가로모드로 돌아갈 시 crash나는 현상이 발견하여
 * CustomViewFlipper로 예외처리.
 */

package com.rsupport.mobile.agent.ui.views;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import com.rsupport.util.log.RLog;

public class ComputerViewFlipper extends ViewFlipper {

    public ComputerViewFlipper(Context context) {
        super(context);
    }

    public ComputerViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch (IllegalArgumentException e) {
            stopFlipping();
            RLog.d("ComputerViewFlipper", "FLIPER ::: IllegalArgumentException ");
        }

    }

}
