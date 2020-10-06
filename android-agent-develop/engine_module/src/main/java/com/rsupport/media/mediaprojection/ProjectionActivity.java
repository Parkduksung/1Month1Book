package com.rsupport.media.mediaprojection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import com.rsupport.util.log.RLog;


/**
 * Created by taehwan on 5/13/15.
 * <p>
 * MediaProjection 권한 설정
 */
@TargetApi(21)
public class ProjectionActivity extends Activity {

    private final int PROJECTION_CODE = 1000;
    private MediaProjectionManager projectionManager;
    protected static MediaProjection mediaProjection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = projectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, PROJECTION_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PROJECTION_CODE) {
            RLog.e("Unknown request code : " + requestCode);
            sendBroadcast(ProjectionPermission.BIND_RESULT_FAIL);
            finish();
            return;
        }

        if (resultCode != RESULT_OK) {
            RLog.e("User denied screen sharing permission");
            sendBroadcast(ProjectionPermission.BIND_RESULT_FAIL);
            finish();
            return;
        }

        RLog.i("resultCode " + requestCode);
        mediaProjection = projectionManager.getMediaProjection(resultCode, data);
        sendBroadcast(ProjectionPermission.BIND_RESULT_SUCCESS);
        finish();
    }

    private void sendBroadcast(int bindResultSuccess) {
        Intent intent = new Intent();
        intent.setAction(ProjectionPermission.ACTION_BIND_RESULT);
        intent.addCategory(getPackageName());
        intent.putExtra(ProjectionPermission.EXTRA_KEY_BIND_RESULT, bindResultSuccess);
        sendBroadcast(intent);
    }
}
