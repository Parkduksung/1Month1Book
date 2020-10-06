package com.rsupport.mobile.agent.modules.channel.screen;

public class ChannelConstants {


    // enum rcpPayloadType
    public static final int rcpChannel = 200;
    public static final int rcpChannelNop = rcpChannel + 1;

    public static final int rcpX264Stream = 242;

    public static final int rcpX264StreamHeader = 2;
    public static final int rcpX264StreamHeaderRec = 100;
    public static final int rcpX264StreamSPPS = 102;
    public static final int rcpX264StreamData = 103;


    public static final int rcpNopRequest = 0;
    public static final int rcpNopConfirmNoAck = 2;

    //---------------------------->>

    public static final int sz_rcpPacket = 5;
    public static final int sz_rcpMessage = 1;
    public static final int sz_rcpDataMessage = 5;

}
