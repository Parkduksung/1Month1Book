package com.rsupport.srn30.screen.capture;

import com.rsupport.srn30.screen.encoder.ScapOption;

abstract public class AbstractCaptureable implements IScreenCaptureable{
	protected ScapOption scapOption = null;

	public void setScapOption(ScapOption scapOption){
		this.scapOption = scapOption;
	}
	
	public ScapOption getScapOption(){
		return scapOption;
	}
	
	public String getEncOptions(ScapOption scapOption) {
		StringBuilder sb = new StringBuilder("opts=enc");
		sb.append("&type=").append(scapOption.getEncoderType());
		sb.append("&stretch=").append((float)scapOption.getStretch().x/(float)scapOption.getStretch().y);
		sb.append("&tilecache=").append(scapOption.getTileCache());
		sb.append("&jpgQuality=").append(scapOption.getJpegQuality());
		sb.append("&remotebpp=").append(scapOption.getRemotebpp());
		sb.append("&useDelaySend=").append(0);
		return sb.toString();
	}
}
