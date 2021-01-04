package com.rsupport.srn30.screen.channel;

import java.nio.ByteBuffer;

public interface IChannelWriter {
	public boolean write(byte[] buffer, int offset, int count) throws Exception;
	public boolean write(ByteBuffer imageBuffer) throws Exception;
	public boolean writeAshmem(long ashmemAddress) throws Exception;
}
