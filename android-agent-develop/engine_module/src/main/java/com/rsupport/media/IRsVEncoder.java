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

public interface IRsVEncoder {

    public IRsVEncoder createInstance();

    public void start();

    public void stop();

    public boolean configure(RsMediaCodecVideo.Builder builder);

    public void setCodecListener(ICodecListener codecListener);

    public void queueInput(byte[] data, int offset, int size, boolean finish);

    public int getColorFormat();

    public Object getOutputFormat();

}
