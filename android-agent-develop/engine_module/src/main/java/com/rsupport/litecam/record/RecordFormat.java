package com.rsupport.litecam.record;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.view.Surface;

import com.rsupport.litecam.util.WatermarkImage;

/**
 * RecordManager에서 사용하는 RecordFormat이며, 기본 Data Type과 Audio/Video에 대한 setting이다.
 * 해당 클래스의 KEY 값은 RecordPreference에서 함께 사용된다.
 *
 * <p>
 * Keys common to all audio/video formats, <b>all keys not marked optional are
 * mandatory</b>:
 *
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Value Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>{@link #KEY_FILE_PATH}</td>
 * <td>String</td>
 * <td>파일의 저장 경로. ex)/sdcard/mobizen</td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_FILE_NAME}</td>
 * <td>String</td>
 * <td>파일의 시작이름을 지정합니다. _ 뒤의 이름은 자동으로 지정됩니다. ex)mobizen_</td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_FILE_FULL_NAME}</td>
 * <td>String</td>
 * <td>저장될 파일 이름을 직접 지정합니다. ex)mobizen.mp4</td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_WATERMARK_TEST}</td>
 * <td>String</td>
 * <td><b>테스트 프로그램에서만 TRUE/FALSE 설정 한다.</b></td>
 * </tr>
 * </table>
 *
 * <p>
 * Video formats have the following keys:
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Value Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>{@link #KEY_WIDTH}</td>
 * <td>Integer</td>
 * <td>Video의 width를 지정합니다.</td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_HEIGHT}</td>
 * <td>Integer</td>
 * <td>Video의 height를 지정합니다.</td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_VIDEO_RATIO}</td>
 * <td>Float</td>
 * <td>Video의 비율을 결정 (90%(0.9f), 75%(0.75f), 50%(0.5f), 30%(0.3f))</td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_VIDEO_BIT_RATE}</td>
 * <td>Integer</td>
 * <td>Video의 bits/second를 설정</td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_IS_GPU}</td>
 * <td>Integer</td>
 * <td>이미지 변환 작업시 GPU 사용 여부 결정, <b>Only Android 4.3(Jelly bean API 18)</b></td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_WATERMARK}</td>
 * <td>boolean</td>
 * <td>Watermark 여부를 결정합니다.</b></td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_WATERMARK_IMAGE}</td>
 * <td>{@link WatermarkImage}</td>
 * <td>Watermark에 사용할 이미지 이름을 지정합니다.</b></td>
 * </tr>
 * </table>
 *
 * <p>
 * Audio formats have the following keys:
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Value Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>{@link #KEY_SAMPLE_RATE}</td>
 * <td>Integer</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_AUDIO_BIT_RATE}</td>
 * <td>Integer</td>
 * <td>Audio의 bits/second를 설정</td>
 * </tr>
 * <tr>
 * <td>{@link #KEY_IS_AUDIORECORD}</td>
 * <td>Boolean</td>
 * <td>Audio record 사용 유무를 설정</td>
 * </tr>
 * </table>
 */

@SuppressLint("UseValueOf")
public class RecordFormat {
    private Map<String, Object> mMap;

    /**
     * 파일의 전체 저장경로를 지정합니다. 지정 type은 String입니다.<br>
     * <b>Default</b> : /sdcard/Mobizen/
     */
    public static final String KEY_FILE_PATH = "file-path";

    /**
     * 파일의 저장경로를 지정합니다. 지정 type은 String입니다.
     */
    public static final String KEY_DIRECTORY_PATH = "directory-path";

    /**
     * 저장 파일의 시작 이름을 지정합니다. 파일 이름은 파일의 시작부분 이름이며, "file_" 식으로 지정하면 됩니다. 지정 type은 String입니다.<br>
     * <b>Default</b> : "mobizen_{@link RecordSet#FILE_DATE_TYPE}.mp4"
     */
    public static final String KEY_FILE_NAME = "file-name";

    /**
     * 파일의 이름을 직접 지정하는 경우 사용합니다. 해당 key값을 지정하지 않으면 아래 Default 값이 기본값으로 지정됩니다. Type은 String 입니다.<br>
     * <b>Default</b> : "mobizen_{@link RecordSet#FILE_DATE_TYPE}.mp4"
     */
    public static final String KEY_FILE_FULL_NAME = "file-full-name";

    /**
     * @deprecated Watermark를 테스트에 사용하며, Boolean Type으로 지정합니다. true : 사용, false : 미사용<br>
     * <b>Default</b> : true
     */
    public static final String KEY_WATERMARK = "Watermark";

    /**
     * Video의 width을 지정합니다. 단말기의 {@link Surface#ROTATION_0}와 {@link Surface#ROTATION_180}을 기준으로 항상 넓이여야 합니다. Type은 Integer 입니다.<br>
     * <b>Default</b> : 현재 화면의 1:2 사이즈가 지정된다.
     */
    public static final String KEY_WIDTH = "width";

    /**
     * Video의 height를 지정합니다. 단말기의 {@link Surface#ROTATION_0}와 {@link Surface#ROTATION_180}을 기준으로 항상 높이여야 합니다. Type은 Integer 입니다.<br>
     * <b>Default</b> : 현재 화면의 1:2 사이즈가 지정된다.
     */
    public static final String KEY_HEIGHT = "height";

    /**
     * 비디오 사이즈의 배율을 지정하며, {@link #KEY_WIDTH}와 {@link #KEY_HEIGHT}의 값이 0일 경우 배율리 지정합니다. Type은 String 입니다.<br>
     * <b>Default</b> : 현재 화면의 녹화가능 최대 사이즈가 지정된다.
     *
     * <p><b>지정 사이즈</b> 240 ~ 2160p 로 지정.
     */
    public static final String KEY_VIDEO_RATIO = "video-ratio";

    /**
     * @deprecated 비디오 사이즈의 배율을 직접 지정하며, {@link #KEY_WIDTH}와 {@link #KEY_HEIGHT}의 값이 0일 경우 {@link #KEY_VIDEO_RATIO} 값을 지정합니다. Type은 float 입니다.<br>
     * <b>Default</b> : 현재 화면의 50%(0.5f) 사이즈가 지정된다.
     */
    public static final String KEY_RATIO_CUSTOM = "ratio";

    /**
     * 비디오 품질을 지정합니다. 2 Mbps 정도가 적당합니다. Type은 Integer 입니다.<br>
     * <b>Default</b> : 0(자동으로 지정됩니다.)
     *
     * <p><b>참고 테이블</b>
     * <table>
     * <tr><td><b>비디오 품질</b></td><td><b>value</b></td></tr>
     * <tr><td><b>Auto</b></td><td>0</td></tr>
     * <tr><td><b>500 kbps</b></td><td>512000</td></tr>
     * <tr><td><b>1 Mbps</b></td><td>1048576</td></tr>
     * <tr><td><b>2 Mbps</b></td><td>2097152</td></tr>
     * <tr><td><b>3 Mbps</b></td><td>3145728</td></tr>
     * <tr><td><b>5 Mbps</b></td><td>5242880</td></tr>
     * <tr><td><b>10 Mbps</b></td><td>10485760</td></tr>
     * </table>
     */
    public static final String KEY_VIDEO_BIT_RATE = "video-bitrate";

    /**
     * 비디오 frame rate를 지정합니다. Type은 Integer 입니다.<br>
     *
     * <b>Default</b> : 0(자동으로 지정됩니다.)
     *
     * <p><b>참고 테이블</b>
     * <table>
     * <tr><td><b>FPS</b></td><td><b>value</b></td></tr>
     * <tr><td><b>Auto</b></td><td>0</td></tr>
     * <tr><td><b>5</b></td><td>5</td></tr>
     * <tr><td><b>10</b></td><td>10</td></tr>
     * <tr><td><b>15</b></td><td>15</td></tr>
     * <tr><td><b>20</b></td><td>20</td></tr>
     * <tr><td><b>No limits</b></td><td>-1</td></tr>
     * </table>
     */
    public static final String KEY_FRAME_RATE = "frame-rate";

    /**
     * GPU 사용 유무를 지정합니다. Android 버전에 따라서 처리가 다르며 4.2.2 이하는 자동으로 false 처리됩니다. Type은 Boolean입니다.<br>
     * <b>Default</b> : false
     *
     * <p>JellyBean 4.3 (API 18) 이상 isGPU 변수를 통해 CPU/GPU를 구분하여 사용이 가능합니다.(추후 CPU/GPU 사용 가능 여부가 변경될 수 있음)
     * JellyBean 4.2.2 (API 17) 이하는 CPU만 사용이 가능합니다.
     */
    public static final String KEY_IS_GPU = "Image-transformation";

    /**
     * 오디오 sample rate 지정합니다. Type은 Integer 입니다.<br>
     * <b>Default</b> : 8000(8 kHz)
     *
     * <p><b>참고 테이블</b>
     * <table>
     * <tr><td><b>samplerate</b></td><td><b>value</b></td></tr>
     * <tr><td><b>8 kHz</b></td><td>8000</td></tr>
     * </table>
     */
    public static final String KEY_SAMPLE_RATE = "sample-rate";

    /**
     * Audio 녹음 여부를 지정합니다. Type은 Boolean 입니다.<br>
     * <b>Default</b> : true
     */
    public static final String KEY_IS_AUDIORECORD = "is-audiorecord";

    /**
     * Watermark로 Watermark 사용 여부에 따라서 사용됩니다. 아래 사용법과 같이 셋팅해주면 되며, 폴더는 /assets/ 아래의 이미지를 사용하셔야 합니다.
     * <b>Defalut</b> : Watermark 이미지는 Mobizen으로 셋팅되어 있습니다.
     *
     * <p>isExceededCapacity is generally used like this:
     * <pre>
     * WatermarkImage watermark = new WatermarkImage();
     * watermark.WATERMARK_240 = "wm_mobizen_240.png";
     * watermark.WATERMARK_360 = "wm_mobizen_360.png";
     * watermark.WATERMARK_480 = "wm_mobizen_480.png";
     * watermark.WATERMARK_720 = "wm_mobizen_720.png";
     * watermark.WATERMARK_1080 = "wm_mobizen_1080.png";
     * watermark.WATERMARK_1440 = "wm_mobizen_1440.png";
     * watermark.WATERMARK_2160 = "wm_mobizen_2160.png";
     * format.setWatermarkImage(RecordFormat.KEY_WATERMARK_IMAGE, watermark);
     * </pre>
     */
    public static final String KEY_WATERMARK_IMAGE = "watermark-image";


    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecordFormat() {
        mMap = new HashMap();
    }

    /**
     * Returns the value of an integer key.
     */
    public final int getInteger(String name) {
        return ((Integer) mMap.get(name)).intValue();
    }

    /**
     * Returns the value of an integer key, or the default value if the
     * key is missing or is for another type value.
     *
     * @hide
     */
    public final int getInteger(String name, int defaultValue) {
        try {
            return getInteger(name);
        } catch (NullPointerException e) { /* no such field */ } catch (ClassCastException e) { /* field of different type */ }
        return defaultValue;
    }

    /**
     * Returns the value of a float key.
     */
    public final float getFloat(String name) {
        return ((Float) mMap.get(name)).floatValue();
    }

    /**
     * Returns the value of an integer key, or the default value if the
     * key is missing or is for another type value.
     *
     * @hide
     */
    public final float getFloat(String name, float defaultValue) {
        try {
            return getFloat(name);
        } catch (NullPointerException e) { /* no such field */ } catch (ClassCastException e) { /* field of different type */ }
        return defaultValue;
    }

    /**
     * Returns the value of an Boolean key.
     */
    public final boolean getBoolean(String name) {
        return ((Boolean) mMap.get(name));
    }

    /**
     * Returns the value of an integer key, or the default value if the
     * key is missing or is for another type value.
     *
     * @hide
     */
    public final boolean getBoolean(String name, boolean defaultValue) {
        try {
            return getBoolean(name);
        } catch (NullPointerException e) { /* no such field */ } catch (ClassCastException e) { /* field of different type */ }
        return defaultValue;
    }

    /**
     * Returns the value of a string key.
     */
    public final String getString(String name) {
        return ((String) mMap.get(name));
    }

    /**
     * Returns the value of an integer key, or the default value if the
     * key is missing or is for another type value.
     *
     * @hide
     */
    public final String getString(String name, String defaultValue) {
        String temp = getString(name);
        if (temp == null) {
            return defaultValue;
        }

        return temp;
    }

    /**
     * Watermark Image를 return 합니다.
     */
    public final WatermarkImage getWatermarkImage(String name) {
        return ((WatermarkImage) mMap.get(name));
    }

    /**
     * Returns the value of an integer key, or the default value if the
     * key is missing or is for another type value.
     *
     * @hide
     */
    public final WatermarkImage getWatermarkImage(String name, WatermarkImage defaultValue) {
        WatermarkImage temp = getWatermarkImage(name);
        if (temp == null) {
            return defaultValue;
        }

        return temp;
    }


    /**
     * Sets the value of an integer key.
     */
    public final void setInteger(String name, int value) {
        mMap.put(name, new Integer(value));
    }

    /**
     * Sets the value of a float key.
     */
    public final void setFloat(String name, float value) {
        mMap.put(name, new Float(value));
    }

    /**
     * Sets the value of a Boolean key.
     */
    public final void setBoolean(String name, boolean value) {
        mMap.put(name, value);
    }

    /**
     * Sets the value of a string key.
     */
    public final void setString(String name, String value) {
        mMap.put(name, value);
    }

    /**
     * Watermark 이미지 셋팅
     */
    public final void setWatermarkImage(String name, WatermarkImage watermark) {
        mMap.put(name, watermark);
    }
}
