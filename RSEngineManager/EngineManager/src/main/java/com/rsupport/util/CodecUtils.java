package com.rsupport.util;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;

import com.rsupport.util.rslog.MLog;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by kwcho on 8/5/15.
 * MediaCodec 관련된 기능을 제공하는 class.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class CodecUtils {

    private static CodecUtils codecUtils = null;

    static {
        CodecUtils.codecUtils = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? new
                CodecUtils21() : new CodecUtils();
    }

    /**
     * h264 encoding 지원 하는 codec.
     */
    protected static final String[] SUPPORTED_H264_HW_CODEC_PREFIXES = {
            "OMX.qcom."
    };

    protected static final String[][] DEVICE_DEPENDENCE_CODEC = {
             {"sm-t111m" , "samsung", "OMX.MARVELL.VIDEO.H264ENCODER"} //Tab 3
            ,{"sm-t111" , "samsung", "OMX.MARVELL.VIDEO.H264ENCODER"} //Tab 3 Lite wifi-3g
            ,{"sm-t110" , "samsung", "OMX.MARVELL.VIDEO.H264ENCODER"} //Tab 3 Lite wifi
            ,{"GT-I8200N", "samsung", "OMX.MARVELL.VIDEO.H264ENCODER"} //겔럭시S3 mini
            ,{"GT-I8200", "samsung", "OMX.MARVELL.VIDEO.H264ENCODER"} //겔럭시S3 mini
            ,{"GT-I8200L", "samsung", "OMX.MARVELL.VIDEO.H264ENCODER"} //겔럭시S3 mini
            ,{"GT-I8200Q", "samsung", "OMX.MARVELL.VIDEO.H264ENCODER"} //겔럭시S3 mini
    };

    public static MediaCodecInfo selectAVCCodec(){
        return codecUtils.getMediaCodecInfo();
    }

    protected MediaCodecInfo getMediaCodecInfo(){
        LinkedHashMap<String, MediaCodecInfo> avcCodecTable = new LinkedHashMap<>();

        int numCodecs = MediaCodecList.getCodecCount();

        // set supported avc codecs to the avcCodecTable.
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }
            for (String type: codecInfo.getSupportedTypes()) {
                if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                    String codecName = codecInfo.getName();
                    if(avcCodecTable.containsKey(codecName) == false){
                        avcCodecTable.put(codecName, codecInfo);
                        break;
                    }
                    else{
                        MLog.w("already contain codec : " + codecName);
                    }
                }
            }
        }

        Set<String> keySet = avcCodecTable.keySet();

        // filter device dependence codec.
        for (String[] deviceInfo : DEVICE_DEPENDENCE_CODEC) {
            if(Build.MODEL.toLowerCase().equals(deviceInfo[0].toLowerCase()) == true &&
                    Build.MANUFACTURER.toLowerCase().equals(deviceInfo[1].toLowerCase()) == true){
                for(String codecName : keySet){
                    if(codecName.toLowerCase().startsWith(deviceInfo[2].toLowerCase()) == true){
                        MLog.i("SelectCodec : " + codecName);
                        return avcCodecTable.get(codecName);
                    }
                }
            }
        }

        // detect supported h264 codec.
        for (String hwCodecPrefix : SUPPORTED_H264_HW_CODEC_PREFIXES) {
            for(String codecName : keySet){
                if(codecName.toLowerCase().startsWith(hwCodecPrefix.toLowerCase()) == true){
                    MLog.i("SelectCodec : " + codecName);
                    return avcCodecTable.get(codecName);
                }
            }
        }

        // return first the element
        if(avcCodecTable.size() > 0){
            return avcCodecTable.get(avcCodecTable.keySet().iterator().next());
        }

        return null;
    }
}
