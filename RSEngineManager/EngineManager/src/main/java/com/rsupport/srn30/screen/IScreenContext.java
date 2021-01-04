package com.rsupport.srn30.screen;

import java.io.FileDescriptor;

import android.content.Context;

public interface IScreenContext {
	public int bindEngine(Context context, String packageName, int boostMode);
	public void unBindEngine();
	
	public String screenShot();
	public boolean screenShot(String filePath);

	public boolean start(FileDescriptor sock, int channelId);
}
