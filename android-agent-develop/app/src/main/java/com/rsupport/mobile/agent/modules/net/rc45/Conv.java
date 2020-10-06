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
 * FileName: Conv.java
 * Author  : "jjkim@rsupport.com"
 * Date    : 2013. 7. 30.
 * Purpose : byte 와 기본형변수들을 littleEndian, BigEndian 으로 서로간 conversion 할수있도록 함.
 *
 * [History]
 *
 * 2013. 7. 30. -Initialize
 * 2013. 8. 05  -BigEndian 변환코드 추가 (kyeom@rsupport.com)
 *
 */
package com.rsupport.mobile.agent.modules.net.rc45;


import com.rsupport.util.log.RLog;

import java.io.UnsupportedEncodingException;

public class Conv {

    private Conv() {
        throw new AssertionError();
    }

    /* *********************************************************
     *          Primitive type ---> BigEndian bytes            *
     ***********************************************************/

    /**
     * int => BigEndian
     */
    public static int intToByte4BE(int n, byte[] dst, int offset) {
        dst[offset + 0] = (byte) ((n >> 24));
        dst[offset + 1] = (byte) ((n >> 16));
        dst[offset + 2] = (byte) ((n >> 8));
        dst[offset + 3] = (byte) (n);
        return offset + 4;
    }

    /**
     * short => BigEndian
     */
    public static int shortToByte2BE(short n, byte[] dst, int offset) {
        dst[offset + 0] = (byte) ((n >> 8));
        dst[offset + 1] = (byte) (n);
        return offset + 2;
    }

    /* *********************************************************
     *          BigEndian bytes ---> Primitive type            *
     ***********************************************************/

    /**
     * BigEndian => int
     */
    public static int byte4ToIntBE(byte[] src, int offset) {
        return ((src[offset + 0]) << 24) |
                (src[offset + 1] & 0xff) << 16 |
                (src[offset + 2] & 0xff) << 8 |
                (src[offset + 3] & 0xff);
    }

    /**
     * BigEndian => short
     */
    public static short byte2ToShortBE(byte[] src, int offset) {
        return (short) ((src[offset + 0] & 0xff) << 8 |
                (src[offset + 1] & 0xff));
    }


    /* *********************************************************
     *          Primitive type ---> LittleEndian bytes         *
     ***********************************************************/

    /**
     * int => littleEndian
     */
    public static int int2byte(int n, byte[] dst, int offset) {
        dst[offset] = (byte) (n);
        return offset + 1;
    }

    /**
     * int => littleEndian
     */
    public static int int2byte4(int n, byte[] dst, int offset) {
        dst[offset + 0] = (byte) (n);
        dst[offset + 1] = (byte) (n >> 8);
        dst[offset + 2] = (byte) (n >> 16);
        dst[offset + 3] = (byte) (n >> 24);
        return offset + 4;
    }

    /**
     * int => littleEndian
     */
    public static int int2byte2(int n, byte[] dst, int offset) {
        dst[offset + 0] = (byte) (n);
        dst[offset + 1] = (byte) (n >> 8);
        return offset + 2;
    }

    /**
     * short => littleEndian
     */
    public static int short2byte2(short n, byte[] dst, int offset) {
        dst[offset + 0] = (byte) (n);
        dst[offset + 1] = (byte) (n >> 8);
        return offset + 2;
    }

    /* *********************************************************
     *          LittleEndian bytes ---> Primitive type         *
     ***********************************************************/

    /**
     * littleEndian => int
     */
    public static int byte4Toint(byte[] src, int offset) {
        int ret = (src[offset + 3] & 0xff);
        ret = (ret << 8) | (src[offset + 2] & 0xff);
        ret = (ret << 8) | (src[offset + 1] & 0xff);
        ret = (ret << 8) | (src[offset + 0] & 0xff);
        return ret;
    }

    /**
     * littleEndian => int
     */
    public static int byte2Toint(byte[] src, int offset) {
        int ret = (src[offset + 1] & 0xff);
        ret = (ret << 8) | (src[offset + 0] & 0xff);
        return ret;
    }

    /**
     * littleEndian => shrot
     */
    public static short byte2Toshort(byte[] src, int offset) {
        int ret = (src[offset + 1] & 0xff);
        ret = (ret << 8) | (src[offset + 0] & 0xff);
        return (short) ret;
    }

    public static int stringToBytes(String str, byte[] dst, int offset, String charset) throws UnsupportedEncodingException {
        if (str == null || str.equals("")) {
            return offset;
        }

        byte[] strBytes = str.getBytes(charset);

        System.arraycopy(strBytes, 0, dst, offset, strBytes.length);
        offset += strBytes.length;

        if (charset.equalsIgnoreCase("UTF-8")) {
            //none
        } else if (charset.equalsIgnoreCase("UTF-16LE")) {
            dst[offset] = 0;
            offset += 1;
            dst[offset] = 0;
            offset += 1;
        } else {

        }

        strBytes = null;
        return offset;
    }

    public static int len_bytesBEToString(byte[] srcBytes, int offset, StringBuilder destText, String charset) {

        int len = Conv.byte4ToIntBE(srcBytes, offset);
        offset += 4;

        try {
            destText.append(new String(srcBytes, offset, len, charset));
            offset += len;

        } catch (UnsupportedEncodingException e) {
            RLog.e(e);
        }
        return offset;
    }

    public static int stringToLen_BytesBE(String str, byte[] dst, int offset, String charset) throws UnsupportedEncodingException {
        if (str == null || str.equals("")) {
            return intToByte4BE(0, dst, offset);
        }
        byte[] strBytes = str.getBytes(charset);

        if (charset.equalsIgnoreCase("UTF-8")) {

            offset = intToByte4BE(strBytes.length, dst, offset);
            System.arraycopy(strBytes, 0, dst, offset, strBytes.length);
            offset += strBytes.length;

        } else if (charset.equalsIgnoreCase("UTF-16LE")) {

            offset = intToByte4BE(strBytes.length + 2, dst, offset);
            System.arraycopy(strBytes, 0, dst, offset, strBytes.length);
            offset += strBytes.length;

            dst[offset] = 0;
            offset += 1;
            dst[offset] = 0;
            offset += 1;

        } else {
            offset = intToByte4BE(strBytes.length, dst, offset);
            System.arraycopy(strBytes, 0, dst, offset, strBytes.length);
            offset += strBytes.length;
        }
        strBytes = null;
        return offset;
    }

    public static int stringToLen_Bytes(String str, byte[] dst, int offset, String charset) throws UnsupportedEncodingException {
        if (str == null || str.equals("")) {
            return int2byte4(0, dst, offset);
        }
        byte[] strBytes = str.getBytes(charset);

        if (charset.equalsIgnoreCase("UTF-8")) {

            offset = int2byte4(strBytes.length, dst, offset);
            System.arraycopy(strBytes, 0, dst, offset, strBytes.length);
            offset += strBytes.length;

        } else if (charset.equalsIgnoreCase("UTF-16LE")) {

            offset = int2byte4(strBytes.length + 2, dst, offset);
            System.arraycopy(strBytes, 0, dst, offset, strBytes.length);
            offset += strBytes.length;

            dst[offset] = 0;
            offset += 1;
            dst[offset] = 0;
            offset += 1;

        } else {
            offset = int2byte4(strBytes.length, dst, offset);
            System.arraycopy(strBytes, 0, dst, offset, strBytes.length);
            offset += strBytes.length;
        }
        strBytes = null;
        return offset;
    }

    public static int fillString(String str, byte[] dst, int idst) {

        if (str != null) {
            byte[] strBytes = null;
            try {
                strBytes = str.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                RLog.w(e);
            }
            int len = strBytes.length;
            System.arraycopy(strBytes, 0, dst, idst, len);
            idst += len;
        }
        dst[idst] = 0; // null-terminated.
        return idst + 1;
    }

    public static int arraycopy(byte[] srcBuffer, int srcOffset, int srcLen, byte[] dstBuffer, int dstOffset) {
        System.arraycopy(srcBuffer, srcOffset, dstBuffer, dstOffset, srcLen);
        return dstOffset + srcLen;
    }

}
