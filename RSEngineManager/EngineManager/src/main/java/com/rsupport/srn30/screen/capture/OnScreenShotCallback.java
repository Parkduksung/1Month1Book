package com.rsupport.srn30.screen.capture;

public interface OnScreenShotCallback {
	public static final String SCREEN_SHOT_STATUS_START = "screen_shot_status_start";
	public static final String SCREEN_SHOT_STATUS_READY = "screen_shot_status_ready";
	public static final String SCREEN_SHOT_STATUS_ERROR = "screen_shot_status_error";
	public void onStart(String imgPath);
	public void onReady();
	public void onComplete(String imgPath);
	public void onError(String imgPath);
}
