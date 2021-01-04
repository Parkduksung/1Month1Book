package com.rsupport.utils;

import android.os.Build;
import android.os.Environment;
import android.test.AndroidTestCase;

import com.rsupport.util.FilePath;
import com.rsupport.util.LauncherUtils;
import com.rsupport.util.Utils;
import com.rsupport.util.rslog.MLog;

/**
 * Created by kwcho on 2/9/15.
 */
public class LauncherUtilsTestcase extends AndroidTestCase{
    public void testGetLauncherPID(){
        int launcherPID = LauncherUtils.getLauncherPID(getContext());
        MLog.i("launcherPID.%d", launcherPID);
    }

    public void testIsAliveLauncher(){
        boolean isAlive = LauncherUtils.isAliveLauncher(getContext());
        MLog.i("isAlive.%b", isAlive);
    }

    public void testExecuteLauncher(){
        boolean executeResult = LauncherUtils.executeLauncher(getContext(), true);
        MLog.i("executeResult .%b", executeResult );
    }

    public void testFileMkdirs(){

        String directoryname = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mobizen/tmp";
        String filename = "tmp_broadcast_thumbnail.jpg";
        String thumbnailFile = directoryname + "/" + filename;

        MLog.e("thumbnailFile : " + thumbnailFile );

        if(FilePath.mkdirs(thumbnailFile) == false){
            MLog.e("false!!!");
        }
    }

}
