package com.rsupport.srn30.screen.encoder;

import android.graphics.Point;

import com.rsupport.srn30.ASHM_SCREEN;
import com.rsupport.srn30.adjust.AdjustBitRate;

public class ScapOption {
	public static final int ENCODER_TYPE_JPG = 'T';
	public static final int ENCODER_TYPE_JPG_FOR_VD = 'D';
	public static final int ENCODER_TYPE_OMX = 'O';
	public static final int ENCODER_TYPE_OMX_FOR_VD = 'V';
	
	static public final int STATE_ENCODER_NONE	    		= 0x0000;
	static public final int STATE_ENCODER_RUNNING    		= 0x0010;
	static public final int STATE_ENCODER_PAUSED		 	= 0x0020;
	static public final int STATE_ENCODER_STOP    			= 0x0040;
	// knox drm state
	static public final int STATE_ENCODER_DRM    			= 0x00100;
	
	public static final int DEFAULT_ENCODER = ENCODER_TYPE_JPG;
	public static final int DEFAULT_QUALITY = 80;
	public static final int DEFAULT_BITRATE = 1024*1024*2;
	public static final int DEFAULT_FRAME_RATE = 30;
	public static final int DEFAULT_BITTYPE = ASHM_SCREEN.RGBwithDIRTY;
	public static final int DEFAULT_IFRAME_INTERVAL = 10;
	
	private int encoderType = DEFAULT_ENCODER;
	private int runState = STATE_ENCODER_STOP;
	private Point stretch = null;
	private int tileCache = 0;
	private int jpegQuality = DEFAULT_QUALITY;
	
	private int bitrate = DEFAULT_BITRATE;
	private int bitType = DEFAULT_BITTYPE;
	private int frameRate = DEFAULT_FRAME_RATE;
	private int iFrameInterval = DEFAULT_IFRAME_INTERVAL;
	private int colorFormat = 0;
	
	private int stride = 0;
	private int remotebpp = 8;
	
	private int sumBitRateIndex = 0;
	private int currentBitRateIndex = 0;
	private int changeEventCount = 0;
	private int bitrateIndex = 0;
	private int topBitRateIndex = 0;
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString())
		.append(" : ")
		.append("encoderType(").append(String.format("%c", encoderType)).append("), ").append("\n")
		.append("stretch(").append(stretch).append("), ").append("\n")
		.append("tileCache(").append(tileCache).append("), ").append("\n")
		.append("jpegQuality(").append(jpegQuality).append("), ").append("\n")
		.append("bitrate(").append(bitrate).append("), ").append("\n")
		.append("bitType(").append(bitType).append("), ").append("\n")
		.append("colorFormat(").append(colorFormat).append("), ").append("\n")
		.append("frameRate(").append(frameRate).append("), ").append("\n")
		.append("iFrameInterval(").append(iFrameInterval).append("), ").append("\n")
		.append("stride(").append(stride).append("), ").append("\n")
		.append("remotebpp(").append(remotebpp).append("), ").append("\n")
		.append("runState(").append(runState).append(")");
		return sb.toString();
	}
	
	public ScapOption(){
		stretch = new Point();
	}
	
	public void clear() {
		stretch.x = 0;
		stretch.y = 0;
		encoderType = DEFAULT_ENCODER;
		runState = STATE_ENCODER_STOP;
		bitrate = DEFAULT_BITRATE;
		tileCache = 0;
		jpegQuality = DEFAULT_QUALITY;
		bitType = DEFAULT_BITTYPE;
		frameRate = DEFAULT_FRAME_RATE;
		iFrameInterval = DEFAULT_IFRAME_INTERVAL;
		colorFormat = 0;
	}
	
	public void setStretch(int width, int height){
		stretch.x = width;
		stretch.y = height;
	}
	
	public int getEncoderType() {
		return encoderType;
	}
	public void setEncoderType(int encoderType) {
		this.encoderType = encoderType;
	}
	
	public int getRunState() {
		return runState;
	}
	
	public void setRunFlags(int runFlags) {
		this.runState = runFlags;
	}
	
	public void addRunFlags(int encoderChanged) {
		this.runState |= encoderChanged;
	}
	
	public Point getStretch() {
		return stretch;
	}
	public void setStretch(Point stretch) {
		this.stretch = stretch;
	}
	public int getTileCache() {
		return tileCache;
	}
	public void setTileCache(int tileCache) {
		this.tileCache = tileCache;
	}
	public int getJpegQuality() {
		return jpegQuality;
	}
	public void setJpegQuality(int jpegQuality) {
		this.jpegQuality = jpegQuality;
	}

	public int getBitrate() {
		return bitrate;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	public int getRemotebpp() {
		return remotebpp;
	}

	public void setRemotebpp(int remotebpp) {
		this.remotebpp = remotebpp;
	}

	public int getBitType() {
		return bitType;
	}

	public void setBitType(int bitType) {
		this.bitType = bitType;
	}

	public int getStride() {
		return stride;
	}

	public void setStride(int stride) {
		this.stride = stride;
	}
	
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	public int getFrameRate() {
		return frameRate;
	}
	
	public void setIFrameInterval(int iFrameInterval) {
		this.iFrameInterval  = iFrameInterval;
	}

	public int getIFrameInterval() {
		return iFrameInterval;
	}

	public int getColorFormat() {
		return colorFormat;
	}

	public void setColorFormat(int colorFormat) {
		this.colorFormat = colorFormat;
	}
	
	public void saveBitRateSetting(AdjustBitRate adjustBitRate) {
		if(adjustBitRate != null){
			sumBitRateIndex = adjustBitRate.getSumBitRateIndex();
			currentBitRateIndex = adjustBitRate.getCurrentBitrateIndex();
			changeEventCount = adjustBitRate.getChangeEventCount();
			bitrateIndex = adjustBitRate.getBitrateIndex();
			topBitRateIndex = adjustBitRate.getTopBitRateIndex();
		}
	}
	
	public void restoreBitRateSetting(AdjustBitRate adjustBitRate){
		if(adjustBitRate != null){
			if (changeEventCount != 0) {
				adjustBitRate.setSumBitRateIndex(sumBitRateIndex);
				adjustBitRate.setCurrentBitRateIndex(currentBitRateIndex);
				adjustBitRate.setChangeEventCount(changeEventCount);
				adjustBitRate.setBitrateIndex(bitrateIndex);
				adjustBitRate.setTopBitRateIndex(topBitRateIndex);
			}
			adjustBitRate.setConfigure(getStretch().x, getStretch().y, getFrameRate());
			setBitrate(adjustBitRate.getCurrentBitrate());
		}
	}
}
