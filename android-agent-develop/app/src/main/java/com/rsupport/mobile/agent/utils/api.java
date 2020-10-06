package com.rsupport.mobile.agent.utils;


import java.io.File;
import java.io.OutputStream;


public class api {
    public static boolean isFileExist(String file) {
        try {
            return (new File(file)).exists();
        } catch (Exception e) {
        }
        return false;
    }

    ;
}
