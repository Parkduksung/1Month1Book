package com.rsupport.srn30.screen.encoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

import com.rsupport.srn30.rcp;
import com.rsupport.srn30.adjust.AdjustFPS;
import com.rsupport.srn30.screen.capture.IScreenCaptureable;
import com.rsupport.srn30.screen.channel.IChannelWriter;
import com.rsupport.srn30.screen.channel.Srn30Packet;
import com.rsupport.util.LockObject;
import com.rsupport.util.rslog.MLog;

abstract public class AbstractEncoder implements IEncoder, Runnable{
	protected Context context = null;
	protected ScapOption scapOption = null;
	protected IScreenCaptureable screenCaptureable = null;
	protected IChannelWriter channelWriter = null;
	private Thread thread = null;
	private LinkedBlockingQueue<ICommand> commandQueue = null;
	private LockObject lockObject = null;
	protected int hwRotation = 0;

	abstract public boolean initialized(Object initResult);
	abstract public boolean onSuspended();
	abstract public boolean sendFrame() throws Exception;

	public AbstractEncoder(Context context) {
		this.context = context;
		lockObject = new LockObject();
		commandQueue = new LinkedBlockingQueue<ICommand>();
	}

	public void onDestroy(){
		MLog.i("#enter onDestroy");
		thread = null;
		if(commandQueue != null){
			commandQueue.clear();
			commandQueue = null;
		}

		if(screenCaptureable != null && screenCaptureable.isAlive()){
			screenCaptureable.close();
		}
		screenCaptureable = null;

		synchronized (this) {
			scapOption = null;
		}
		
		context = null;
		channelWriter = null;
		hwRotation = 0;
		MLog.i("#exit onDestroy");
	}

	@Override
	public void setOption(ScapOption scapOption) {
		this.scapOption = scapOption;
	}

	@Override
	public ScapOption getScapOption() {
		return scapOption;
	}

	@Override
	public void setOnScreenCaptureable(IScreenCaptureable screenCaptureable){
		this.screenCaptureable = screenCaptureable;
	}

	@Override
	public void setHWRotation(int hwRotation){
		this.hwRotation = hwRotation;
	}

	@Override
	public synchronized boolean isAlived() {
		if(thread == null){
			return false;
		}

		if(thread.isAlive() == false){
			return false;
		}

		if(scapOption.getRunState() == ScapOption.STATE_ENCODER_STOP){
			return false;
		}

		if(scapOption.getRunState() == ScapOption.STATE_ENCODER_NONE){
			return false;
		}
		return true;
	}

	@Override
	public void command(ICommand command) throws InterruptedException{
		if(commandQueue != null){
			commandQueue.put(command);
			lockObject.notifyLock();
		}
	}

	@Override
	public void suppend(int timeOut) throws InterruptedException{
		if(scapOption.getRunState() != ScapOption.STATE_ENCODER_PAUSED){
			command(new PauseCommand());
			waitForRunFlag(ScapOption.STATE_ENCODER_PAUSED, timeOut);
		}
	}

	@Override
	public synchronized void resume() {
		getScapOption().setRunFlags(ScapOption.STATE_ENCODER_RUNNING);
		lockObject.notifyLock();
	}

	@Override
	public synchronized boolean start() throws InterruptedException{
		if(thread == null){
			thread = new Thread(this, "EncoderJpeg");
			thread.start();
		}
		if(scapOption.getRunState() == ScapOption.STATE_ENCODER_PAUSED){
			return true;
		}
		waitForRunFlag(ScapOption.STATE_ENCODER_RUNNING, 3000);
		return scapOption.getRunState() == ScapOption.STATE_ENCODER_RUNNING;
	}

	@Override
	public void stop() throws InterruptedException{
		if(scapOption.getRunState() != ScapOption.STATE_ENCODER_STOP){
			command(new StopCommand());
			if(lockObject != null){
				lockObject.notifyLock();
			}
			waitForRunFlag(ScapOption.STATE_ENCODER_STOP, 3000);
		}
	}

	@Override
	public void setChannelWriter(IChannelWriter channelWriter) {
		this.channelWriter = channelWriter;
	}

	@Override
	public boolean isRotationResetEncoder() {
		if(screenCaptureable != null){
			return screenCaptureable.isRotationResetEncoder();
		}
		return false;
	}

	@Override
	public boolean isResizeResetEncoder() {
		if(screenCaptureable != null){
			return screenCaptureable.isResizeResetEncoder();
		}
		return false;
	}

	protected void waitForRunFlag(int runFlag, int timeOut) throws InterruptedException{
		if(timeOut <= 0){
			return;
		}

		long startTime = System.currentTimeMillis();
		while(getScapOption().getRunState() != runFlag){
			if(getScapOption().getRunState() == ScapOption.STATE_ENCODER_STOP){
				break;
			}
			if(System.currentTimeMillis() - startTime > timeOut){
				break;
			}
			Thread.sleep(10);
		}
	}

	private void processCommandQueue(){
		if(commandQueue != null){
			ICommand command = commandQueue.poll();
			if(command != null){
				command.execute();
			}
		}
	}

	protected synchronized boolean sendOption2Msg(){
		try {
			int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay()
					.getRotation();
			ByteBuffer bb = Srn30Packet.scapOption2Msg( (rotation + hwRotation) % 4, scapOption);
			if(channelWriter != null){
				return channelWriter.write(bb.array(), 0, bb.position());
			}
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
		}
		return false;
	}
	
	private boolean sendScreenState(int state) throws Exception{
		ByteBuffer bb = ByteBuffer.allocate(256).order(ByteOrder.LITTLE_ENDIAN);
		bb.put((byte) rcp.rcpScreenCtrl); // payload
		bb.putInt(1); // msgsize
		int screenState = rcp.rcpScreenSuspend;
		if(state == ScapOption.STATE_ENCODER_DRM || state == ScapOption.STATE_ENCODER_RUNNING){
			screenState = rcp.rcpScreenResume;
		}
		bb.put((byte)screenState); // state
		MLog.v("screenState.%d", screenState);
		if(channelWriter != null){
			return channelWriter.write(bb.array(), 0, bb.position());
		}
		return false;
	}

	@Override
	public void run() {
		try {

			AdjustFPS adjustFPS = new AdjustFPS();
			adjustFPS.init(scapOption.getFrameRate());
			
			if(screenCaptureable == null){
				sendScreenState(ScapOption.STATE_ENCODER_STOP);
				scapOption.setRunFlags(ScapOption.STATE_ENCODER_STOP);
				return;
			}
			
			if(screenCaptureable.isAlive() == true){
				screenCaptureable.close();
			}
			
			if(scapOption.getRunState() == ScapOption.STATE_ENCODER_PAUSED){
				lockObject.enforceLock();
				processCommandQueue();
				if(scapOption.getRunState() == ScapOption.STATE_ENCODER_STOP == true){
					return;
				}
			}

			boolean initResult = initialized(screenCaptureable.initialized());
			if(initResult == false){
				MLog.e("encoderJpeg initialized fail!");
				sendScreenState(ScapOption.STATE_ENCODER_STOP);
				scapOption.setRunFlags(ScapOption.STATE_ENCODER_STOP);
				return;
			}

			scapOption.setRunFlags(ScapOption.STATE_ENCODER_RUNNING);

			sendOption2Msg();

			sendScreenState(ScapOption.STATE_ENCODER_RUNNING);

			while(scapOption.getRunState() != ScapOption.STATE_ENCODER_STOP){

				if(adjustFPS.isContinue()){
					continue;
				}

				if(scapOption.getRunState() == ScapOption.STATE_ENCODER_PAUSED == true){
					if(screenCaptureable != null){
						screenCaptureable.close();
					}
					
					onSuspended();
					sendScreenState(ScapOption.STATE_ENCODER_PAUSED);
					
					if(lockObject != null){
						MLog.d("encoder paused.");
						lockObject.enforceLock();
						MLog.d("encoder resume.");
					}
				}

				processCommandQueue();

				synchronized (this) {
					if(screenCaptureable.isAlive() == false && 
							scapOption.getRunState() == ScapOption.STATE_ENCODER_RUNNING == true){
						initialized(screenCaptureable.initialized());
						sendOption2Msg();
						sendScreenState(ScapOption.STATE_ENCODER_RUNNING);
					}
				}

				if(scapOption.getRunState() == ScapOption.STATE_ENCODER_STOP == true){
					break;
				}

				if(scapOption.getRunState() == ScapOption.STATE_ENCODER_RUNNING == true  ||
						scapOption.getRunState() == ScapOption.STATE_ENCODER_DRM){
					synchronized (this) {
						int state = screenCaptureable.prepareCapture();
						if(state == IScreenCaptureable.STATE_CONTINUE){
							continue;
						}

						if(state == IScreenCaptureable.STATE_BREAK){
							MLog.w("prepareCapture fail");
							break;
						}

						if(screenCaptureable.capture() == false){
							if(scapOption != null){
								scapOption.setRunFlags(ScapOption.STATE_ENCODER_DRM);
							}
							continue;
						}else{
							if(scapOption != null){
								scapOption.setRunFlags(ScapOption.STATE_ENCODER_RUNNING);
							}
						}

						if(sendFrame() == false){
							MLog.w("sendFrame fail");
							break;
						}

						if(screenCaptureable.postCapture() == false){
							MLog.w("prepareCapture fail");
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
		} finally{
			synchronized (AbstractEncoder.this) {
				if(scapOption != null){
					try {
						sendScreenState(ScapOption.STATE_ENCODER_STOP);
					} catch (Exception e) {
						MLog.e(Log.getStackTraceString(e));
					}
					scapOption.setRunFlags(ScapOption.STATE_ENCODER_STOP);
				}
			}
		}
		MLog.i("encoder terminate!");
	}

	public class StopCommand implements ICommand{
		@Override
		public void execute() {
			synchronized (AbstractEncoder.this) {
				getScapOption().setRunFlags(ScapOption.STATE_ENCODER_STOP);
			}
		}
	}

	public class PauseCommand implements ICommand{
		@Override
		public void execute() {
			synchronized (AbstractEncoder.this) {
				getScapOption().setRunFlags(ScapOption.STATE_ENCODER_PAUSED);
			}
		}
	}

}
