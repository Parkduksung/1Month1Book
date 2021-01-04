package com.rsupport.srn30.screen.capture;

import android.view.Surface;

public interface IVirtualDisplay {
	public boolean createVirtualDisplay(String name, int w, int h, int dpi, Surface surface, int flag);
	public boolean release();
}
