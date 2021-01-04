package com.rsupport.rsperm.projection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import com.rsupport.rsperm.ProjectionPermission;
import com.rsupport.util.rslog.MLog;

/**<pre>*******************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 ********************************************************************************</pre>
 *
 * <b>Copyright (c) 2012 RSUPPORT Co., Ltd. All Rights Reserved.</b><p>
 *
 * <b>NOTICE</b> :  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.<p>
 *
 * FileName: ProjectionActivity.java<br>
 * Author  : kwcho<br>
 * Date    : Nov 20, 20147:13:13 PM<br>
 * Purpose : <p>
 *
 * [History]<p>
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ProjectionActivity extends Activity{
	private int PROJECTION_CODE = 100;
	
	private MediaProjectionManager projectionManager = null;

	private boolean sendResult = false;
	private boolean isUserLeave = false;
	private PowerManager powerManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setFinishOnTouchOutside(false);
		super.onCreate(savedInstanceState);
		projectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		startActivityForResult(projectionManager.createScreenCaptureIntent(), PROJECTION_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		sendResult = true;

		if (requestCode != PROJECTION_CODE) {
			MLog.e("Unknown request code: " + requestCode);
			sendBroadcast(ProjectionPermission.BIND_RESULT_FAIL, new Intent());
			finish();
			return;
		}
		if (resultCode != RESULT_OK) {
			MLog.e("User denied screen sharing permission");
			sendBroadcast(ProjectionPermission.BIND_RESULT_FAIL, new Intent());
			finish();
			return;
		}
		MLog.v("onActivityResult : " + resultCode);
		sendBroadcast(ProjectionPermission.BIND_RESULT_SUCCESS, data);
		finish();
	}
	
	private void sendBroadcast(int bindResultSuccess, Intent intent) {
		intent.setAction(ProjectionPermission.ACTION_BIND_RESULT);
		intent.addCategory(getPackageName());
		intent.putExtra(ProjectionPermission.EXTRA_KEY_BIND_RESULT, bindResultSuccess);
		sendBroadcast(intent);
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		isUserLeave = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(isUserLeave){ // 잠금화면등의 요인으로 인한 Stop호출인지 확인
			if(checkScreenOn() == true){
				if(sendResult == false){
					MLog.e("Called RecentTask");
					sendBroadcast(ProjectionPermission.BIND_RESULT_FAIL, new Intent());
				}
				finish();
			}
		} else {
			if(sendResult == false){
				MLog.i("Wait onActivityResult");
			}
		}
	}

	private boolean checkScreenOn(){
		return powerManager.isInteractive();
	}
}
