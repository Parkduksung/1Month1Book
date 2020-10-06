package com.rsupport.media.mediaprojection;

/**
 * Created by taehwan on 5/13/15.
 */
public interface IScreenCapturable {

    public static final int STATE_CONTINUE = 0;
    public static final int STATE_NEXT = 1;
    public static final int STAE_BREAK = 2;

    public Object initialized() throws Exception;

    public void close();
}
