package com.rsupport.utils;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.test.AndroidTestCase;

import com.rsupport.util.CodecUtils;
import com.rsupport.util.rslog.MLog;

/**
 * Created by kwcho on 8/5/15.
 */
public class CodecUtilsTestcase extends AndroidTestCase{

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void testDetectMediaCodec(){
        MediaCodecInfo mediaCodecInfo = CodecUtils.selectAVCCodec();
        assertNotNull(mediaCodecInfo);
        MLog.e("mediaCodecInfo : " + mediaCodecInfo.getName());
    }
}
