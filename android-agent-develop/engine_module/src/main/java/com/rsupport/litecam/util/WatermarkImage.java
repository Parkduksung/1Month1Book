package com.rsupport.litecam.util;

import android.content.Context;

import com.rsupport.litecam.record.RecordFormat;
import com.rsupport.litecam.record.RecordSet;

public class WatermarkImage {
    /**
     * 다음의 파일 이름은 assets 폴더의 파일 명으로 사용하여야 합니다.
     * 파일은 100x36, 200x72, 300x100 으로 3가지를 기준으로 하였습니다.
     */
    public String WATERMARK_240 = "wm_mobizen_240.png";
    public String WATERMARK_360 = "wm_mobizen_360.png";
    public String WATERMARK_480 = "wm_mobizen_480.png";
    public String WATERMARK_720 = "wm_mobizen_720.png";
    public String WATERMARK_1080 = "wm_mobizen_1080.png";
    public String WATERMARK_1440 = "wm_mobizen_1440.png";
    public String WATERMARK_2160 = "wm_mobizen_2160.png";

    /**
     * GPU에서 사용할 이미지 가져오기
     *
     * @param context
     * @param format
     * @return
     */
    public String getWatermarkName(Context context, RecordFormat format) {
        return getWatermarkName(format, RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[0]);
    }

    /**
     * CPU에서 사용할 이미지 가져오기
     *
     * @param context
     * @param format
     * @param videoSize
     * @return
     */
    public String getWatermarkName(Context context, RecordFormat format, String ratioValue) {
        ratioValue = RecordScreen.getRatioValue(context, ratioValue);

        int videoSize;
        if (ratioValue.equals("-1")) {
            videoSize = RecordScreen.getDefalutRatioScreenName(context.getApplicationContext(), ratioValue);
        } else {
            videoSize = Integer.parseInt(ratioValue);
        }

        LLog.e("Watermark size: " + videoSize);
        return "assets/" + getWatermarkName(format, videoSize);
    }

    private String getWatermarkName(RecordFormat format, int videoSize) {
        if (videoSize == RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[0]) {
            return WATERMARK_2160;
        }

        if (videoSize == RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[1]) {
            return WATERMARK_1440;
        }

        if (videoSize == RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[2]) {
            return WATERMARK_1080;
        }

        if (videoSize == RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[3]) {
            return WATERMARK_720;
        }

        if (videoSize == RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[4]) {
            return WATERMARK_480;
        }

        if (videoSize == RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[5]) {
            return WATERMARK_360;
        }

        return WATERMARK_240;
    }
}
