package com.rsupport.srn30.screen.channel;

public class scap {

	// enum scapMsgId // host -> viewer
	public final static int scapNull = 0;

	// prevent timeout socket close.
	public final static int scapChannelNop = 1;

	// screen capture engine state notification, inputs
	public final static int scapNotify = 2;
	public final static int scapOption = 3;
	public final static int scapOption2 = 4;
	public final static int scapMouseInput = 5;
	public final static int scapKeyInput = 6;
	public final static int scapCursorPos = 7;
	public final static int scapCursorCached = 8;
	public final static int scapCursorNew = 9;

	// appshare
	public final static int scapInitDesktop = 10; // 10
	public final static int scapColorMap = 11;
	public final static int scapSrnRgn = 12;
	public final static int scapEnc = 13;

	public final static int scapInitChannel = 14;

	// simple conference [3/31/2010 objects]
	public final static int scapSyncState = 15;
	public final static int scapNotify2Dec = 16; // internal state.

	// android [9/24/2010 objects]
	public final static int scapBulk = 17; // [6/2/2011 objects]

	public final static int scapVersion = 18;
	public final static int scapExtension = 19;
	public final static int scapMobile3DAnswer = 20;
	public final static int scapRotation = 21; // 2013.09.03
	public final static int scapNop = 22; // 2013.10.11(Mobile to Viewer)

	// update screen: expands with scapUpdateid
	public final static int scapUpdate = 100; // = 100
	public final static int scapUpdateRequest = 101;

	public final static int scapMsgIdEnd = 102;

}
