package com.rsupport.rsperm;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.rsupport.srn30.screen.PermissionLoader;
import com.rsupport.srn30.screen.capture.IScreenCaptureable;
import com.rsupport.srn30.screen.encoder.ScapOption;
import com.rsupport.util.rslog.MLog;

import java.io.IOException;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RSPermissionForProjection extends RSPermission{

    private ProjectionPermission projectionPermission = null;

    @Override
    public synchronized boolean bind(String packageName) {
        if(super.bind(packageName) == true){
            IEnginePermission enginePermission = PermissionLoader.createEnginePermission(getContext(),
                    "", PermissionLoader.PRIORITY_PROJECTION|PermissionLoader.FLAG_PRIORITY_PERM_ONLY);
            if(enginePermission != null){
                projectionPermission = (ProjectionPermission)enginePermission;
                return true;
            }
        }
        return false;
    }

    @Override
    public int getType() {
        if(projectionPermission != null){
            return projectionPermission.getType();
        }
        return IEnginePermission.BIND_ERROR_NOT_FOUND;
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        if(projectionPermission != null){
            PermissionLoader.release(projectionPermission);
            projectionPermission = null;
        }
    }

    @Override
    public synchronized boolean isBound() {
        if(super.isBound() == true){
            if(projectionPermission != null){
                return projectionPermission.isBound();
            }
        }
        return false;
    }

    @Override
    protected boolean waitForBind(int timeout) {
        long startTime = System.currentTimeMillis();
        while(super.isBound() == false){
            if(System.currentTimeMillis() - startTime > timeout){
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    MLog.e(Log.getStackTraceString(e));
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized void unbind() {
        super.unbind();
        if(projectionPermission != null){
            projectionPermission.unbind();
        }
    }

    @Override
    public int hwRotation() throws Exception {
        if(projectionPermission != null){
            return projectionPermission.hwRotation();
        }
        return 0;
    }

    @Override
    public int[] getSupportCaptureType() {
        if(projectionPermission != null){
            return projectionPermission.getSupportCaptureType();
        }
        return super.getSupportCaptureType();
    }

    @Override
    public int[] getSupportEncoder() {
        if(projectionPermission != null){
            return projectionPermission.getSupportEncoder();
        }
        return super.getSupportEncoder();
    }

    @Override
    public boolean screenshot(String imgPath) throws IOException {
        if(projectionPermission != null){
            return projectionPermission.screenshot(imgPath);
        }
        return super.screenshot(imgPath);
    }

    @Override
    public boolean setMaxLayer(int type) throws IOException {
        // projection is not support
        return false;
    }

    @Override
    public IScreenCaptureable createScreenCaptureable(ScapOption scapOption) {
        if(projectionPermission != null){
            return projectionPermission.createScreenCaptureable(scapOption);
        }
        return super.createScreenCaptureable(scapOption);
    }

    @Override
    public int getCurrentCaptureType() {
        if(projectionPermission != null){
            return projectionPermission.getCurrentCaptureType();
        }
        return super.getCurrentCaptureType();
    }
}
