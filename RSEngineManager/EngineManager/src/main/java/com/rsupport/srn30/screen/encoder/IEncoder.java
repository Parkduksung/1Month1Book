package com.rsupport.srn30.screen.encoder;

import java.nio.ByteBuffer;

import com.rsupport.srn30.screen.capture.IScreenCaptureable;
import com.rsupport.srn30.screen.channel.IChannelWriter;

public interface IEncoder {

	public void setHWRotation(int hwRotation);
	public void setOption(ScapOption scapOption);
	public void setOption(ByteBuffer buffer);

	public ScapOption getScapOption();

	public boolean start() throws InterruptedException;
	public void resume();
	public void suppend(int timeOut) throws InterruptedException;
	public void stop() throws InterruptedException;
	public void command(ICommand command) throws InterruptedException;
	
	public void onDestroy();

	public void setOnScreenCaptureable(IScreenCaptureable screenCaptureable);
	public void setChannelWriter(IChannelWriter channelWriter);
	public boolean isAlived();
	public boolean isRotationResetEncoder();
	public boolean isResizeResetEncoder();
	
	public static interface ICommand{
		public void execute();
	}
}
