package com.rsupport.srn30.screen.encoder;

import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.rsperm.IEnginePermission;
import com.rsupport.rsperm.SonyPermission;
import com.rsupport.srn30.rcp;
import com.rsupport.srn30.screen.channel.IChannelWriter;
import com.rsupport.srn30.screen.encoder.IEncoder.ICommand;
import com.rsupport.srn30.screen.encoder.codec.MediaCodecEncoder;
import com.rsupport.srn30.screen.rotation.OrientationManager;
import com.rsupport.util.Screen;
import com.rsupport.util.rslog.BuildConfig;
import com.rsupport.util.rslog.MLog;

public class EncoderManager {
	private final int rcpOption_SCap_Encoder = 0;
	private final int rcpOption_SCap_Ratio = 1;
	private Context context = null;
	private IEncoder encoder = null;
	private IEnginePermission permission = null;
	private IChannelWriter channelWriter = null;
	private ScapOption scapOption = null;
	private OrientationManager orientationManager = null;

	public EncoderManager(Context context) {
		this.context = context;
		scapOption = new ScapOption();
	}

	public synchronized void onDestroy() {
		MLog.i("#enter onDestroy");
		if(encoder != null){
			encoder.onDestroy();
			encoder = null;
		}
		permission = null;
		context = null;
		channelWriter = null;
		scapOption = null;
		orientationManager = null;
		MLog.i("#exit onDestroy");
	}

	public boolean onCommand(int payloadtype, ByteBuffer msg) {
		switch (payloadtype) {
		case rcp.rcpScreenCtrl:
			MLog.i("rcpScreenCtrl");
			requestScreenControll(msg);
			break;
		case rcp.rcpOption:
			MLog.i("rcpOption");
			requestOption(msg);
			break;
		}
		return true;
	}

	public void command(ICommand command){
		try {
			encoder.command(command);
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
		}
	}

	public synchronized void encoderSuspend(int timeOut){
		if(encoder != null){
			try {
				encoder.suppend(timeOut);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void encoderResume(){
		if(encoder != null){
			encoder.resume();
		}
	}
	
	public synchronized void orientationChanged(int rotation) throws InterruptedException{
		if(encoder != null && encoder.isRotationResetEncoder() == true){
			swapStretch(scapOption.getStretch());
			int runFlag = scapOption.getRunState();
			MLog.d("orientationChanged : " + runFlag);
			
			releaseEncoder();
			
			scapOption.setRunFlags(runFlag);
			IEncoder encoder = createEncoder(scapOption);
			
			if(runFlag == ScapOption.STATE_ENCODER_PAUSED){
				encoder.suppend(-1);
			}
			boolean startResult = encoder.start();
			MLog.i("startResult.%b", startResult);
		}
	}

	private void requestScreenControll(ByteBuffer msg) {
		int id = msg.get() & 0xff;
		switch (id) {
		case rcp.rcpScreenSuspend: // 화면 전달 일시 중지
			MLog.i("rcpScreenSuspend");
			if(encoder != null){
				try {
					encoder.suppend(3000);
				} catch (Exception e) {
				}
			}
			break;

		case rcp.rcpScreenResume: // 화면 전달 다시 시작
			MLog.i("rcpScreenResume");
			if(encoder != null){
				if(encoder.isAlived() == false){
					try {
						boolean startResult = encoder.start();
						MLog.i("startResult.%b", startResult);
					} catch (Exception e) {
					}
				}
				encoder.resume();
			}
			break;
		}
	}

	private void requestOption(ByteBuffer msg) {
		int id = msg.get() & 0xff;
		msg.getInt(); // skip dataSize
		switch (id) {
		case rcp.rcpOption_SCap:
			int subType = msg.getInt();
			switch (subType) {
			case rcpOption_SCap_Encoder:
				MLog.i("setEncoder");
				releaseEncoder();
				createEncoder((msg.get()&0xFF), msg);
				break;
			case rcpOption_SCap_Ratio:
				MLog.i("setRatio");
				if(scapOption.getRunState() == ScapOption.STATE_ENCODER_DRM){
					MLog.e("drm state");
					return;
				}
				setRatioOption(msg.getInt(), msg.getInt());
				break;
			}
			break;
		}
	}

	private synchronized void releaseEncoder(){
		if(encoder != null){
			if(encoder instanceof AbstractEncoder){
				((AbstractEncoder)encoder).onDestroy();
				encoder = null;
			}
		}
	}
	
	/**
	 * Display size의 배수를 처리한다.
	 * @param num
	 * @param unit
	 * @return
	 */
	private int align(int num, int unit) { 
		return (num+unit-1) & ~(unit-1); 
	}
	
	/**
	 * Display size의 배수를 처리한다. 실제 Display 사이즈보다 큰 경우에 대한 예외처리.
	 * @param align
	 * @param num
	 * @param unit
	 * @return
	 */
	private int align(int align, int num, int unit) {
		if (align >= num) {
			align = num / unit * unit;
		}
		return align;
	}
	
	private void setRatioOption(int width, int height) {
		// 64의 배수로 설정한다.(일부폰 16의 배수로 하면 화면이 깨진다)
		int width_align = 64;
		Rect rect = Screen.getScreenRect(context, orientationManager.getHWRotation());
        

		int nAlignedWidth = align(width, width_align);
		MLog.i("nAlignedWidth1 : " + nAlignedWidth);
		nAlignedWidth = align(nAlignedWidth, rect.right, width_align);
		MLog.i("nAlignedWidth2 : " + nAlignedWidth);
		
		int nAlignedHeight = align(height, width_align);
		MLog.i("nAlignedHeight1 : " + nAlignedHeight);
		nAlignedHeight = align(nAlignedHeight, rect.bottom, width_align);
		MLog.i("nAlignedHeight2 : " + nAlignedHeight);
		MLog.i("setRatioOption get size : " + width +", " + height + " nAlignedWidth " + nAlignedWidth + " rect.left " + rect.left + " nAlignedHeight " + nAlignedHeight + " rect.bottom " + rect.bottom);

		if(encoder != null){
			if(encoder.isResizeResetEncoder() == true){
				int runFlag = scapOption.getRunState();
				releaseEncoder();
				scapOption.setStretch(nAlignedWidth, nAlignedHeight);
				IEncoder encoder = createEncoder(scapOption);
				scapOption.setRunFlags(runFlag);
				try {
					if(runFlag == ScapOption.STATE_ENCODER_PAUSED){
						encoder.suppend(-1);
					}
					boolean startResult = encoder.start();
					MLog.i("startResult : " + startResult);
				} catch (Exception e) {
					MLog.e(Log.getStackTraceString(e));
				}
			}
		}
		else{
			scapOption.setStretch(nAlignedWidth, nAlignedHeight);
		}
	}
	
	private IEncoder createEncoder(int requestEncoder){
		IEncoder encoder = null;
		switch (requestEncoder) {
		case ScapOption.ENCODER_TYPE_JPG:
			encoder = new EncoderAshmemForJpg(context);
			break;
		case ScapOption.ENCODER_TYPE_JPG_FOR_VD:
			encoder = new EncoderVirtualDisplayForJpg(context);
			break;
		case ScapOption.ENCODER_TYPE_OMX:
			encoder = new EncoderAshmemForOmx(context);
			break;
		case ScapOption.ENCODER_TYPE_OMX_FOR_VD:
			encoder = new EncoderVirtualDisplayForOmx(context);
			break;
		}
		return encoder;
	}

	/**
	 * 회전, 사이즈 조절시 호출된다.
	 * @param scapOption
	 * @return
	 */
	private synchronized IEncoder createEncoder(ScapOption scapOption){
		encoder = createEncoder(scapOption.getEncoderType());
		encoder.setHWRotation(orientationManager.getHWRotation());
		encoder.setOption(scapOption);
		encoder.setOnScreenCaptureable(permission.createScreenCaptureable(scapOption));
		encoder.setChannelWriter(channelWriter);
		return encoder;
	}
	
	/**
	 * 최초 Encoder 생성에서 불린다.
	 * @param requestEncoder
	 * @param msg
	 */
	private synchronized void createEncoder(int requestEncoder, ByteBuffer msg){
		requestEncoder = findEncoderType(permission.getSupportEncoder(), requestEncoder);
		MLog.i("find encoder : " + String.format("'%c'", requestEncoder));
		scapOption.setEncoderType(requestEncoder);
		encoder = createEncoder(requestEncoder);
		encoder.setHWRotation(orientationManager.getHWRotation());
		encoder.setOption(scapOption);
		encoder.setOption(msg);
		encoder.setOnScreenCaptureable(permission.createScreenCaptureable(encoder.getScapOption()));
		encoder.setChannelWriter(channelWriter);
		
		// 강제로 화면 사이즈를 조절해야 한다면 조절한다.
		Rect rect = Screen.getScreenRect(context, orientationManager.getHWRotation());
		MLog.v("Screen.getScreenRect : " + rect.bottom + " , " + rect.right);

		float displayRatio = 0;
		int height = 0;
		int width = 0;

		//테블릿 단말 (가로가 긴 단말 보정)
		if(rect.bottom > rect.right){
			displayRatio = (float)rect.bottom / rect.right;
			width = 720;
			height = ((int)(width * displayRatio)) / 64 * 64;
		}else{
			displayRatio = (float)rect.right / rect.bottom;
			height = 720;
			width = ((int)(height * displayRatio)) / 64 * 64;
		}


		if(enForceStretch(width, height) == true){

			encoder.setOption(scapOption);
			MLog.i("enforce stretch x.%d, y.%d",  scapOption.getStretch().x, scapOption.getStretch().y);
		}

		// 회전할때 다시 display 를 다시 생성해야 한다면 swap
		if(encoder.isRotationResetEncoder() == true){
			swapScreenSize();
		}
		
		MLog.i("createEncoder scapOption : " + scapOption.toString());
	}
	
	/**
	 * 화면 사이즈를 강제로 조절 한다.
	 * {@link com.rsupport.srn30.screen.encoder.AbstractEncoder#isResizeResetEncoder()} 값이 false 이고 {@link com.rsupport.rsperm.SonyPermission} 이 아닐경우에만 동작한다.
	 * {@link com.rsupport.rsperm.SonyPermission}의 경우에는 내부에서 처리된다.
	 * @param enForceMaxWidth 0 일경우는 display size,
	 * @param enForceMaxHeight 0 일 경우 display size
	 * @return true 이면 강제 조절 한 상태, false 이면 강제조절 안됨.
	 */
	private boolean enForceStretch(int enForceMaxWidth, int enForceMaxHeight){
		if (com.rsupport.android.engine.BuildConfig.IS_TAAS) {
			Rect rect = Screen.getScreenRect(context, orientationManager.getHWRotation());
			scapOption.getStretch().x = rect.right;
			scapOption.getStretch().y = rect.bottom;
			return true;
		} else if(encoder.isResizeResetEncoder() == false && (permission instanceof SonyPermission) == false){
			Rect rect = Screen.getScreenRect(context, orientationManager.getHWRotation());

			rect.right = (rect.right / 16 * 16);
			rect.bottom = (rect.bottom / 16 * 16);

			scapOption.getStretch().x = rect.right;
			scapOption.getStretch().y = rect.bottom;

			if((enForceMaxWidth > 0 && enForceMaxHeight > 0) &&
					(scapOption.getStretch().x > enForceMaxWidth ||
					scapOption.getStretch().y > enForceMaxHeight)){
				scapOption.getStretch().x = enForceMaxWidth;
				scapOption.getStretch().y = enForceMaxHeight;
			}
			return true;
		}
		return false;
	}
	
	
	private void swapScreenSize(){
		int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay()
				.getRotation();
		
		MLog.d("swapScreenSize rotation : " + rotation + ", orientationManager.getHWRotation() : " + orientationManager.getHWRotation());
		// 폰이 가로 모드 일때 처리.
		if(rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90){
			// hw rotation 이 90,270이 아닐때는 회전.
			if(orientationManager.getHWRotation() == Surface.ROTATION_0 ||
					orientationManager.getHWRotation() == Surface.ROTATION_180){
				swapStretch(scapOption.getStretch());
			}
		}
	}
	
	private void swapStretch(Point stretch){
		int tempX = stretch.x;
		stretch.x = stretch.y;
		stretch.y = tempX;
	}
	
	private int findEncoderType(int[] supportedEncoder, int request){
		MLog.i("request encoder : " + String.format("'%c'", request));
		// MediaCodec 을 사용하는 요청인데 코덱을 찾지 못하면 jpg 방식으로 변경.
		if(request == ScapOption.ENCODER_TYPE_OMX || request == ScapOption.ENCODER_TYPE_OMX_FOR_VD){
			MediaCodecEncoder encoder = new MediaCodecEncoder();
			if(encoder.getColorFormat() <= 0){
				request = ScapOption.ENCODER_TYPE_JPG;
			}
			encoder.onDestroy();
			encoder = null;
		}
		
		// 'O' 로 요청이 오면 가능하다면 'V' 를 사용한다.
		if(request == ScapOption.ENCODER_TYPE_OMX){
			if(isExistEncoder(supportedEncoder, ScapOption.ENCODER_TYPE_OMX_FOR_VD)){
				return ScapOption.ENCODER_TYPE_OMX_FOR_VD;
			}
			else if(isExistEncoder(supportedEncoder, ScapOption.ENCODER_TYPE_OMX)){
				return ScapOption.ENCODER_TYPE_OMX;
			}
			else if(isExistEncoder(supportedEncoder, ScapOption.ENCODER_TYPE_JPG_FOR_VD)){
				return ScapOption.ENCODER_TYPE_JPG_FOR_VD;
			}
			else if(isExistEncoder(supportedEncoder, ScapOption.ENCODER_TYPE_JPG)){
				return ScapOption.ENCODER_TYPE_JPG;
			}
		}
		
		// 'T' 로 요청이 오면 가능하다면 'D' 를 사용한다.
		if(request == ScapOption.ENCODER_TYPE_JPG){
			if(isExistEncoder(supportedEncoder, ScapOption.ENCODER_TYPE_JPG_FOR_VD)){
				return ScapOption.ENCODER_TYPE_JPG_FOR_VD;
			}
			else if(isExistEncoder(supportedEncoder, ScapOption.ENCODER_TYPE_JPG)){
				return ScapOption.ENCODER_TYPE_JPG;
			}
			else if(isExistEncoder(supportedEncoder, ScapOption.ENCODER_TYPE_OMX_FOR_VD)){
				return ScapOption.ENCODER_TYPE_OMX_FOR_VD;
			}
			else if(isExistEncoder(supportedEncoder, ScapOption.ENCODER_TYPE_OMX)){
				return ScapOption.ENCODER_TYPE_OMX;
			}
		}

		return ScapOption.ENCODER_TYPE_JPG;
	}

	private boolean isExistEncoder(int[] supportedEncoder, int request){
		for(int encoder : supportedEncoder){
			if(encoder == request){
				return true;
			}
		}
		return false;
	}

	public void setIEnginePermission(IEnginePermission permission) {
		this.permission= permission; 
	}

	public void setOnChannelWriter(IChannelWriter channelWriter) {
		this.channelWriter = channelWriter;
	}

	public void setOrientationManager(OrientationManager orientationManager) {
		this.orientationManager = orientationManager;
	}


	public synchronized ScapOption getScapOption(){
		return scapOption;
	}
}
