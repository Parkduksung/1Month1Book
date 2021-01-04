package com.rsupport.util;

import android.content.Context;

/**
 * Created by kwcho on 3/23/16.
 */
public interface ILauncherUtils {

    /**
     * Liblauncher 의 PID 를 반환한다.
     * @param context
     * @return
     */
    public int getLauncherPID(Context context, final boolean isFinShellChannel);

    /**
     * Liblauncher 의 PID 를 반환한다.
     * @param context
     * @return
     */
    public int getLauncherPID(Context context);

    /**
     * launcher 이 살아 있는지 확인한다.
     * @param context
     * @return
     */
    public boolean isAliveLauncher(Context context);

    /**
     * Launcher 를 실행한다.(rooting 폰일 경우에만 동작한다)
     * @param context
     * @return
     */
    public boolean executeLauncher(Context context, boolean enableInput);

    /**
     * launcher 를 종료 시킨다.
     * thread 에서 동작한다.
     * @param context
     */
    public void killLauncher(Context context);

    /**
     * Injection 기능이 있는지 반환한다
     * @return 있으면 true, 그렇지 않으면 false
     */
    public boolean hasInjector();
}