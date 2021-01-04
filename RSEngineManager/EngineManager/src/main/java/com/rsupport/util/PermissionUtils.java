package com.rsupport.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.RemoteControl;

import com.rsupport.util.rslog.MLog;

import java.util.List;

/**
 * Created by kwcho on 5/16/15.
 */
public class PermissionUtils {

    /**
     * 소니 단말에 설치되어있는것이 아닌 다른 VNC 패키지가 있는 경우 걸러낸다.
     * Sony 에서 실제 사용되는 패키지 = "com.realvnc.android.remote"
     */
    private static final String[] EXCLUDE_REAL_VNC_PKAGE_NAMES = {
            // htc desire에서 PC제어관련 패키지 사용하는것으로 추정
            "com.realvnc.mirrorlinksample",
            "com.htc.mirrorlinkserver"
    };

    /**
     * MediaProjection 을 사용 가능한지를 판단한다.
     * 일반 단말에서는 5.0 이상이어도 MediaProjection 이 없을 수 있다.
     * @param context
     * @return 사용가능하면 true, 그렇지 않으면 false
     */
    public static boolean isAvailableMediaProjection(Context context){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            return false;
        }
        MediaProjectionManager projectionManager = (MediaProjectionManager)context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = projectionManager.createScreenCaptureIntent();

        if(intent == null){
            MLog.w("not found capture intent");
            return false;
        }

        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if(list == null || list.size() == 0){
            MLog.w("projection resolve list not found");
            return false;
        }
        return true;
    }


    public static boolean isAvailableSonyRemoteService(Context context){
        if(RemoteControl.serviceAvailable(context) == true){
            Intent intent = new Intent("com.android.remote.BIND");
            PackageManager pm = context.getPackageManager();
            ResolveInfo serviceInfo = pm.resolveService(intent, 0);

            if(serviceInfo.serviceInfo == null) {
                MLog.e("isRealVncPkage serviceInfo null");
                return false;
            }

            for(int i = 0; i < EXCLUDE_REAL_VNC_PKAGE_NAMES.length; i++){
                if(EXCLUDE_REAL_VNC_PKAGE_NAMES[i].equals(serviceInfo.serviceInfo.packageName) == true){
                    MLog.e("exclude package : " + EXCLUDE_REAL_VNC_PKAGE_NAMES[i]);
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isSupportInject(Context context, String targetPackageName){
        if(targetPackageName == null){
            return false;
        }
        //Manifest.permission.INJECT_EVENTS
        return hasPermission(context, targetPackageName, "android.permission.INJECT_EVENTS");
    }

    public static boolean isSupportVirtualDisplay(Context context, String targetPackageName){
        if(targetPackageName == null){
            return false;
        }
        boolean hasVirtualDisplay = hasPermission(context, targetPackageName, "android.permission.CAPTURE_VIDEO_OUTPUT");
        boolean hasSecureVirtualDisplay = hasPermission(context, targetPackageName, "android.permission.CAPTURE_SECURE_VIDEO_OUTPUT");
        boolean supportVirtualDisplay = (hasVirtualDisplay && hasSecureVirtualDisplay) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // virtual display권한은 있지만 지원하지 않는 오류에대한 예외 처리.(LGE rsperm aidl index 이슈)
        if(supportVirtualDisplay == true && isNotSupportVirtualDisplayRSPerm(context, targetPackageName) == true){
            MLog.e("NotSupportVirtualDisplayRSPerm : " + targetPackageName);
            return false;
        }

        MLog.d("isSupportVirtualDisplay : " + hasVirtualDisplay + " , " + hasSecureVirtualDisplay + " , " + supportVirtualDisplay);
        return supportVirtualDisplay;
    }

    public static boolean isSupportReadFrameBuffer(Context context, String targetPackageName){
        // 7.0 이상은 ReadFrameBuffer 를 지원 하지 않는다.
        if(targetPackageName == null || Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            return false;
        }
        boolean hasReadFrameBuffer = hasPermission(context, targetPackageName, "android.permission.READ_FRAME_BUFFER");
        MLog.d("isSupportReadFrameBuffer : " + hasReadFrameBuffer);
        return hasReadFrameBuffer;
    }

    private static boolean hasPermission(Context context, String rspermPkgName, String permission){
        try {
            String[] permissionInfo = context.getPackageManager().getPackageInfo(rspermPkgName,
                    PackageManager.GET_META_DATA|PackageManager.GET_PERMISSIONS).
                    requestedPermissions;
            if(permissionInfo != null){
                for(String permInfo : permissionInfo){
                    if(permission.equals(permInfo)){
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static boolean isNotSupportVirtualDisplayRSPerm(Context context, String targetPackageName){
        try {
            final int LGE_SIGNATURE = -1160602166;
            boolean isLGESignature = false;
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(targetPackageName, PackageManager.GET_SIGNATURES);
            if(packageInfo == null){
                return true;
            }
            Signature[] signatures = packageInfo.signatures;
            for(int i = 0; i < signatures.length; i++){
                if(signatures[i].hashCode() == LGE_SIGNATURE){
                    isLGESignature = true;
                    break;
                }
            }
            if(isLGESignature == true && targetPackageName.equals("com.rsupport.rsperm") && packageInfo.versionCode == 318){
                MLog.e("NotSupportVirtualDisplayRSPerm 318 : " + targetPackageName);
                return true;
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    public static boolean hasRemoteSubmixPermission(Context context, String targetPackageName){
        boolean hasVirtualDisplay = hasPermission(context, targetPackageName, Manifest.permission.CAPTURE_AUDIO_OUTPUT);
        if(hasVirtualDisplay == false){
            return false;
        }

        PackageManager pm = context.getPackageManager();

        int checkSignatureResult = pm.checkSignatures("com.android.settings", targetPackageName);

        if(checkSignatureResult != PackageManager.SIGNATURE_MATCH){
            MLog.i("checkSignatureResult : " + checkSignatureResult);
            return false;
        }
        return hasVirtualDisplay;
    }
}
