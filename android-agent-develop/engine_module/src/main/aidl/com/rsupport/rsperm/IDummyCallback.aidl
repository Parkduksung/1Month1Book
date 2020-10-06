package com.rsupport.rsperm;

interface IDummyCallback
{
	oneway void onEvent(in byte[] data);
	int	getInt(in byte[] data);
}
