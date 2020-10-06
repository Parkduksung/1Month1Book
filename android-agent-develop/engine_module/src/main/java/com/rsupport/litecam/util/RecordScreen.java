package com.rsupport.litecam.util;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.os.Build;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.litecam.ScreenInfo.ResolutionInfo;
import com.rsupport.litecam.record.RecordSet;

/**
 * Record 가능한 사이즈 정보를 한다.
 *
 * @author taehwan
 */
public class RecordScreen {
    private static int SCALE_WIDTH = 32;
    private static int SCALE_HEIGTH = 32;

    /**
     * Backcamara가 아니며, {@link RecordProfile}의 설정 값이 null 일 경
     */
    private static final int PROFILE_NULL = -1;

    /**
     * Backcamara가 아니며, {@link RecordProfile}의 설정 값 보다 작을 경우 에
     */
    private static final int PROFILE_SUCCESS = 1;

    /**
     * Backcamara가 아니며, {@link RecordProfile}의 설정 값 보다 작을 경우 fail
     */
    private static final int PROFILE_FAILE = 0;

    /**
     * BACK camera가 존재하며 profile의 사이즈 정보를 체크한다.
     *
     * @param size    현재 스크린의 사이즈를 가져온다.
     * @param profile 의 정보를 가져온다.
     * @return profile의 값이 -1일 경우 {@link #PROFILE_NULL}을 리턴,
     * profile의 값이 현재 screen 사이즈 보다 클 경우는 정상이며, {@link #PROFILE_SUCCESS} 리턴,
     * profile의 값이 현재 screen 사이즈 보다 작을 경우 {@link #PROFILE_FAILE}을 리턴한다.
     */
    private static int isBackcamaraRecordSizeCheck(Point size, SystemRecordProfile.RecordProfile profile) {
        if (profile.cameraFacing == -1 || profile.cameraFacing == CameraInfo.CAMERA_FACING_FRONT) {
            return PROFILE_NULL;
        }

        LLog.e("profile.width: " + profile.width + " profile.height: " + profile.height + " profile.camera: " + profile.cameraFacing);
        if (profile.cameraFacing == CameraInfo.CAMERA_FACING_BACK
                && ((profile.width >= size.x || profile.width >= size.y)
                && (profile.height >= size.x || profile.height >= size.y))) {
            return PROFILE_SUCCESS;
        }

        return PROFILE_FAILE;
    }


    private static boolean isOverScreenSize(Point size, SystemRecordProfile.RecordProfile profile) {
        if ((profile.width < size.x || profile.width < size.y)
                && (profile.height < size.x || profile.height < size.y)) {
            return true;
        }

        return false;
    }

    /**
     * 현재 해상도 기준 ratio를 체크한다.
     * minRatio는 현재 시스템의 해상도에서 {@link RecordSet#DEFAULT_VIDEO_MIN_RESOLUTION_TYPE}을 나눈 값을 사용한다.
     * maxRatio는 RecordProfile의 결과에 따라서 정해진다.
     *
     * @param minRatio
     * @param ratio
     * @return
     */
    public static boolean isRecordSizeCheck(float minRatio, float maxRatio, float ratio, boolean isOverScreenSize) {
        if (isOverScreenSize) {
            if (minRatio < ratio && ratio <= maxRatio) {
                return true;
            }

        } else {
            if (minRatio < ratio && ratio < maxRatio) {
                return true;
            }
        }

        return false;
    }

    /**
     * Record 가능한 사이즈를 {@link CamcorderProfile}를 이용하여 미리 체크한다. 동영상 녹화 가능 최대 사이즈는
     * {@link CamcorderProfile}의 {@link CamcorderProfile#QUALITY_HIGH}을 사용하여 체크한다.
     * 그 이상은 녹화가 되지 않는것으로 간주한다.
     *
     * @param context
     */
    public static ArrayList<String> getRecordScreenSizeOptionInfo(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        Point size = getRealSize(display, 1.0f, false);
        boolean isDualCoreSize = isDualCoreSize(size);

        SystemRecordProfile.RecordProfile profile = SystemRecordProfile.create().getCamcorderProfile();
        int isBackcamaraRecordSize = isBackcamaraRecordSizeCheck(size, profile);

        boolean isOverScreenSize = isOverScreenSize(size, profile);

        float minRatio = getRatio(display, size, RecordSet.DEFAULT_VIDEO_MIN_RESOLUTION_TYPE);
        float maxRatio = RecordSet.VIDEO_MAX_RATIO;
        if (isBackcamaraRecordSize == PROFILE_FAILE) {
            maxRatio = getRatio(display, size, profile.height);
        }

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE.length; ++i) {
            int resolutionType = RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[i];
            float ratio = getRatio(display, size, resolutionType);

            // BACK camera가 존재하며 Dual core 이상이면서 profile의 사이즈 보다 큰 경우
            if (!isDualCoreSize
                    && isBackcamaraRecordSize == PROFILE_FAILE
                    && isRecordSizeCheck(minRatio, maxRatio, ratio, isOverScreenSize)) {
                LLog.i("Camrecorder video size: " + resolutionType);
                list.add(resolutionType + "");

                // Back camera가 존재하며 Dual core 이상이면서 profile의 사이즈 보다 작을 경우
            } else if (!isDualCoreSize
                    && isBackcamaraRecordSize == PROFILE_SUCCESS
                    && isRecordSizeCheck(minRatio, maxRatio, ratio, isOverScreenSize)) {
                LLog.i("Camrecorder video size: " + resolutionType);
                list.add(resolutionType + "");

                // Back camera가 아닌 경우 처리
            } else if (!isDualCoreSize
                    && isBackcamaraRecordSize == PROFILE_NULL
                    && isRecordSizeCheck(minRatio, maxRatio, ratio, isOverScreenSize)) {
                LLog.i("video size: " + resolutionType);
                list.add(resolutionType + "");
            }

            // Dual core 조건에 맞는 경우
            if (isDualCoreSize
                    && ratio > RecordSet.VIDEO_DUAL_CORE_DEFAULT_RATIO && ratio < RecordSet.VIDEO_MAX_RATIO) {
                LLog.i("Video Size: " + resolutionType);
                list.add(resolutionType + "");
            }
        }

        if (list.size() <= 0) {
            list.add("" + RecordSet.VIDEO_DEFAULT_RATIO);
        }

        return list;
    }

    public static ArrayList<String> getRecordScreenSizes720P(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        Point size = getRealSize(display, 1.0f, false);
        boolean isDualCoreSize = isDualCoreSize(size);

        SystemRecordProfile.RecordProfile profile = SystemRecordProfile.create().getCamcorderProfile();
        int isBackcamaraRecordSize = isBackcamaraRecordSizeCheck(size, profile);

        boolean isOverScreenSize = isOverScreenSize(size, profile);

        float minRatio = getRatio(display, size, RecordSet.DEFAULT_VIDEO_MIN_RESOLUTION_TYPE);
        float maxRatio = RecordSet.VIDEO_MAX_RATIO;
        if (isBackcamaraRecordSize == PROFILE_FAILE) {
            maxRatio = getRatio(display, size, profile.height);
        }

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE.length; ++i) {
            int resolutionType = RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[i];

            float ratio = getRatio(display, size, resolutionType);

            // BACK camera가 존재하며 Dual core 이상이면서 profile의 사이즈 보다 큰 경우
            if (!isDualCoreSize
                    && isBackcamaraRecordSize == PROFILE_FAILE
                    && isRecordSizeCheck(minRatio, maxRatio, ratio, isOverScreenSize)) {
                LLog.i("Camrecorder video size: " + resolutionType);
                list.add(resolutionType + "");

                // Back camera가 존재하며 Dual core 이상이면서 profile의 사이즈 보다 작을 경우
            } else if (!isDualCoreSize
                    && isBackcamaraRecordSize == PROFILE_SUCCESS
                    && isRecordSizeCheck(minRatio, maxRatio, ratio, isOverScreenSize)) {
                LLog.i("Camrecorder video size: " + resolutionType);
                list.add(resolutionType + "");

                // Back camera가 아닌 경우 처리
            } else if (!isDualCoreSize
                    && isBackcamaraRecordSize == PROFILE_NULL
                    && isRecordSizeCheck(minRatio, maxRatio, ratio, isOverScreenSize)) {
                LLog.i("video size: " + resolutionType);
                list.add(resolutionType + "");
            }

            // Dual core 조건에 맞는 경우
            if (isDualCoreSize
                    && ratio > RecordSet.VIDEO_DUAL_CORE_DEFAULT_RATIO && ratio < RecordSet.VIDEO_MAX_RATIO) {
                LLog.i("Video Size: " + resolutionType);
                list.add(resolutionType + "");
            }
        }

        if (list.size() <= 0) {
            list.add("" + RecordSet.VIDEO_DEFAULT_RATIO);
        }

        return list;
    }

    public static int getDefalutRatioScreenName(Context context, String size) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point realSize = getRealSize(display, 1.0f, true);

        float ratio = Float.parseFloat(size);

        int screenSize = 0;
        if (realSize.x > realSize.y) {
            screenSize = (int) (realSize.y * ratio);

        } else {
            screenSize = (int) (realSize.x * ratio);
        }

        for (int i = 0; i < RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE.length; ++i) {
            if (RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[i] <= screenSize) {
                return RecordSet.DEFAULT_VIDEO_RESOLUTION_TYPE_SIZE[i];
            }
        }

        return 0;
    }

    private static float getRatio(Display display, Point size, float videoSize) {
        float ratio = 0.0f;

        LLog.i("Size.x: " + size.x + " size.y: " + size.y + " videoSize: " + videoSize);
        if (size.x > size.y) {
            ratio = (float) videoSize / (float) size.y;
        } else {
            ratio = (float) videoSize / (float) size.x;
        }

        return ratio;
    }

    /**
     * 배수가 맞는지 체크하는 변수로, unit의 배수에 따라서 정의된다.
     *
     * @param num
     * @param unit
     * @return
     */
    private static int align(int num, int unit) {
        return (num + unit - 1) & ~(unit - 1);
    }

    /**
     * Rotation에 따라서 Point 생성이 달라지게 되므로 아래와 같이 정의한다.
     *
     * @param size
     * @param rotation
     * @return
     */
    private static Point setPoint(Point size, int rotation) {
        Point temp;
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            temp = new Point(size.y, size.x);

        } else {
            temp = new Point(size.x, size.y);
        }
        return temp;
    }

    /**
     * 32 또는 64의 배수 처리
     *
     * @param temp
     */
    private static void setScaleSize(Point temp) {
        if (temp.x >= 1080 || temp.x >= 1920) {
            SCALE_WIDTH = 64;
        }

        if (temp.y >= 1080 || temp.y >= 1920) {
            SCALE_HEIGTH = 64;
        }
    }

    private static ResolutionInfo setResolution(Display display, Point size, int rotate) {
        ResolutionInfo encInfo = new ResolutionInfo();
        if (rotate < 0)
            encInfo.rotate = display.getRotation();
        else
            encInfo.rotate = rotate;

        Point temp = getRealSize(display, 1.0f, false);
        setScaleSize(temp);

        // 32 배수 처리.
        size.x = align(size.x, SCALE_WIDTH);
        size.y = align(size.y, SCALE_HEIGTH);

        LLog.d("1. screenSize.x : " + size.x + " screenSize.y : " + size.y + " temp.x " + temp.x + " temp.y " + temp.y);

        if (size.x > temp.x || size.y > temp.y) {
            encInfo.screenSize = setPoint(temp, encInfo.rotate);

        } else {
            encInfo.screenSize = setPoint(size, encInfo.rotate);
        }
        LLog.d("2. screenSize.x : " + encInfo.screenSize.x + " screenSize.y : " + encInfo.screenSize.y + " screenSize.x : " + size.x + " screenSize.y : " + size.y);

        encInfo.alignedScreenSize = setPoint(size, encInfo.rotate);
        encInfo.dstScreenSize = setPoint(size, encInfo.rotate);

        LLog.i(true, "Screen width: " + encInfo.screenSize.x + " height: " + encInfo.screenSize.y + ", Capture width: " + size.x + " height: " + size.y);
        return encInfo;
    }


    public static ResolutionInfo getResolutionEx(Context context, int ratio) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        Point size = new Point();
        display.getRealSize(size);

        int rotate = display.getRotation();

        if (rotate == 1 || rotate == 3) {
            int width = size.x;
            size.x = size.y;
            size.y = width;
        }

        size.x = (int) (size.x * ((float) ratio / 100));
        size.y = (int) (size.y * ((float) ratio / 100));

        size.x = align(size.x, SCALE_WIDTH);
        size.y = align(size.y, SCALE_HEIGTH);

        ResolutionInfo resolution = new ResolutionInfo();

        resolution.dstScreenSize = new Point(size.x, size.y);
        resolution.screenSize = new Point(size.x, size.y);
        resolution.alignedScreenSize = new Point(size.x, size.y);

        return resolution;
    }


    /**
     * 새로 추가한 resolution 처리로, String 값을 넘겨 받는다. 이경우 Preferece에 등록된 값을 기준으로 정리한다.
     *
     * @param context
     * @param ratioValue
     * @return
     */
    public static ResolutionInfo getResolution(Context context, String ratioValue) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        Point size = getRealSize(display, 1.0f, true);

        ratioValue = getRatioValue(context, ratioValue);
        float ratio = RecordSet.VIDEO_DEFAULT_RATIO;
        if (!ratioValue.equals("-1")) {
            ratio = getRatio(display, size, Float.parseFloat(ratioValue));
        }

        return setResolution(display, getRealSize(display, ratio, true), -1);
    }

    public static ResolutionInfo getResolution720P(Context context, String ratioValue) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        Point size = getRealSize(display, 1.0f, true);

        ratioValue = getRatioValue720P(context, ratioValue);
        float ratio = RecordSet.VIDEO_DEFAULT_RATIO;
        if (!ratioValue.equals("-1") && Float.parseFloat(ratioValue) >= 1.f) {
            ratio = getRatio(display, size, Float.parseFloat(ratioValue));
        }

        if (size.y == 480) {
            ratio = 1;
        }

        return setResolution(display, getRealSize(display, ratio, true), -1);
    }

    public static String getRatioValue(Context context, String ratioValue) {
        boolean isOK = false;
        ArrayList<String> list = getRecordScreenSizeOptionInfo(context);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(ratioValue)) {
                isOK = true;
                break;
            }
        }

        if (!isOK && list.size() > 0) {
            ratioValue = list.get(0);
            LLog.i(true, "change ratioValue : " + ratioValue);
            return ratioValue;

        } else if (isOK && list.size() > 0) {
            return ratioValue;
        }

        return "-1";
    }

    public static String getRatioValue720P(Context context, String ratioValue) {
        boolean isOK = false;
        ArrayList<String> list = getRecordScreenSizes720P(context);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(ratioValue)) {
                isOK = true;
                break;
            }
        }

        if (!isOK && list.size() > 0) {
            ratioValue = list.get(0);
            LLog.i(true, "change ratioValue : " + ratioValue);
            return ratioValue;

        } else if (isOK && list.size() > 0) {
            return ratioValue;
        }

        return "-1";
    }

    /**
     * 배율만 아는 경우에 사용하는 변수로 ratio에 따라서 계산을 자동으로 한다. 배율이 0.0f이면 기본값으로 {@link RecordSet#VIDEO_DEFAULT_RATIO}을 사용한다.
     *
     * @param context
     * @param ratio
     * @return
     */
    public static ResolutionInfo getResolution(Context context, float ratio) {
        if (ratio <= 0.0f)
            ratio = RecordSet.VIDEO_DEFAULT_RATIO;

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        Point size = getRealSize(display, ratio, true);

        return setResolution(display, size, -1);
    }

    /**
     * width, height 값이 있을 경우 사용되는 메소드로 width, height값이 정의되는 경우에 호출
     *
     * @param context
     * @param width
     * @param height
     * @return
     */
    public static ResolutionInfo getResolution(Context context, int width, int height, int rotate) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        Point size = getSizeCheck(display, new Point(width, height));
        return setResolution(display, size, rotate);
    }

    /**
     * width, height의 사이즈를 가져와서 {@link RecordSet#MAX_RATIO}와 {@link RecordSet#MIN_RATIO}을 체크하는 메소드.
     *
     * @param display
     * @param size
     * @return
     */
    private static Point getSizeCheck(Display display, Point size) {
        Point temp = new Point();

        temp = getRealSize(display, RecordSet.VIDEO_MAX_RATIO, true);
        if (size.x > temp.x || size.y > temp.y) {
            size.x = temp.x;
            size.y = temp.y;
        }

//		temp = getRealSize(display, RecordSet.VIDEO_MIN_RATIO, true);
//		if (size.x < temp.x || size.y < temp.y) {
//			size.x = temp.x;
//			size.y = temp.y;
//		}

        return size;
    }

    /**
     * 현재 단말기의 realSize를 구하고, ratio(배율)에 따라서  %를 설정하는 메소드이다.
     * 현재 화면의 사이즈는 {@link Display#getRealSize}를 통해서 가져온다. 배율의 경우 최대 {@link RecordSet#MAX_RATIO}와 최소 {@link RecordSet#MIN_RATIO}으로 정한다.
     *
     * @param display
     * @param ratio
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static Point getRealSize(Display display, float ratio, boolean isRatioCheck) {
        Point size = new Point();
        if (Build.VERSION.SDK_INT > 13) {
            try {
                display.getRealSize(size);

            } catch (NoSuchMethodError e) {
                display.getSize(size);
            }

        } else {
            size.x = display.getWidth();
            size.y = display.getHeight();
        }
        if (isRatioCheck) {
            /*
             * 배율이 0.9f 이상일 경우 0.9f로 고정
             *
             * 일부 기기에서 100% 사이즈를 인코딩하지 못하는 버그 발생
             */
            if (ratio >= RecordSet.VIDEO_MAX_RATIO) {
                ratio = RecordSet.VIDEO_MAX_RATIO;
            }

            /*
             * 사이즈가 1000을 넘지 않고, Dual core 일 경우 제한을 두기 위한 설정.
             * ratio 가 0.75f 이하일 경우에만 ratio를 0.75 로 고정시키도록 한다.
             *
             * 일부 기기에서 0.75% 아래의 사이즈를 인코딩 하지 못하는 버그 발생으로 인한 제한.
             */
            if (isDualCoreSize(size) && ratio < RecordSet.VIDEO_DUAL_CORE_DEFAULT_RATIO) {
                ratio = RecordSet.VIDEO_DUAL_CORE_DEFAULT_RATIO;
            }
        }

        /**
         * 계산된 배율에 따라 가로, 세로의 배율을 곱한다.
         */
        size.x *= ratio;
        size.y *= ratio;

        return size;
    }

    /**
     * CPU가 Dual core와 capture size가 가로, 세로 1000이 넘지 않을 경우 제한을 두기위한 메소드.
     *
     * @param size
     * @return
     */
    private static boolean isDualCoreSize(Point size) {
        if (APICheck.isDualCoreCheck() && (size.x < RecordSet.VIDEO_DUAL_CORE_AVAILABLE_SIZE && size.y < RecordSet.VIDEO_DUAL_CORE_AVAILABLE_SIZE)) {
            return true;
        }
        return false;
    }

    /**
     * 화면 Rotation을 정의하며 Muxer에서 사용.
     *
     * @param rotate
     * @return
     */
    public static int getDegress(int rotate) {
        switch (rotate) {
            case Surface.ROTATION_90:
                return 270;

            case Surface.ROTATION_180:
                return 180;

            case Surface.ROTATION_270:
                return 90;
        }
        return 0;
    }

    /**
     * OpenGL ES에서 사용하는 Watermark Rotation에 필요한 값 정의
     *
     * @param rotate
     * @return
     */
    public static int getDegressWatermark(int rotate) {
        switch (rotate) {
            case Surface.ROTATION_90:
                return 90;

            case Surface.ROTATION_180:
                return 180;

            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }
}
