package com.rsupport.srn30.screen.capture;



public interface IScreenCaptureable {
	public static final int STATE_CONTINUE = 0;
	public static final int STATE_NEXT = 1;
	public static final int STATE_BREAK = 2;
	
	public Object initialized() throws Exception;
	public int prepareCapture() throws Exception;
	public boolean capture() throws Exception;
	public boolean postCapture() throws Exception;
	public void close();
	public boolean isAlive();
	
	/**
	 * 화면 회전시 Encoder 재 생성.
	 * @return true 이면 화면 회전시마다 encoder를 다시 생성한다. false 면 rotation 값만 서버에 전달 한다.
	 */
	public boolean isRotationResetEncoder();
	
	/**
	 * 화면 사지즈 조절시 Encoder 재 생성.
	 * @return true 이면 화면 사이즈를 조절 할때마다 encoder를 다시 생성한다. 후 optionMsg 를 전송한다.
	 * false 이면 아무 일도 하지 않는다.
	 */
	public boolean isResizeResetEncoder();
}
