package com.rsupport.srn30;

import com.rsupport.srn30.IScreenCallback;

interface IScreen {
    void registerCallback(IScreenCallback cb);
    
    boolean start(String rsperm, int bindTimeout, in ParcelFileDescriptor sock, int channelId);
    void	enableUpdate(boolean enable);
    
    boolean command(String cmd);
    
    boolean	test(String args);
}