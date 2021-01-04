package com.rsupport.util;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import android.os.Environment;

import com.rsupport.util.rslog.MLog;

public class FilePath {

    public static boolean mkdirs(String filePath){
        int index = filePath.lastIndexOf(File.separator);
        if(index <= 0){
            MLog.e("index error: " + index + ", filePath : " + filePath);
            return false;
        }
        String directory = filePath.substring(0, index);
        if("".equals(directory) == true){
            MLog.e("not directory name invalid: " + directory);
            return false;
        }

        File direFile = new File(directory);
        if(direFile.exists() == false){
            return direFile.mkdirs();
        }else{
            if(direFile.isDirectory() == false){
                MLog.e("not directory : " + filePath);
                return false;
            }
        }
        return direFile.exists();
    }


	public static String getScreenshotPath(boolean isPng) {
		StringBuilder sb = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());
		sb.append(File.separator);
		sb.append(Environment.DIRECTORY_PICTURES).append("/Screenshots/");
        
        File f = new File(sb.toString());
        if (!f.exists())
        	f.mkdirs();
        
        Calendar c = Calendar.getInstance();
        return sb.toString() + String.format(Locale.ENGLISH, "Screenshot_%04d-%02d-%02d-%02d-%02d-%02d" + (isPng ? ".png":".jpg"),
        		c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH),
        		c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
	}

}
