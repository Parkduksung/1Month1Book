package com.rsupport.media.mediaprojection;


import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.view.Surface;

import com.rsupport.util.LockObject;
import com.rsupport.util.log.RLog;

/**
 * Created by taehwan on 5/13/15.
 * <p/>
 * MediaProjection 권한을 사용하기 위한 클래스.
 */
@TargetApi(21)
public class ProjectionPermission {

    public static final String ACTION_BIND_RESULT = "com.rsupport.rvagent.action.BIND_RESULT";
    public static final String EXTRA_KEY_BIND_RESULT = "extra_key_bind_result";

    public static final int BIND_RESULT_SUCCESS = 0x00000001;
    public static final int BIND_RESULT_FAIL = 0x00000002;

    private LockObject lockObject;

    private int bindResult;
    private boolean isRegisterBindReceiver = false;

    private Context context;

    public ProjectionPermission(Context context) {
        lockObject = new LockObject();
        this.context = context;
    }

    public void onDestory() {
        lockObject.notifyLock();
        if (ProjectionActivity.mediaProjection != null) {
            ProjectionActivity.mediaProjection.stop();
            ProjectionActivity.mediaProjection = null;
        }
    }

    public IScreenCapturable createScreenCapturable() {
        return new VirtualDisplayCapturable();
    }

    public boolean bind() {
        registerBindReceiver();
        Intent intent = new Intent(context, ProjectionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        try {
            pendingIntent.send();
        } catch (Exception e) {
            RLog.e(e);
            unRegisterBindReceiver();
            return false;
        }
        lockObject.lock();

        unRegisterBindReceiver();
        return (bindResult == BIND_RESULT_SUCCESS);
    }

    private synchronized void registerBindReceiver() {
        isRegisterBindReceiver = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BIND_RESULT);
        filter.addCategory(context.getPackageName());
        context.registerReceiver(bindNotifyReceiver, filter);
    }

    private synchronized void unRegisterBindReceiver() {
        if (isRegisterBindReceiver) {
            isRegisterBindReceiver = false;
            context.unregisterReceiver(bindNotifyReceiver);
        }
    }

    public void unbind() {
        unRegisterBindReceiver();
    }

    public boolean isBound() {
        return (bindResult == BIND_RESULT_SUCCESS);
    }

    private BroadcastReceiver bindNotifyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_BIND_RESULT.equals(intent.getAction())) {
                bindResult = intent.getIntExtra(EXTRA_KEY_BIND_RESULT, BIND_RESULT_FAIL);
                if (lockObject != null) {
                    lockObject.notifyLock();
                }
            }
        }
    };

    class VirtualDisplayImpl implements IVirtualDisplay {
        VirtualDisplay virtualDisplay;

        @Override
        public boolean createVirtualDisplay(String name, int width, int height, int dpi, Surface surface, int flags) {
            try {
                virtualDisplay = ProjectionActivity.mediaProjection.createVirtualDisplay(
                        name,
                        width,
                        height,
                        dpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        surface,
                        null,
                        null
                );
            } catch (Exception e) {
                RLog.e(e);
                return false;
            }
            RLog.i("createVirtualDisplay success");
            return true;
        }

        @Override
        public boolean release() {
            try {
                if (virtualDisplay != null) {
                    virtualDisplay.release();
                }
            } catch (Exception e) {
                RLog.e(e);
            }
            return false;
        }
    }

    class VirtualDisplayCapturable implements IScreenCapturable {
        private IVirtualDisplay virtualDisplayImpl;

        @Override
        public Object initialized() throws Exception {
            RLog.i("MediaProjection initialized");
            if (virtualDisplayImpl == null) {
                virtualDisplayImpl = new VirtualDisplayImpl();
            }
            return virtualDisplayImpl;
        }

        @Override
        public void close() {
            RLog.i("MediaProjection close");
            if (virtualDisplayImpl != null) {
                virtualDisplayImpl.release();
                virtualDisplayImpl = null;
            }
        }
    }
}
