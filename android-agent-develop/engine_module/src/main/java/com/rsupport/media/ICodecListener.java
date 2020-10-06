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

import java.nio.ByteBuffer;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

public interface ICodecListener {

    public void onVDeqeueFormatChanged(MediaFormat format);

    public void onVDeqeueFormatChanged(byte[] data);

    public void onVDeqeueFormatChanged(byte[] data, int offset, int size);

    public void onVDequeueOutput(byte[] data);

    public void onVDequeueOutput(byte[] data, int offset, int size);

    public void onVDequeueOutput(ByteBuffer byteBuffer, BufferInfo bufferInfo);

    public void onVideoHeader(byte[] data);

    public void onVideoHeaderRec(byte[] data);

    public void onVStart();

    public void onVStop();

    public void onVError();

    public void onADeqeueFormatChanged(MediaFormat format);

    public void onADequeueOutput(byte[] data);

    public void onADequeueOutput(ByteBuffer byteBuffer, BufferInfo bufferInfo);

    public void onAStart();

    public void onAStop();

    public void onAError();

}
