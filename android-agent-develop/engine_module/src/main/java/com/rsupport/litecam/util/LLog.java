/*
 * Copyright (C) 2014 Taehwan Kwon (thkwon@rsupport.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rsupport.litecam.util;

import android.util.Log;

public class LLog {
    private static final String TAG = "liteCam";
    private static boolean isLog = false;

    public static void setUsed(boolean isLog) {
        LLog.isLog = isLog;
    }

    public static boolean getUsed() {
        return isLog;
    }

    private static String srcAt() {
        StackTraceElement ste = new Throwable().getStackTrace()[2];
        return String.format(" at %s(%s:%d)",
                ste.getMethodName(), ste.getFileName(), ste.getLineNumber());
    }

    // //////////////////////////////////////////

    /**
     * DEBUG 로그 출력으로 {@link #isLog}에 따라서 표시가 안될 수 있다.
     *
     * @param msg
     * @param args
     */
    public static void d(String msg, Object... args) {
        if (isLog)
            Log.d(TAG, String.format(msg + srcAt(), args));
    }

    /**
     * DEBUG 로그 출력으로 {@link #isLog}에 따라서 표시가 안될 수 있다.
     *
     * @param msg
     * @param args
     */
    public static void e(String msg, Object... args) { // Send an ERROR log message.
        Log.e(TAG, String.format(msg + srcAt(), args));
    }

    /**
     * DEBUG 로그 출력으로 {@link #isLog}에 따라서 표시가 안될 수 있다.
     *
     * @param msg
     * @param args
     */
    public static void i(String msg, Object... args) { // Send an INFO log message.
        if (isLog) {
            Log.i(TAG, String.format(msg + srcAt(), args));
        }
    }

    /**
     * DEBUG 로그 출력으로 {@link #isLog}에 따라서 표시가 안될 수 있다.
     *
     * @param msg
     * @param args
     */
    public static void w(String msg, Object... args) { // Send a WARN log message.
        if (isLog)
            Log.w(TAG, String.format(msg + srcAt(), args));
    }


    // //////////////////////////////////////////

    /**
     * TAG 표리여부를 직접 선택 할 수 있다.
     *
     * @param msg
     * @param isLog
     * @param args
     */
    public static void d(boolean isLog, String msg, Object... args) { // Send a DEBUG log message.
        if (isLog)
            Log.d(TAG, String.format(msg + srcAt(), args));
    }

    /**
     * TAG 표리여부를 직접 선택 할 수 있다.
     *
     * @param msg
     * @param isLog
     * @param args
     */
    public static void e(boolean isLog, String msg, Object... args) { // Send an ERROR log message.
        if (isLog)
            Log.e(TAG, String.format(msg + srcAt(), args));
    }

    /**
     * TAG 표리여부를 직접 선택 할 수 있다.
     *
     * @param msg
     * @param isLog
     * @param args
     */
    public static void i(boolean isLog, String msg, Object... args) { // Send an INFO log message.
        if (isLog) {
            Log.i(TAG, String.format(msg + srcAt(), args));
        }
    }

    /**
     * TAG 표리여부를 직접 선택 할 수 있다.
     *
     * @param msg
     * @param isLog
     * @param args
     */
    public static void w(boolean isLog, String msg, Object... args) { // Send a WARN log message.
        if (isLog) {
            Log.w(TAG, String.format(msg + srcAt(), args));
        }
    }


    // //////////////////////////////////////////

    /**
     * TAG, msg를 직접 전송한다.
     *
     * @param tag
     * @param msg
     */
    public static void d(String tag, String msg, Object... args) { // Send a DEBUG log message.
        if (isLog)
            Log.d(tag, String.format(msg + srcAt(), args));
    }

    /**
     * TAG, msg를 직접 전송한다.
     *
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg, Object... args) { // Send an ERROR log message.
        Log.e(tag, String.format(msg + srcAt(), args));
    }

    /**
     * TAG, msg를 직접 전송한다.
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg, Object... args) { // Send an INFO log message.
        if (isLog)
            Log.i(tag, String.format(msg + srcAt(), args));
    }

    /**
     * TAG, msg를 직접 전송한다.
     *
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg, Object... args) { // Send a WARN log message.
        if (isLog)
            Log.w(tag, String.format(msg + srcAt(), args));
    }


    // //////////////////////////////////////////

    /**
     * TAG, msg를 직접 전송한다.
     *
     * @param tag
     * @param msg
     */
    public static void d(boolean isLog, String tag, String msg, Object... args) { // Send a DEBUG log message.
        if (isLog)
            Log.d(tag, String.format(msg + srcAt(), args));
    }

    /**
     * TAG, msg를 직접 전송한다.
     *
     * @param tag
     * @param msg
     */
    public static void e(boolean isLog, String tag, String msg, Object... args) { // Send an ERROR log message.
        if (isLog)
            Log.e(tag, String.format(msg + srcAt(), args));
    }

    /**
     * TAG, msg를 직접 전송한다.
     *
     * @param tag
     * @param msg
     */
    public static void i(boolean isLog, String tag, String msg, Object... args) { // Send an INFO log message.
        if (isLog)
            Log.i(tag, String.format(msg + srcAt(), args));
    }

    /**
     * TAG, msg를 직접 전송한다.
     *
     * @param tag
     * @param msg
     */
    public static void w(boolean isLog, String tag, String msg, Object... args) { // Send a WARN log message.
        if (isLog)
            Log.w(tag, String.format(msg + srcAt(), args));
    }
}
