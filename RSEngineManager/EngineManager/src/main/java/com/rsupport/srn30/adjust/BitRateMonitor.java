package com.rsupport.srn30.adjust;


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
 * FileName: BitRateMonitor.java<br>
 * Author  : kwcho<br>
 * Date    : 2014. 8. 22.오후 12:32:17<br>
 * Purpose : network 에 send time을 측정하여 bitrate 를 조절 할 수 있다록 한다.<p>
 *
 * @see com.rsupport.srn30.adjust.AdjustBitRate
 * [History]<p>
 */
public class BitRateMonitor {
	/**
	 * network에 보내지는 Check 갯수.
	 */
	private final int MAX_CHECK_SEND_FRAME = 10; 
	
	/**
	 * Bitrate 를 낮추는 기준에 되는 FPS
	 */
	private int LOWER_FPS = 1000 / 20;
	
	/**
	 * Bitrate 를 높이는 기준에 되는 FPS
	 */
	private int UPPER_FPS = 1000 / 29;
	
	/**
	 * FrameRate 를 실제로 조절 할 수 있는 listener callback
	 */
	private OnBitRateChangeListener listener = null;
	
	private int sendFrameCount = 0;
	private long startTime = 0;
	private long endTime = 0;
	private long totalDuration = 0;
	
	/**
	 * Framerate 변경을 감지는는 callback 을 등록한다.
	 * @param changeListener
	 */
	public void setOnFrameRateChangeListener(OnBitRateChangeListener changeListener){
		this.listener = changeListener;
	}

	/**
	 * {@link #startTime()} 에서 {@link #endTime()} 까지 걸린 시간을 측정한다.
	 * @return
	 */
	public long getDuration() {
		return endTime - startTime;
	}

	/**
	 * 시작시간을 마킹한다.
	 */
	public void startTime() {
		startTime = System.currentTimeMillis();
	}

	/**
	 * 종료 시간을 마킹한다.
	 */
	public void endTime() {
		endTime = System.currentTimeMillis();
	}
	
	/**
	 * {@link #MAX_CHECK_SEND_FRAME} 만큼 걸린 시간을 측정한다.
	 */
	private void sumDuration(){
		totalDuration += getDuration();
		sendFrameCount++;
	}
	
	/**
	 * {@link #MAX_CHECK_SEND_FRAME} 보내는동안의 평균 시간을 구한다.
	 * @return
	 */
	private long getDurationAVG(){
		return totalDuration/MAX_CHECK_SEND_FRAME;
	}
	
	/**
	 * {@link com.rsupport.srn30.adjust.OnBitRateChangeListener} 에 변경 event 를 전달한다.
	 */
	public void checkChangeFrameRate(){
		if(sendFrameCount < MAX_CHECK_SEND_FRAME){
			sumDuration();
		}else{
			if(getDurationAVG() > LOWER_FPS){
				if(listener != null){
					listener.onLowerEvent();
				}
			}
			else if(getDurationAVG() < UPPER_FPS){
				if(listener != null){
					listener.onUpperEvent();
				}
			}
			totalDuration = getDuration();
			sendFrameCount = 0;
		}
	}

	public void onDestroy() {
		listener = null;
	}
}
