/*
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
 *
 * Author  : Park Sung Yeon <br>
 * Date    : 2014. .  <br>
 *
 */

package com.rsupport.media;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public class ICodecAdapter implements ICodecListener {

    @Override
    public void onVDeqeueFormatChanged(MediaFormat format) {
    }

    @Override
    public void onVDeqeueFormatChanged(byte[] data) {
    }

    @Override
    public void onVDeqeueFormatChanged(byte[] data, int offset, int size) {
    }

    @Override
    public void onVDequeueOutput(byte[] data) {
    }

    @Override
    public void onVDequeueOutput(byte[] data, int offset, int size) {
    }

    @Override
    public void onVDequeueOutput(ByteBuffer byteBuffer, BufferInfo bufferInfo) {
    }

    @Override
    public void onVideoHeader(byte[] data) {
    }

    @Override
    public void onVideoHeaderRec(byte[] data) {
    }

    @Override
    public void onVStart() {
    }

    @Override
    public void onVStop() {
    }

    @Override
    public void onVError() {
    }

    @Override
    public void onADeqeueFormatChanged(MediaFormat format) {
    }

    @Override
    public void onADequeueOutput(byte[] data) {
    }

    @Override
    public void onADequeueOutput(ByteBuffer byteBuffer, BufferInfo bufferInfo) {
    }

    @Override
    public void onAStart() {
    }

    @Override
    public void onAStop() {
    }

    @Override
    public void onAError() {
    }

}
