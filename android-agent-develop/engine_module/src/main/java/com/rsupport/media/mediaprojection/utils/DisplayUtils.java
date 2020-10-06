package com.rsupport.media.mediaprojection.utils;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;

import androidx.annotation.NonNull;

import android.view.Display;
import android.view.Surface;

import com.rsupport.util.FixedSize;
import com.rsupport.util.log.RLog;

import config.EngineConfigSetting;
import kotlin.Pair;

/**
 * Created by taehwan on 5/13/15.
 * <p/>
 * 스크린 사이즈를 처리한다.
 */
public class DisplayUtils {

    /**
     * VirtualDisplay의 사이즈를 정의한다.
     */
    public static Point detectVirtualDisplaySize(Display display, int maxResolution) {
        Point screenSize = getScreenSize(display);

        int widthPixels = screenSize.x;
        int heightPixels = screenSize.y;

        if (isFullSizeScreen(display)) {
            return new Point(screenSize.x / 2, screenSize.y / 2);
        }

        if ((widthPixels > 1920 && heightPixels > 1080) || (widthPixels > 1080 && heightPixels > 1920)) {
            maxResolution = 720;
            // 겔럭시 폴드 720 가로 모드에서 인코딩 안됨
            if (Build.MODEL.contains("SM-F907")) {
                maxResolution = 480;
            }
        }

        if (EngineConfigSetting.isZidoo()) {
            maxResolution = 1000;
        }

        if (EngineConfigSetting.isHCIBuild()) {
            maxResolution = 1000;
        }

        if (isHorizontalDevice(display, widthPixels, heightPixels)) {
            maxResolution = 1000;
        }

        RLog.i("before widthPixels.%d, heightPixels.%d, resolution.%d", widthPixels, heightPixels, maxResolution);
        int minPixels = Math.min(widthPixels, heightPixels);
        // 작은 사이즈가 maxResolution 보다 크면 제한 한다.
        if (minPixels > maxResolution) {
            if (isHorizontalDevice(display, widthPixels, heightPixels)) {
                if (display.getRotation() == Surface.ROTATION_90 || display.getRotation() == Surface.ROTATION_270) {
                    float ratio = (float) heightPixels / (float) widthPixels;
                    widthPixels = maxResolution;
                    heightPixels = (int) (maxResolution * ratio);
                } else {
                    float ratio = (float) widthPixels / (float) heightPixels;
                    heightPixels = maxResolution;
                    widthPixels = (int) (maxResolution * ratio);
                }
            } else {
                if (display.getRotation() == Surface.ROTATION_90 || display.getRotation() == Surface.ROTATION_270) {
                    float ratio = (float) widthPixels / (float) heightPixels;
                    heightPixels = maxResolution;
                    widthPixels = (int) (maxResolution * ratio);

                } else {
                    float ratio = (float) heightPixels / (float) widthPixels;
                    widthPixels = maxResolution;
                    heightPixels = (int) (maxResolution * ratio);
                }
            }
        }

        RLog.i("middle widthPixels.%d, heightPixels.%d", widthPixels, heightPixels);

        widthPixels = align(widthPixels, 16);
        heightPixels = align(heightPixels, 16);

        RLog.i("after widthPixels.%d, heightPixels.%d", widthPixels, heightPixels);

        return new Point(widthPixels, heightPixels);
    }

    public static boolean isHorizontalDevice(Display display, int width, int height) {
        if ((display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180) && width > height ||
                (display.getRotation() == Surface.ROTATION_90 || display.getRotation() == Surface.ROTATION_270) && height > width) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 삼성 knox 10인치 탭 1280 800 단말 터치 이슈. 풀사이즈로 캡쳐 하도록 변경
     *
     * @return
     */
    @TargetApi(19)
    public static boolean isFullSizeScreen(Display display) {
        if (EngineConfigSetting.isKnox) {
            Point size = getScreenSize(display);
            int rotation = display.getRotation();

            switch (rotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (size.x == 1280 && size.y == 800) {
                        return true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (size.x == 800 && size.y == 1280) {
                        return true;
                    }
                    break;
                default:
                    return false;
            }

        }
        return false;
    }

    /**
     * 64의 배수 처리
     */
    private static int align(int num, int unit) {
        return (num + unit - 1) & ~(unit - 1);
    }

    /**
     * 현재 Display의 ScreenSize를 return 한다.
     */
    @TargetApi(17)
    public static Point getScreenSize(@NonNull Display display) {
        Point size = new Point();
        try {
            display.getRealSize(size);
        } catch (NoSuchMethodError e) {
            if (Build.VERSION.SDK_INT > 13) {
                display.getSize(size);
            } else {
                size.x = display.getWidth();
                size.y = display.getHeight();

            }
        }

        return size;
    }

    public static int getMinDisplaySize(@NonNull Display display) {
        Point displayPoint = getScreenSize(display);
        return Math.min(displayPoint.x, displayPoint.y);
    }

    public static float getDisplayRatio(@NonNull Display display) {
        Point displayPoint = getScreenSize(display);
        float max = Math.max(displayPoint.x, displayPoint.y);
        float min = Math.min(displayPoint.x, displayPoint.y);
        return max / min;
    }

    public static Point getKnoxMaxDisplay(@NonNull Display display, Point source) {
        Pair<Integer, Integer> fixedSize = new FixedSize(
                DisplayUtils.getMinDisplaySize(display),
                DisplayUtils.getDisplayRatio(display)
        ).calculate(source.x, source.y);
        return new Point(fixedSize.getFirst(), fixedSize.getSecond());
    }

    /**
     * {@link Surface#ROTATION_0}, {@link Surface#ROTATION_90}, {@link Surface#ROTATION_180}, {@link Surface#ROTATION_270}
     * 을 Degree 로 변관한다.
     */
    public static int convertSurfaceRotationToDegree(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_90:
                return 90;

            case Surface.ROTATION_270:
                return 270;

            case Surface.ROTATION_180:
                return 180;
            default:
                return 0;
        }
    }
}
