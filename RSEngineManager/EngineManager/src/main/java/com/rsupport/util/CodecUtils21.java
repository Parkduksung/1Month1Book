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
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CodecUtils21 extends CodecUtils{

    @Override
    public MediaCodecInfo getMediaCodecInfo(){
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodecInfo[] mediaCodecInfos = mediaCodecList.getCodecInfos();

        LinkedHashMap<String, MediaCodecInfo> avcCodecTable = new LinkedHashMap<>();

        int numCodecs = mediaCodecInfos != null ? mediaCodecInfos.length: 0;

        // set supported avc codecs to the avcCodecTable.
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = mediaCodecInfos[i];

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
