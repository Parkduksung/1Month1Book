package com.rsupport.util;

import android.content.Context;
import android.util.Log;

import com.rsupport.util.rslog.MLog;


public class Plugin {

//    private static boolean isSoLoaded = false;
//    static boolean isLoaded() {
//        return isSoLoaded;
//    }

    public static final int LOAD_MAINMODULE = 1;
    public static final int LOAD_SUBMODULE = 2;
    static public int loadLibrary(String soPath) {
        //log.v("load :" + soPath);

        // for linked module loading(loading submodule).
        int submodule = soPath.lastIndexOf(":s");
        if (submodule>0) {
            soPath = soPath.substring(0, submodule);
        }

        if (soPath == null) // || !new File(soPath).exists())
            return -1;

        String msg = null;
        try {
            System.load(soPath);
            return (submodule>0) ? LOAD_SUBMODULE : LOAD_MAINMODULE; // sub-module loading ok.

        } catch (UnsatisfiedLinkError e) {
            try {
                //int slash = soPath.lastIndexOf("/");
                //if (slash>0) soPath = soPath.substring(slash+1);
                System.loadLibrary(soPath); //load so from system path
                return (submodule>0) ? LOAD_SUBMODULE : LOAD_MAINMODULE; // sub-module loading ok.

            } catch (Exception e1) {
                msg = e.toString() + ",";
                msg += e1.toString();
                MLog.e("ex: " + msg);
            }
        } catch (Exception e) {
            msg = e.toString();
            MLog.e("ex: "+msg);
        }

        // ---------------------------- defeat dex2jar
        Context context = RsupApplication.context;
        String szPackage = null;
        try {
            szPackage = context.getPackageName();
            if (szPackage == null)
                throw new android.util.AndroidException();
        } catch (android.util.AndroidException e) {
        } catch (Exception e) {
            Log.e("", szPackage + ", " + e.toString());
        }

        try {
            szPackage = context.getPackageName();
            if (szPackage == null)
                throw new android.util.AndroidException();
        } catch (android.util.AndroidException e) {
            Log.e("", szPackage + ", " + e.toString());
        }
        // ---------------------------- defeat dex2jar
        throw new RuntimeException(msg);
    }
}
