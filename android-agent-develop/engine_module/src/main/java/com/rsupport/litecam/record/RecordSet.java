package com.rsupport.litecam.record;

import java.text.SimpleDateFormat;

import android.media.AudioFormat;

import com.rsupport.litecam.util.DirectoryManage;

/**
 * Record에 필요한 데이터 정보.
 *
 * @author taehwan
 */
public class RecordSet {
    public static final String VIDEO_MIME_TYPE = "video/avc"; // H.264 Advanced
    public static final int IFRAME_INTERVAL = 10; // 5 seconds between I-frames
    public static final int FRAME_RATE = 30; // FPS


    // Audio
    public static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    public static int RECORD_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    public static int AUDIO_CHANNEL_COUNT = 2;
    public static final int RECORD_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * Watermark default true
     */
    public static boolean ISWatermark = true;

    /**
     * GPU is default value false.
     */
    public static boolean ISGPU = false;
    public static int RS_COLOR_FORMAT = -1;

    /**
     * temp file의 path
     */
    public static String FILE_PATH = null;

    /**
     * Android {@link android.media.MediaMuxer}에서 하나의 파일의 최대 용량은 2GB로 정의되어 있어, 2GB를 넘지 않도록 확인하기 위한 변수
     * {@link #isEncoderTotalSize} 을 통해서 2GB가 초과하였는지 확인이 가능하며,
     *
     * <p>{@link #videoDataOutput} {@link #audioDataOutput}의 OutputBuffer의 크기를 더하여 계산한다.
     */
    public static long ENCODER_TOTAL_SIZE;

    public static final String DEFAULT_FILE_PATH = "/mobizen/record";

    /**
     * 파일 이름은 "liteCam_{@link #FILE_DATE_TYPE}.mp4 으로 저장 되며 기본 이름은 아래와 같습니다.
     * {@link DirectoryManage}에서 사용하는 변수입니다.
     */
    public static final String DEFAULT_FILE_NAME = "mobizen_";

    /**
     * Video의 Defualt bitrate 5 MB
     */
    public static final int DEFAULT_VIDEO_BIT_RATE = 5242880;

    /**
     * Audio Default sample rate 8000
     */
    public static final int DEFAULT_SAMPLE_RATE = 8000;

    /**
     * Audio Default bit rate 96000
     */
    public static int DEFAULT_AUDIO_BIT_RATE = 96000;

    /**
     * Audio Default recording is true
     */
    public static final boolean DEFAULT_AUDIORECORD = true;

    /**
     * GPU 사용 false
     */
    public static final boolean DEFAULT_IS_GPU = false;

    /**
     * 파일에 사용하는 날짜. 시분초는 아래와 같이 설정됩니다. {@link SimpleDateFormat} 함수를 사용하여 설정합니다.
     * {@link DirectoryManage}에서 사용하는 변수입니다.
     */
    public static final String FILE_DATE_TYPE = "yyyyMMdd_HHmmss";

    /**
     * Dual core 제품 중 해상도가 x, y가 1000 미만의 경우 75%로 강제 변경한다.
     */
    public static final float VIDEO_DUAL_CORE_DEFAULT_RATIO = .6f;
    public static final int VIDEO_DUAL_CORE_AVAILABLE_SIZE = 1000;

    /**
     * Dual core 셋팅
     */
    public static final int DUAL_CORE = 2;

    /**
     * Quad core 셋팅
     */
    public static final int QUAD_CORE = 4;

    /**
     * Video 기본 배율로 75%
     */
    public static final float VIDEO_DEFAULT_RATIO = .75f;

    /**
     * 최대 화면 배율 100% 이하
     */
    public static final float VIDEO_MAX_RATIO = 1.0f;

    /**
     * 최소 화면 배율 30%
     */
//	public static final float VIDEO_MIN_RATIO_4K = .2f;

    /**
     * 최소 화면 배율 30%
     */
    public static final float VIDEO_MIN_RATIO = .35f;

    /**
     * Video default size 중 min 사이즈를 지정한다.
     */
    public static final int DEFAULT_VIDEO_MIN_RESOLUTION_TYPE = 240;

    /**
     * Video의 default size
     * <p>
     * Youtube 의 해상도 문서를 참고하여 작성.
     * https://support.google.com/youtube/answer/1722171?hl=ko
     */
    public static final int[] DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE = {2160, 1440, 1080, 720, 480, 360, 240};

    /**
     * Quad core 기준 최대 limit
     */
    public static final int BANCHMARK_QUAD_CORE_FPS_LIMITE = 20;

    /**
     * Dual core 기준 최대 limit
     */
    public static final int BANCHMARK_DUAL_CORE_FPS_LIMITE = 15;

    /**
     * 1 sec를 기준으로 FPS를 정의한다.
     */
    public static final int BANCHMARK_FPS_TIME = 1000;

    /**
     * CPU Count
     */
    public static int CPU_COUNT = -1;

    public static final int BANCHMARK_FPS_AUTO = 0;
    public static final int BANCHMARK_FPS_NO_LIMITS = -1;

    public static final int[] DEFAULT_FRAME_RATE = {0, 5, 10, 15, 20, -1};
}
