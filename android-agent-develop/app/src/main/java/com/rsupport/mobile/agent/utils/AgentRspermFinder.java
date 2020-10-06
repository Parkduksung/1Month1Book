package com.rsupport.mobile.agent.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.rsupport.android.engine.install.finder.AbstractEngineFinder;
import com.rsupport.android.engine.install.finder.Parameter;
import com.rsupport.common.jni.ETC;
import com.rsupport.util.log.RLog;

import java.lang.reflect.Method;
import java.util.Locale;


public class AgentRspermFinder extends AbstractEngineFinder {

    private String DEFAULT_URL = "http://mous.rsupport.com/api/rest/mobile/searchUpdate";

    private final int OS_ID = 1;
    private int productID = 0;
    private String url = DEFAULT_URL;

    public AgentRspermFinder(Context context) {
        super(context);
    }

    public void setServerURL(String url) {
        this.url = url;
    }

    public void setProductId(int productId) {
        this.productID = productId;
    }

    @Override
    public Parameter[] getRequestParams() {
        RLog.d("AgentRsperm productID : " + productID);

        Parameter[] params = new Parameter[9];
        params[0] = new Parameter("productId", String.valueOf(productID));
        params[1] = new Parameter("modelName", Build.MODEL);
        params[2] = new Parameter("osId", String.valueOf(OS_ID));
        params[3] = new Parameter("osVersion", Build.VERSION.RELEASE);
        params[4] = new Parameter("signature", String.valueOf(signature[0]));
        params[5] = new Parameter("manufacturer", Build.MANUFACTURER);
        params[6] = new Parameter("marketEnabled", getGooglePlayState());
        params[7] = new Parameter("neon", getSupportNeonCPU());
        params[8] = new Parameter("resolution", getResolution());
        return params;
    }

    @Override
    public String getServerURL() {
        RLog.d("AgentRsperm getServerURL : " + this.url);
        return this.url;
    }

    @Override
    public void setChangeServerURL(String s) {

    }


    protected String getResolution() {
        int width = 0;
        int height = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        try {
            Method rawWidthMethod = Display.class.getMethod("getRawWidth");
            Method rawHeightMethod = Display.class.getMethod("getRawHeight");
            width = (Integer) rawWidthMethod.invoke(display);
            height = (Integer) rawHeightMethod.invoke(display);
        } catch (Exception e) {
            DisplayMetrics dispInfo = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >= 17) {
                try {
                    Method getMetrics = display.getClass().getMethod("getRealMetrics", android.util.DisplayMetrics.class);
                    getMetrics.invoke(display, dispInfo);
                } catch (Exception e1) {
                    windowManager.getDefaultDisplay().getMetrics(dispInfo);
                }
            } else {
                windowManager.getDefaultDisplay().getMetrics(dispInfo);
            }
            width = dispInfo.widthPixels;
            height = dispInfo.heightPixels;
        }
        return String.format(Locale.KOREA, "%dx%d", width, height);
    }

    protected String getGooglePlayState() {
        int result = context.getPackageManager().checkSignatures(context.getPackageName(), "com.android.vending");
        if (result == PackageManager.SIGNATURE_UNKNOWN_PACKAGE) {
            return "0";
        }
        return "1";
    }

    protected String getSupportNeonCPU() {
        return new ETC().isNeonCPU() == ETC.CPU_NEON ? "1" : "0";
    }

}