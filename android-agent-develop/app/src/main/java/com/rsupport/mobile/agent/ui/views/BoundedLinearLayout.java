package com.rsupport.mobile.agent.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.rsupport.mobile.agent.R;

/*******************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 ********************************************************************************
 *
 * Copyright (c) 2012 RSUPPORT Co., Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.
 *
 * FileName: AccessCodeFunc.java
 * Author  : khkim
 * Date    : 2014. 1. 8.
 * Purpose : 최대 바운더리를 지정할 수 있는 costom LinearLayout
 *
 * [History]
 * -
 */

public class BoundedLinearLayout extends LinearLayout {

    /**
     * 레이아웃 최대 가로 사이즈
     **/
    private int mBoundedWidth;

    /**
     * 레이아웃 최대 세로 사이즈 (현재 사용되는 부분 없음. 차후 확장성을 위해 만들어둠)
     **/
    private int mBoundedHeight;

    public BoundedLinearLayout(Context context) {
        super(context);

        mBoundedWidth = 0;
        mBoundedHeight = 0;
    }

    public BoundedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.BoundedLayout);

        /* khkim : 단말 버전에 따라 dimens 값 읽어오는 부분에서   custom_match_parent(-1)의  int 값을 인식하지 못함
         * dip 값 없이 int만 들어오는 경우는 fill_parent 이므로 Exception 무시하면 자동 처리됨 */
        try {
            mBoundedWidth = arr.getDimensionPixelSize(R.styleable.BoundedLayout_bounded_width, 0);
        } catch (UnsupportedOperationException e) {
            mBoundedWidth = 0;
            Log.d("BoundedLinearLayout", "mBoundedWidth" + e.getStackTrace()[0].getMethodName());
        }

        try {
            mBoundedHeight = arr.getDimensionPixelSize(R.styleable.BoundedLayout_bounded_height, 0);
        } catch (UnsupportedOperationException e) {
            mBoundedHeight = 0;
            Log.d("BoundedLinearLayout", "mBoundedHeight" + e.getStackTrace()[0].getMethodName());
        }

        arr.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (mBoundedWidth > 0 && mBoundedWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedWidth, measureMode);
        }

        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mBoundedHeight > 0 && mBoundedHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedHeight, measureMode);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
