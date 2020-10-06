package com.rsupport.media.mediaprojection;

import android.view.Surface;

/**
 * Created by taehwan on 5/13/15.
 */
public interface IVirtualDisplay {

    /**
     * VirtualDisplay를 초기화 한다.
     */
    boolean createVirtualDisplay(String name, int width, int height, int dpi, Surface surface, int flags);

    /**
     * VirtualDisplay를 release.
     */
    boolean release();
}
