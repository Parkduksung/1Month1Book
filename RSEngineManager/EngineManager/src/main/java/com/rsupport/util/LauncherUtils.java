package com.rsupport.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.util.Log;

import com.rsupport.jarinput.shell.FinPacket;
import com.rsupport.jarinput.shell.IShellPacket;
import com.rsupport.jarinput.shell.ShellChannel;
import com.rsupport.jarinput.shell.ShellPacketHandler;
import com.rsupport.jarinput.shell.SyncPacket;
import com.rsupport.util.rslog.MLog;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

/**
 * Created by kwcho on 2/9/15.
 */
public class LauncherUtils {
    /** launcher 실행 중이 아니다 **/
    public static final int INVALID_LAUNCHER = -1;

    private static ILauncherUtils launcherUtils = new LauncherUtilForInject();

    /**
     * Liblauncher 의 PID 를 반환한다.
     * @param context
     * @return
     */
    public static int getLauncherPID(final Context context, final boolean isFinShellChannel){
        return launcherUtils.getLauncherPID(context, isFinShellChannel);
    }

    /**
     * Liblauncher 의 PID 를 반환한다.
     * @param context
     * @return
     */
    public static int getLauncherPID(Context context){
        return launcherUtils.getLauncherPID(context);
    };

    /**
     * launcher 이 살아 있는지 확인한다.
     * @param context
     * @return
     */
    public static boolean isAliveLauncher(Context context){
        return launcherUtils.isAliveLauncher(context);
    }

    /**
     * Launcher 를 실행한다.(rooting 폰일 경우에만 동작한다)
     * @param context
     * @return
     */
    public static boolean executeLauncher(Context context, boolean enableInput){
        return launcherUtils.executeLauncher(context, enableInput);
    }

    /**
     * launcher 를 종료 시킨다.
     * thread 에서 동작한다.
     * @param context
     */
    public static void killLauncher(Context context) {
        launcherUtils.killLauncher(context);
    }

    public static void init(ILauncherUtils launcherUtils){
        LauncherUtils.launcherUtils = launcherUtils;
    }

    public static boolean hasInjector(){
        return launcherUtils.hasInjector();
    }
}