package com.rsupport.rsperm;

import android.provider.Settings;

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
 * FileName: UDSSettings.java<br>
 * Author  : kwcho<br>
 * Date    : 2014. 4. 4.오후 5:33:08<br>
 * Purpose : Shell 을 통해 설정 가능한 설정을 할 수 있다.<p>
 * <p>
 * [History]<p>
 * 2014.04.04 - IME 설정 추가
 */
public class UDSSettings {
    private String packageName = null;

    public UDSSettings(String packageName) {
        this.packageName = packageName;
    }

    public boolean putSFloat(IRSPerm rsperm, String name, float value) throws Exception {
        return create(name).putSFloat(rsperm, name, value);
    }

    public boolean putSInt(IRSPerm rsperm, String name, int value) throws Exception {
        return create(name).putSInt(rsperm, name, value);
    }

    public boolean putSLong(IRSPerm rsperm, String name, long value) throws Exception {
        return create(name).putSLong(rsperm, name, value);
    }

    public boolean putSString(IRSPerm rsperm, String name, String value) throws Exception {
        return create(name).putSString(rsperm, name, value);
    }

    public boolean putGFloat(IRSPerm rsperm, String name, float value) throws Exception {
        return create(name).putGFloat(rsperm, name, value);
    }

    public boolean putGInt(IRSPerm rsperm, String name, int value) throws Exception {
        return create(name).putGInt(rsperm, name, value);
    }

    public boolean putGLong(IRSPerm rsperm, String name, long value) throws Exception {
        return create(name).putGLong(rsperm, name, value);
    }

    public boolean putGString(IRSPerm rsperm, String name, String value) throws Exception {
        return create(name).putGString(rsperm, name, value);
    }

    private ISetting create(String name) {
        ISetting setting = null;
        if (Settings.Secure.DEFAULT_INPUT_METHOD.equals(name) == true) {
            setting = new IME();
        } else {
            setting = new ISetting.Stub();
        }
        return setting;
    }

    class IME extends ISetting.Stub {
        private final String KEY_BOARD_CLASS = "com.rsupport.common.android.keyboard.SoftKeyboard";

        @Override
        public boolean putSString(IRSPerm rsperm, String name, String value) throws Exception {
            String keyID = String.format("%s/%s", packageName, KEY_BOARD_CLASS);
            String setResult = rsperm.exec(String.format("/system/bin/ime set %s 2>&1", keyID));
            if (setResult != null && setResult.contains(keyID)) {
                return true;
            }
            RLog.w("keyboard com.rsupport.setting error : %s", setResult);
            return false;
        }
    }

    interface ISetting {
        public boolean putSFloat(IRSPerm rsperm, String name, float value) throws Exception;

        public boolean putSInt(IRSPerm rsperm, String name, int value) throws Exception;

        public boolean putSLong(IRSPerm rsperm, String name, long value) throws Exception;

        public boolean putSString(IRSPerm rsperm, String name, String value) throws Exception;

        public boolean putGFloat(IRSPerm rsperm, String name, float value) throws Exception;

        public boolean putGInt(IRSPerm rsperm, String name, int value) throws Exception;

        public boolean putGLong(IRSPerm rsperm, String name, long value) throws Exception;

        public boolean putGString(IRSPerm rsperm, String name, String value) throws Exception;

        class Stub implements ISetting {
            @Override
            public boolean putSFloat(IRSPerm rsperm, String name, float value) throws Exception {
                return false;
            }

            @Override
            public boolean putSInt(IRSPerm rsperm, String name, int value) throws Exception {
                return false;
            }

            @Override
            public boolean putSLong(IRSPerm rsperm, String name, long value) throws Exception {
                return false;
            }

            @Override
            public boolean putSString(IRSPerm rsperm, String name, String value) throws Exception {
                return false;
            }

            @Override
            public boolean putGFloat(IRSPerm rsperm, String name, float value) throws Exception {
                return false;
            }

            @Override
            public boolean putGInt(IRSPerm rsperm, String name, int value) throws Exception {
                return false;
            }

            @Override
            public boolean putGLong(IRSPerm rsperm, String name, long value) throws Exception {
                return false;
            }

            @Override
            public boolean putGString(IRSPerm rsperm, String name, String value) throws Exception {
                return false;
            }
        }
    }

}
