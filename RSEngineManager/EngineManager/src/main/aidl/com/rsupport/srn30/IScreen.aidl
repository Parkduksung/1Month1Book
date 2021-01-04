package com.rsupport.srn30;

import com.rsupport.srn30.IScreenCallback;

interface IScreen {
    void registerCallback(IScreenCallback cb);
    
    boolean start(String rsperm, int bindTimeout, in ParcelFileDescriptor sock, int channelId, int permissionPriority);
    void	enableUpdate(boolean enable);
    
    boolean command(String cmd);
    
    String screenShot();
    boolean screenShotToPath(String filePath);

    int capture(int width, int height);
    boolean bindRsperm(String rsperm, int boostMode, int bindTimeout);
    int getBindedType();
    
    boolean setMaxLayer(int maxLayer);
    int createAshmem(int width, int height, int ashmType);
    int readBytes(inout byte[] buffer, int srcOffset, int destOffset, int count);
    void releaseAshmem();
    
	boolean createVirtualDisplay(String name, int w, int h, int dpi, in Surface surf, int flag);
    boolean releaseVirtualDisplay();
    
    int	getFlag();
    
    int	getCaptureType();
    
    boolean	test(String args);

    /* Android Q Audio Capture Option Add */
    void createAudio();

    boolean initAudio(int sampleRate, int channelConfig, int audioFormat);

	boolean startAudio();

	void resumeAudio();

	byte[] readAudio(inout byte[] buffer, int size);

	void pauseAudio();

	void muteAudio(boolean isMute);

	void releaseAudio();

}