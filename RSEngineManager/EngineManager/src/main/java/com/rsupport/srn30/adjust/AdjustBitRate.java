package com.rsupport.srn30.adjust;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Bundle;

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
 * FileName: AdjustBitRate.java<br>
 * Author  : kwcho<br>
 * Date    : 2014. 8. 22.오후 12:20:51<br>
 * Purpose : FrameRate 를 조절 하는 class 이다.<p>
 * 
 * @see com.rsupport.srn30.adjust.BitRateMonitor
 * @see com.rsupport.srn30.adjust.OnBitRateChangeListener
 *
 * [History]<p>
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class AdjustBitRate implements OnBitRateChangeListener{
	
	/**
	 * 기본 FPS Index {@link #BITERATES}
	 */
	private final int DROP_BITRATE_INDEX = 3;
	
	/**
	 * 시작 Bitrate index
	 */
	private final int START_BITRATE_INDEX = 7;
	
	/**
	 * max bitrate index
	 */
	private final int MAX_BITRATE_INDEX = START_BITRATE_INDEX + 3;
	
	/**
	 * width * height * fps * codec value
	 */
	private final float CODEC_VALUE = 0.12f;
	
	/**
	 * baseBitrate offset
	 */
	private int BITRATE_OFFSET = 1024 * 256;
	

	/**
	 * Bitrate 가 변경되고 다음 bitrate 변경 요청이 왔을때 해당 시간이 지난 후에 적용된다.
	 */
	private int BITRATE_UPPER_CHANGE_TIME = 1000 * 10;

	/**
	 * Bitrate 가 변경되고 다음 bitrate 변경 요청이 왔을때 해당 시간이 지난 후에 적용된다.
	 */
	private int BITRATE_LOWER_CHANGE_TIME = 1000 * 2;

	// 복원시 필요함.
	private int sumBitRateIndex = START_BITRATE_INDEX;
	// 복원시 필요함.
	private int currentBitRateIndex = START_BITRATE_INDEX;
	// 복원시 필요함.
	private int changeEventCount = 0;
	// 복원시 필요함.
	private int bitrateIndex = START_BITRATE_INDEX;
	// 복원시 필요함.
	private int topBitRateIndex = 0;

	private long bitrateChangeTime = 0;
	private Bundle params = null;
	private MediaCodec mediaCodec = null;
	
	/**
	 * width * height * fps * codec value
	 */
	private int baseBitrate = 0;
	
	public AdjustBitRate(){
		params = new Bundle();
	}
	
	public void setConfigure(int width, int height, int fps){
		// ((가로 * 세로 * fps) * codec value)
		baseBitrate = (int)(((width*height*fps)*CODEC_VALUE));
		BITRATE_OFFSET = baseBitrate/10;
		MLog.d("width.%d, height.%d, fps.%d, baseBitrate.%d, BITRATE_OFFSET.%d", width, height, fps, baseBitrate, BITRATE_OFFSET);
	}
	

	/**
	 * MediaCodec 설정.
	 * @param mediaCodec
	 */
	public void setMediaCodec(MediaCodec mediaCodec){
		this.mediaCodec = mediaCodec;
	}

	/**
	 * 현재 설정된 bitrate index 를 가져온다.
	 * @return
	 */
	public int getCurrentBitrateIndex(){
		return currentBitRateIndex;
	}

	/**
	 * 현재 설정된 bitrate 를 반환한다.
	 * @return
	 */
	public int getCurrentBitrate() {
		if(baseBitrate == 0){
			baseBitrate = (int)(((640*480*30)*CODEC_VALUE));
		}
		
		int currentBitrate = baseBitrate + (BITRATE_OFFSET * (currentBitRateIndex - START_BITRATE_INDEX));
		int ret = (currentBitrate < BITRATE_OFFSET * 2) ? BITRATE_OFFSET * 2 : currentBitrate;
		
		if (Build.MODEL.startsWith("SM-N910")) {
			if (ret > 3000000) {
				return 3000000;
			}
		}
		
		return ret;
	}

	/**
	 * bitrate 를 설정한다.
	 * @param bitrateIndex
	 */
	public void setBitrateIndex(int bitrateIndex){
		if(bitrateIndex > MAX_BITRATE_INDEX){
			bitrateIndex = MAX_BITRATE_INDEX;
		}
		this.bitrateIndex = bitrateIndex;
	}

	/**
	 * 종료시 호출 
	 */
	public void onDestroy(){
		params = null;
		mediaCodec = null;
		bitrateChangeTime = 0;
		bitrateIndex = 0;
	}


	@Override
	public void onUpperEvent() {
		if(System.currentTimeMillis() - bitrateChangeTime > BITRATE_UPPER_CHANGE_TIME){
			bitrateChangeTime = System.currentTimeMillis();
			if(bitrateIndex < MAX_BITRATE_INDEX){
				bitrateIndex++;
				if(topBitRateIndex < bitrateIndex){
					topBitRateIndex = bitrateIndex;
				}
			}
			int beforeUseIndex = currentBitRateIndex;
			
			changeEventCount++;
			sumBitRateIndex += bitrateIndex;
			int avgBitRateIntex = sumBitRateIndex/(changeEventCount + 1);
			currentBitRateIndex = (avgBitRateIntex + topBitRateIndex) / 2;
			
//			MLog.e("bitrateIndex.%d, currentBitRateIndex.%d, sumBitRateIndex.%d, changeEventCount.%d",
//					bitrateIndex,
//					currentBitRateIndex,
//					sumBitRateIndex,
//					changeEventCount);
			
			if(bitrateIndex < currentBitRateIndex){
				currentBitRateIndex = bitrateIndex;
			}
			
			if(currentBitRateIndex != beforeUseIndex){
				params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, getCurrentBitrate());
				MLog.i("currentBitrate.%d", getCurrentBitrate());
				if(mediaCodec != null){
					mediaCodec.setParameters(params);
				}
			}
		}
	}
	
	
	@Override
	public void onLowerEvent() {
		if(bitrateIndex > DROP_BITRATE_INDEX){
			bitrateIndex = DROP_BITRATE_INDEX;
			currentBitRateIndex = bitrateIndex;
			changeEventCount++;
			sumBitRateIndex += bitrateIndex;
			if(mediaCodec != null){
				params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, getCurrentBitrate());
				MLog.i("currentBitrate.%d", getCurrentBitrate());
				mediaCodec.setParameters(params);
			}
			bitrateChangeTime = System.currentTimeMillis();
		}else{
			if(System.currentTimeMillis() - bitrateChangeTime > BITRATE_LOWER_CHANGE_TIME){
				if(bitrateIndex > 0){ 
					bitrateIndex--;
					changeEventCount++;
					currentBitRateIndex = bitrateIndex;
					sumBitRateIndex += bitrateIndex;
					if(mediaCodec != null){
						params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, getCurrentBitrate());
						mediaCodec.setParameters(params);
						MLog.i("currentBitrate.%d", getCurrentBitrate());
					}
					bitrateChangeTime = System.currentTimeMillis();
				}
			}
		}
	}

	public int getSumBitRateIndex() {
		return sumBitRateIndex;
	}

	public void setSumBitRateIndex(int sumBitRateIndex) {
		this.sumBitRateIndex = sumBitRateIndex;
	}

	public void setCurrentBitRateIndex(int currentBitRateIndex) {
		this.currentBitRateIndex = currentBitRateIndex;
	}

	public int getChangeEventCount() {
		return changeEventCount;
	}

	public void setChangeEventCount(int changeEventCount) {
		this.changeEventCount = changeEventCount;
	}
	
	public int getTopBitRateIndex() {
		return topBitRateIndex;
	}
	
	public void setTopBitRateIndex(int topBitRateIndex) {
		this.topBitRateIndex = topBitRateIndex;
	}

	public int getBitrateIndex(){
		return bitrateIndex;
	}
}
