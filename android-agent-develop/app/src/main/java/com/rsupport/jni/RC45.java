/********************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 ********************************************************************************
 *
 * Copyright (c) 2013 RSUPPORT Co., Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.
 *
 * FileName: RC45.java
 * Author  : "kyeom@rsupport.com"
 * Date    : 2012. 5. 13.
 * Purpose : rc45.so 파일을 load 하여 이용할 수 있도록 함.
 *
 * [History]
 *
 * 2013. 7. 24. -Initialize
 *
 */
package com.rsupport.jni;

import android.content.Context;

/**
 * Connecting Source for link with native librc45.so
 * <p>
 * This Class is a library.
 */
public class RC45 {

    final String TEST_JNIPATH = "/data/local/tmp/librc45.so";


    static {
//		System.load("/data/local/tmp/librc45.so");
        System.loadLibrary("rc45");
    }

    public RC45(Context context) {
        setCallBack(context);
    }

    private boolean setCallBack(Context context) {

        boolean isOK = rc45_scb(this, context);

        return isOK;
    }

    /**
     * Callback method
     */
    public void je01(byte[] data) {
    }

    public int je02(byte[] data) {
        return -1;
    }

    public int je03() {
        return -1;
    }

    public native int rc45_d2n(byte[] bulk, int len);

    public native boolean rc45_scb(Object obj, Context context);
}

