package com.rsupport.jarinput;


public interface IMonkeyHandler {
	public void handle(int action, int i1, int i2, int i3, int i4, int i5);
	public void handle(byte[] bb, int offset, int len);
}
