package com.rsupport.srn30.screen.encoder;

import java.nio.ByteBuffer;

import android.content.Context;

import com.rsupport.srn30.ASHM_SCREEN;
import com.rsupport.util.MemoryFileEx;
import com.rsupport.util.rslog.MLog;


public class EncoderAshmemForJpg extends AbstractEncoder{
	private MemoryFileEx ashmem = null;
	
	public EncoderAshmemForJpg(Context context) {
		super(context);
		MLog.i("EncoderAshmemForJpg");
	}
	
	@Override
	public void onDestroy() {
		MLog.i("#enter onDestroy");
		try {
			stop();
		} catch (Exception e) {
		}
		ashmem = null;
		super.onDestroy();
		MLog.i("#exit onDestroy");
	}

	@Override
	public void setOption(ByteBuffer msg) {
		scapOption.setBitType(ASHM_SCREEN.RGBwithDIRTY);
		// jpg quality 100보다 큰경우 default 로 설정한다.
		// 'O', 'V' 로 요청하였지만 codec 을 지원 하지 않으면 'T' 로 변환한다.
		// encoder type 이 'O', 'V' 일경우 msg 는 bitrate이므로 100보다 클수 있다.
		int jpgQuality = msg.getInt();
		if(jpgQuality > 100){
			scapOption.setJpegQuality(ScapOption.DEFAULT_QUALITY);
			scapOption.setTileCache(0);
		}
		else{
			scapOption.setJpegQuality(jpgQuality);
			scapOption.setTileCache(msg.getInt());
		}
		scapOption.setRunFlags(ScapOption.STATE_ENCODER_NONE);
	}

	@Override
	public boolean sendFrame() throws Exception{
		if(channelWriter == null){
			return false;
		}
		if(channelWriter.writeAshmem(ashmem.address()) == false){
			MLog.w("write ashmem fail.");
			return false;
		}
		return true;
	}

	@Override
	public boolean initialized(Object initResult) {
		ashmem = (MemoryFileEx)initResult;
		return true;
	}
	
	
	@Override
	public boolean onSuspended() {
		if(ashmem != null){
			ashmem.close();
			ashmem = null;
		}
		return true;
	}
}
