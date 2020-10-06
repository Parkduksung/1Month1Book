package com.rsupport.mobile.agent.modules.net.protocol;


//rcp50_cmd.h
public class MessageID {

    //Channel ID
    public static final int rcpChannelData = 0;                        // Data Channel
    public static final int rcpChannelScreen = 1;                    // Screen Channel
    public static final int rcpChannelSFTP = 2;                        // SFTP Channel
    public static final int rcpChannelRVS = 3;                        // RVS Channel
    public static final int rcpChannelPrint = 4;                    // Print Channel
    public static final int rcpChannelVoice = 5;                    // Voice Channel
    public static final int rcpChannelSound = 6;                    // Sound Channel
    public static final int rcpChannelDragDropSFTP = 7;                // Drag Drop SFTP Channel
    public static final int rcpWhiteBoardChannel = 8;                // WhiteBoard Channel
    public static final int rcpChannelRVScreen = 9;                    // Reverse Channel
    public static final int rcpChannelHXScreen = 14;                // H264 Channel

    public static final int rcpChannelSession = 99;                // Session Channel
    public static final int rcpChannelLast = 100;


    // #define sz_ChannelMsg		(5)
    public static final int sz_ChannelMsg = 5;

    // Communication type
    public static final int RCP_PROTOCOL_START = 200;
    public static final int PROTOCOL_COMMAND_START = RCP_PROTOCOL_START;

    // enum rcpPayloadType
    public static final int rcpChannel = PROTOCOL_COMMAND_START;
    public static final int rcpChannelNop = rcpChannel + 1;            // Channel Keeping Data
    public static final int rcpKeyMouseCtrl = rcpChannel + 2;            // Mouse & Keyboard control
    public static final int rcpLaserPointer = rcpChannel + 3;            // Laser Point
    public static final int rcpDraw = rcpChannel + 4;                    // Drawing
    public static final int rcpMonitors = rcpChannel + 5;                // Monitor information
    public static final int rcpFavorite = rcpChannel + 6;                // Favorite
    public static final int rcpSysInfo = rcpChannel + 7;                // system infoRV
    public static final int rcpProcess = rcpChannel + 8;                // process info
    public static final int rcpSFTP = rcpChannel + 9;                    // Simple FTP
    public static final int rcpDragDropFTP = rcpChannel + 10;            // Drag&Drop FTP
    public static final int rcpPrint = rcpChannel + 11;                // Remote Print
    public static final int rcpRebootConnect = rcpChannel + 12;        // Reboot Connect
    public static final int rcpSessionTransfer = rcpChannel + 13;        // Session Transfer
    public static final int rcpSessionShare = rcpChannel + 13;            // Session Share
    public static final int rcpReConnect = rcpChannel + 14;            // ReConnection
    public static final int rcpClipboard = rcpChannel + 15;            // Clipboard
    public static final int rcpWhiteBoard = rcpChannel + 16;            // Whiteboard
    public static final int rcpOption = rcpChannel + 17;                // Option
    public static final int rcpAnotherAccountReConnect = rcpChannel + 18;    // Reconnection to Another Account
    public static final int rcpRVScreenShow = rcpChannel + 19;            // Reverse Screen Showing
    public static final int rcpRemoteInfo = rcpChannel + 20;            // Remote Info
    public static final int rcpChat = rcpChannel + 21;                // Text Chat
    public static final int rcpScreenCtrl = rcpChannel + 22;            // Remote Screen Control
    public static final int rcpResolution = rcpChannel + 23;            // Resolution
    public static final int rcpSessionState = rcpChannel + 26;        // Session State of Remote PC
    public static final int rcpRemoteInfoRequest = 0;
    public static final int rcpRemoteInfoData = 1;
    public static final int rcpChannelClose = 8;
    public static final int rcpMobileFTPCancle = rcpChannel + 30;
    public static final int rcpMobile = rcpChannel + 31;
    public static final int rcpRecord = rcpChannel + 32;                // Record
    public static final int rcpScreenCaptur = rcpChannel + 35;
    public static final int rcpTerminalInfo = 239;

    public static final int rcpX264Stream = 242;

    public static final int rcpX264StreamStart = 0;
    public static final int rcpX264StreamStop = 1;
    public static final int rcpX264StreamHeader = 2;
    public static final int rcpX264StreamSPPS = 3;
    public static final int rcpX264StreamResume = 5;
    public static final int rcpX264StreamPause = 6;
    public static final int rcpX264StreamData = 8;


    public static final int rcpXENC264Stream = 243;

    public static final int rcpXENC264StreamStart = 0;
    public static final int rcpXENC264StreamStop = 1;
    public static final int rcpXENC264StreamData = 2;
    public static final int rcpXENC264StreamOption = 3;
    public static final int rcpXENC264StreamHeader = 4;


    /**
     * VD 를 사용하는 경우 화면 회전에 대한 처리.
     */
    public static final int rcpX264StreamReload = 10;

//	public static final int rcpPaloadTypeLast = rcpChannel+22;
//	public static final int PROTOCOL_COMMAND_LAST = rcpChannel+23;

    public static final int rcpScreenCaptureRequest = 0;
    public static final int rcpScreenCaptureConfirm = 1;
    public static final int rcpScreenCaptureReject = 2;

    public static final int rcpClipboardDataRequest = 0;
    public static final int rcpClipboardDataInfo = 1;
    public static final int rcpClipboardData = 2;
    public static final int rcpClipboardDataBegin = 3;
    public static final int rcpClipboardDataEnd = 4;

    public static final int Clipboard_None = 0;
    public static final int Clipboard_Text = 1;
    public static final int Clipboard_UnicodeText = 2;
    public static final int Clipboard_Bitmap = 3;

    public static final int rcpChannelListenRequest = 0;
    public static final int rcpChannelListenConfirm = 1;
    public static final int rcpChannelListenReject = 2;
    public static final int rcpChannelListenFail = 3;

    public static final int rcpChannelConnectRequest = 4;
    public static final int rcpChannelConnectConfirm = 5;
    public static final int rcpChannelConnectReject = 6;
    public static final int rcpChannelConnectFail = 7;

    public static final int rcpNopRequest = 0;
    public static final int rcpNopConfirm = 1;

    public static final int rcpRebootConnectResponse = 1;
    public static final int rcpKbdMouseControlAllow = 2;
    public static final int rcpAnotherAccountResponse = 3;
    public static final int rcpAnotherAccountReConnectInfo = 4;
    public static final int rcpRVScreenShowRequestAllow = 5;
    //---------------------------->>
    // rcpChannel


    // rcpKeyMouseCtrl
    // enum rcpKeyMouseCtrlMsgId
    public static final int rcpKeyMouseCtrlRequest = 0;            // Mouse/Keyboard Control Request
    public static final int rcpKeyMouseCtrlConfirm = 1;            // ���콺/Ű���� ��Ʈ�� ��û ����
    public static final int rcpKeyMouseCtrlReject = 2;            // ���콺/Ű���� ��Ʈ�� ��û �ź�
    public static final int rcpKeyMouseCtrlSuspend = 3;            // ���콺/Ű���� ��Ʈ�� �Ͻ�����
    public static final int rcpKeyMouseCtrlResume = 4;            // ���콺/Ű���� ��Ʈ�� �ٽ� ����
    public static final int rcpMouseTraceActive = 5;                // Host ���콺 ������ ��� Ȱ��ȭ
    public static final int rcpMouseTraceInActive = 6;            // Host ���콺 ������ ��� ��Ȱ��ȭ
    public static final int rcpKeyMouseCtrlInputBlock = 7;        // 화면잠금
    public static final int rcpKeyMouseCtrlInputBlockRelease = 8;    // 화면잠금
    public static final int rcpKeyMouseCtrlMsgId_Last = 20;
    //---------------------------->>

    public static final int rcpButton1Mask = 1;
    public static final int rcpButton2Mask = 2;
    public static final int rcpButton3Mask = 4;
    public static final int rcpButton4Mask = 8;
    public static final int rcpButton5Mask = 16;
    public static final int rcpWheelUpMask = rcpButton4Mask;
    public static final int rcpWheelDownMask = rcpButton5Mask;
    // public static final int sz_rcpMouseEventMsg =6;
    public static final int sz_rcpMouseEventMsg = 5;
    //---------------------------->>

    // rcpRecord
    public static final int rcpScreenRecordRequest = 2;
    public static final int rcpScreenRecordConfirm = 3;
    public static final int rcpScreenRecordReject = 4;
    //---------------------------->>

    // enum rcpKeyMouseEventId
    public static final int rcpKeyEvent = rcpKeyMouseCtrlMsgId_Last;
    public static final int rcpMouseEvent = rcpKeyMouseCtrlMsgId_Last + 1;
    public static final int rcpTouchEvent = rcpKeyMouseCtrlMsgId_Last + 2;
    public static final int rcpKeypadEvent = rcpKeyMouseCtrlMsgId_Last + 3;
    public static final int rcpMonkeyTouch = rcpKeyMouseCtrlMsgId_Last + 4;
    public static final int rcpMonkeyWheel = rcpKeyMouseCtrlMsgId_Last + 5;
    public static final int rcpMonkeyKeypad = rcpKeyMouseCtrlMsgId_Last + 6;
    public static final int rcpMonkeyString = rcpKeyMouseCtrlMsgId_Last + 7;
    public static final int rcpMonkeyKeylayout = rcpKeyMouseCtrlMsgId_Last + 8;
    //---------------------------->>

    // rcpLaserPointer
    // enum rcpLaserPointerMsgId
    public static final int rcpLaserPointerStart = 0;        // ������������ ���� (rcpMsg.data = rcpLaserPointerType, rcpMsg.data + 1 = POINTS)
    public static final int rcpLaserPointerEnd = 1;            // ������������ ���� (noting)
    public static final int rcpLaserPointerPos = 2;            // ������������ ��ǥ (rcpMsg.data = POINTS)
    //---------------------------->>

    // enum rcpLaserPointerType
    public static final int rcpLaserPointerArrow = 0; // ȭ��ǥ
    public static final int rcpLaserPointerCircle = 1; // ��
    //---------------------------->>

    // rcpDraw
    // enum rcpDrawMsgId
    public static final int rcpDrawStart = 0;
    public static final int rcpDrawEnd = 1;
    public static final int rcpDrawInfo = 2;
    public static final int rcpDrawData = 3;

    public static final int rcpDrawObjectType = 4;
    public static final int rcpDrawObjectColor = 5;
    public static final int rcpDrawObjectWidth = 6;

    public static final int rcpDrawClear = 7;

    public static final int rcpDrawLast = 30;

    //enum rcpDrawType
    public static final int rcpDrawFreeLine = 0;
    public static final int rcpDrawLine = 1;
    public static final int rcpDrawRectangle = 2;
    public static final int rcpDrawEllipse = 3;
    public static final int rcpDrawArrow = 4;

    // rcpFavorite
    // enum rcpFavoriteMsgId
    public static final int rcpFavoriteURL = 0;
    public static final int rcpFavoriteCP = 1;                    // Control panel
    public static final int rcpFavoriteFolder = 2;                // folder
    public static final int rcpFavoriteEX = 3;                    // ����
    public static final int rcpFavoriteHotkey = 4;                // hotkey
    public static final int rcpFavoriteMsgIdLast = 100;

    // 0-capture, 1-record start, 2-record stop
    public static final int rcpCaptureStart = 0;
    public static final int rcpRecordStart = 1;
    public static final int rcpRecordStop = 2;
    //---------------------------->>

    // enum rcpFavoriteCPMsgId
    public static final int rcpFavoriteCPListRequest = rcpFavoriteMsgIdLast; // ������ ��� �䱸(.cpl)
    public static final int rcpFavoriteCPList = rcpFavoriteCPListRequest + 1;         // ������ ���
    public static final int rcpFavoriteCPMsgIdLast = 150;
    //---------------------------->>

    // enum rcpFavoriteHotkeyMsgId
    public static final int rcpHotkeyCtrlAltDel = rcpFavoriteCPMsgIdLast; // Ctrl+Alt+Del
    public static final int rcpHotkeyFileSendDialog = rcpHotkeyCtrlAltDel + 1;
    public static final int rcpFavoriteHotkeyMsgIdLast = 200;

    // rcpSysInfo
    //enum rcpSysInfoMsgId
    public static final int rcpCpuMemInfoRequest = 0;
    public static final int rcpCpuMemInfo = 1;
    public static final int rcpSystemInfoRequest = 2;
    public static final int rcpSystemInfo = 3;

    // rcpProcess
    //enum rcpProcessMsgId
    public static final int rcpProcessListRequest = 0;
    public static final int rcpProcessList = 1;
    public static final int rcpProcessKillRequest = 2;

    // monkey input control message id
    public static final int rcpMonkeyTouch2Msg = 24;
    public static final int rcpMonkeyKeylayoutMsg = 26;

    // rcpSFTP
    // enum rcpSFTPMsgId
    public static final int rcpSFTPNone = -1;
    public static final int rcpSFTPRequest = 0;
    public static final int rcpSFTPConfirm = 1;
    public static final int rcpSFTPReject = 2;
    public static final int rcpSFTPStart = 3;
    public static final int rcpSFTPHeader = 4;
    public static final int rcpSFTPStartPos = 5;
    public static final int rcpSFTPNext = 6;
    public static final int rcpSFTPData = 7;
    public static final int rcpSFTPDataEnd = 8;
    public static final int rcpSFTPEnd = 9;
    public static final int rcpSFTPCancel = 10;
    public static final int rcpSFTPError = 11;
    public static final int rcpSFTPSendRequest = 12;
    public static final int rcprcpSFTPMsgId_Last = 13;

    public static final int rcpExpPath = 31;
    public static final int rcpExpPathList = 32;
    public static final int rcpExpPathListEnd = 33;
    public static final int rcpExpFileDelete = 34;
    public static final int rcpExpFileDeleteEnd = 35;
    public static final int rcpExpFileExecute = 36;
    public static final int rcpExpDownloadStart = 37;
    public static final int rcpExpUploadStart = 38;

    public static final int rcpExpFileRequest = 41;
    public static final int rcpExpFTPReceiveFileHeader = 42;
    public static final int rcpExpFTPData = 43;
    public static final int rcpExpFTPFileDataEnd = 44;
    public static final int rcpExpFTPSendEnd = 45;
    public static final int rcpExpFTPFilePos = 46;
    public static final int rcpExpFTPSendFileHeader = 47;
    public static final int rcpExpFTPDiskFreeSpace = 48;
    public static final int rcpExpFTPCancel = 49;
    public static final int rcpExpFTPStart = 50;
    public static final int rcpExpFTPGetEnd = 51;
    public static final int rcpExpFTPOpt = 52;
    public static final int rcpExpFTPSendExistFileHeader = 53;

    public static final int rcpExpSeclectedFileDiskCheck = 60;
    public static final int rcpExpSeclectedFileDiskCheck_OK = 61;
    public static final int rcpExpSeclectedFileDiskCheck_FAIL = 62;

    public static final int rcpExpFolderInfomationRequest = 70;
    public static final int rcpExpFolderInfomation = 71;

    public static final int rcpPreviewFileHeader = 231;

    public static final int rcpExpDownConfirmOK = 52;
    public static final int rcpExpDownConfirmCancel = 53;
    public static final int rcpExpUpConfirmOK = 54;
    public static final int rcpExpUpConfirmCancel = 55;
    public static final int rcpExpDelConfirmOK = 56;
    public static final int rcpExpDelConfirmCancel = 57;

    // rcpDragDropFTP
    // enum rcpDragDropFTPMsgId
    public static final int rcpDragDropFTP_Data = rcprcpSFTPMsgId_Last;
    public static final int rcpDragDropFTP_DND_LEAVE_LBTN = rcpDragDropFTP_Data + 1;
    public static final int rcpDragDropFTP_CNP_TARGET = rcpDragDropFTP_Data + 2;
    public static final int rcpDragDropFTP_CNP_CLEAR = rcpDragDropFTP_Data + 3;
    //---------------------------->>

    // rcpPrint
    // enum rcprcpPrintMsgId
    // rcpPrint
    public static final int rcpPrintEvent = 0;                        // ���� ��� ���� Event
    public static final int rcpPrintStart = 1;                    // ���� ��� ���� (rcpPrintInfoMsg)
    public static final int rcpPrintDocInfo = 2;                    // ���� ���� ����Ÿ (.prd ���� ���)
    public static final int rcpPrintStartDoc = 3;                    // ���� ����
    public static final int rcpPrintStartPage = 4;                // ���� ������ ���� (rcpPrintPageInfo)
    public static final int rcpPrintPageData = 5;                    // ���� ������ ����Ÿ
    public static final int rcpPrintEndPage = 6;                    // ���� ������ �� (�� command�� ���� �� file print)
    public static final int rcpPrintEnd = 7;                        // ���� ��� ��
    public static final int rcpPrintCancel = 8;                        // ���
    public static final int rcprcpPrintMsgId_Last = 9;
    //---------------------------->>

    public static final int sz_rcpPacket = 5;
    public static final int sz_rcpMessage = 1;
    public static final int sz_rcpDataMessage = 5;
    public static final int sz_rcpZipHeader = 8; //sizeof(rcpZipHeader)


    // KeyMouseControlState
    public static final int KeyMouseControl_None = 0x00;
    public static final int KeyMouseControl_Keyboard = 0x01; // Ű���� ����(0x01)
    public static final int KeyMouseControl_Mouse = 0x02; // ���콺 ����(0x02)
    public static final int KeyMouseControl_Suspend = 0x04; // Ű����/���콺 �Ͻ�����

    // rcpSpecialKeyStateType
    public static final int rcpKeyCapsLock = 0x01;
    public static final int rcpKeyNumLock = 0x02;
    public static final int rcpKeyScrollLock = 0x04;
    public static final int rcpKeyShift = 0x08;

    public static final int szrcpSFTPDataSize = 1024 * 1000;

    public static final int rcpChatOpen = 0;
    public static final int rcpChatClose = 1;
    public static final int rcpChatInput = 2;

    public static final int rcpDrawInteractiveStart = 30;
    public static final int rcpDrawInteractiveEnd = 31;

    public static final int rcpOption_rcOption = 0;            // _Function + _Opt + _SF_MaxFileSize
    public static final int rcpOption_Funtion = 1;            // ���
    public static final int rcpOption_Opt = 2;                // �ɼ�
    public static final int rcpOption_SF_MaxFileSize = 3;    // ���� ��۽� ���� �뷮
    public static final int rcpOption_SCap = 4;                // SCAP

    public static final int rcpSessionShareRequest = 0;
    public static final int rcpSessionShareConfirm = 1;
    public static final int rcpSessionShareReject = 2;
    public static final int rcpSessionShareInfo = 3;
    public static final int rcpSessionShareConnected = 4;
    public static final int rcpSessionShareDisconnected = 5;

    public static final int rcpSessionTransferRequest = 6;
    public static final int rcpSessionTransferConfirm = 7;
    public static final int rcpSessionTransferReject = 8;
    public static final int rcpSessionTransferInfo = 9;
    public static final int rcpSessionTransferConnected = 10;
    public static final int rcpSessionTransferDisconnected = 11;

    //rcpMonitors
    public static final int rcpMonitorsInfoRequest = 0;
    public static final int rcpMonitorsInfoResponse = 1;
    public static final int rcpMonitorSelect = 2;

    //rcpResolution
    public static final int rcpResolutionCurrentMode = 0;            // ���� �ػ�
    public static final int rcpResolutionEnumMode = 1;                // ���� ������ �ý��� �ػ�
    public static final int rcpResolutionChange = 2;                // ����� �ػ� ����

    //rcpRebootConnect
    public static final int rcpRebootConnectRequest = 0;            // ����� ���� ��û
    public static final int rcpRebootConnectConfirm = 1;            // ����� ���� ����
    public static final int rcpRebootConnectReject = 2;                // ����� ���� ����
    public static final int rcpRebootConnectInfo = 3;                // ����� ���� ����
    public static final int rcpRebootConnectFail = 4;                // ����� ���� ����
    public static final int rcpRebootSafemodeConnectRequest = 5;    // ������ ����� ���� ��û
    public static final int rcpRebootSafemodeConnectInfo = 6;        // ������ ����� ���� ����
    public static final int rcpRebootSafemodeConnectConfirm = 7;    // ������ ����� ���� ����
    public static final int rcpRebootSafemodeConnectReject = 8;        // ������ ����� ���� ����
    public static final int rcpRebootSafemodeConnectFail = 9;        // ������ ����� ���� ����

    //rcpAnotherAccountReConnect
    public static final int rcpReConnectRequest = 0;
    public static final int rcpReConnectConfirm = 1;
    public static final int rcpReConnectReject = 2;
    public static final int rcpReConnectInfo = 3;
    public static final int rcpReConnectFail = 4;

    //rcpTerminalInfoMsgId
    public static final int rcpTerminalInfoRequest = 0;  // Viewer -> Host
    public static final int rcpTerminalInfoResponse = 1; // Host -> Viewer

    //rcpPlatformType
    public static class RcpPlatformType {
        public static final short Platform_Windows = 1;
        public static final short Platform_Mac = 2;
        public static final short Platform_Linux = 3;
        public static final short Platform_Android = 4;
        public static final short Platform_Iphone = 5;
    }

    //rcpStandbyType
    public static class RcpStandbyType {
        public static final short StandBy_PC = 0; //뷰어 대기 (PC), Platform_Windows, Platform_Mac, Platform_Linux
        public static final short StandBy_MP = 1; //모바일 대기, Platform_Android, Platform_Iphone
        public static final short StandBy_VP = 2; //화상 대기, Platform_Android, Platform_Iphone
        public static final short StandBy_RViewMP = 3;  // RemoteView 모바일 대기, Platform_Android, Platform_Iphone
    }

    /*
     * RemoteView Code Start
     */
    //rcpScreenCtrl
    public static final int rcpScreenBlankStart = 50;
    public static final int rcpScreenBlankEnd = 51;
    /*
     * RemoteView Code End
     */

    /*
     * rcpMobile
     */
    //System 정보
    public static final int rcpMobileSystemInfoRequest = 0;
    public static final int rcpMobileSystemInfo = 1;
    public static final int rcpMobileSystemInfoWIFIInfo = 2;    //System정보텝 상세정보
    public static final int rcpMobileSystemInfoBatteryInfo = 3;
    public static final int rcpMobileSystemInfoMemoryInfo = 4;
    public static final int rcpMobileSystemInfoAPInfo = 5;
    public static final int rcpMobileSystemClipboardInfo = 6;
    public static final int rcpMobileSystemQuickSettings = 7;

    //Process정보
    public static final int rcpMobileProcessInfoRequest = 10;
    public static final int rcpMobileProcessInfo = 11;
    public static final int rcpMobileProcessKill = 12;
    public static final int rcpMobileProcessInfoEnd = 13;
    public static final int rcpMobileProcessKillSuccess = 14;
    public static final int rcpMobileProcessKillFail = 15;
    public static final int rcpMobileProcessChartInfo = 16;
    public static final int rcpMobileMemoryClipboardInfo = 17;
    public static final int rcpMobileProcessClipboardnfo = 18;

    //App Info[1]
    public static final int rcpMobileApplicationInfoRequest = 20;
    public static final int rcpMobileApplicationInfo = 21;
    public static final int rcpMobileApplicationRemove = 22;
    public static final int rcpMobileApplicationRun = 23;
    public static final int rcpMobileApplicationInfoEnd = 24;
    public static final int rcpMobileApplicationRemoveDone = 25;
    public static final int rcpMobileApplicationRunSuccess = 26;
    public static final int rcpMobileApplicationRunFail = 27;
    public static final int rcpMobileApplicationDetailInfo = 28;
    public static final int rcpMobileApplicationClipboardInfo = 29;

    //Log정보
    public static final int rcpMobileLogInfoRequest = 30;
    public static final int rcpMobileLogInfo = 31;
    public static final int rcpMobileLogInfoFail = 32;
    public static final int rcpMobileLogInfoEnd = 33;

    //Message전송
    public static final int rcpMobileMessageRequest = 40;
    public static final int rcpMobileMessageChatRequest = 41;
    public static final int rcpMobileMessageLogRequest = 42;

    // capture, record start, record stop
    public static final int rcpMobileMessageHelper = 43;

    //Camera ON, OFF
    public static final int rcpMobileCameraRequest = 50;
    public static final int rcpMobileCameraKill = 51;

    //OverlayView visible & invisible
    public static final int rcpMobileShowSupport = 55;
    public static final int rcpMobileHideSupport = 56;

    //고객(Mobile)이 상담원에게 전화요청
    public static final int rcpMobilePhoneRequest = 60;

//	//음소거
//	public static final int rcpMobileMuteOnRequest = 70;
//	public static final int rcpMobileMuteOned = 71;
//	public static final int rcpMobileMuteOffRequest = 72;
//	public static final int rcpMobileMuteOffed = 73;

    //Speaker Phone
    public static final int rcpMobileSpeekerPhoneOnRequest = 70;
    public static final int rcpMobileSpeekerPhoneOnResponse = 71;
    public static final int rcpMobileSpeekerPhoneOffRequest = 72;
    public static final int rcpMobileSpeekerPhoneOffResponse = 73;

    //설정창 이동
    public static final int rcpMobileConfig = 80;

    //상태바 제어
    public static final int rcpMobileNotiBarDownRequest = 85;
    public static final int rcpMobileNotiBarUpRequest = 86;

    //자동화면회전
    public static final int rcpMobileRotateLockRequest = 90;    // 회전 금지 요청
    public static final int rcpMobileRotateLocked = 91;            // 회전 금지 되었음
    public static final int rcpMobileRotateReleaseRequest = 92;    // 회전 허용 요청
    public static final int rcpMobileRotateReleased = 93;        // 회전 허용 되었음

    //Network 상태정보
    public static final int rcpMobileWIFIResponse = 100;
    public static final int rcpMobile3GResponse = 101;
    public static final int rcpMobileUSBResponse = 102;

    //그리기 - 안그려지는 Rect 정보
    public static final int rcpMobileDrawNonRect = 110;

    public static final int rcpMobileScreenSupport = 120;                    // 정상적으로 스크린 채널을 연결 할 수 있는 시점에 알려줘야 한다.
    public static final int rcpMobileScreenNotSupport = 121;            // 스크린 채널을 맺을 수 없는 상태 APK 가 지원이 안되는 경우
    public static final int rcpMobileKeyMouseSupport = 122;                // key-mouse control ok
    public static final int rcpMobileKeyMouseNotSupport = 123;            // key-mouse control not support

    public static final int rcpMobileUpdate = 130;                    // apk 업데이트 받을때 초기에 알림

    public static final int rcpMobileUpdate_ScreenWait = 140;       // Screen apk 업데이트 대기중
    public static final int rcpMobileUpdate_Screen = 141;           // Screen apk 업데이트 진행중
    public static final int rcpMobileUpdate_ScreenComplete = 142;   // Screen apk 업데이트 완료

    public static final int rcpMobileUpdate_InputWait = 150;        // Input apk 업데이트 대기중
    public static final int rcpMobileUpdate_Input = 151;            // Input apk 업데이트 진행중
    public static final int rcpMobileUpdate_InputComplete = 152;    // Input apk 업데이트 완료

    public static final int rcpMobileKeyMap = 155;                    // for keymap
    public static final int rcpMobileInputMethod = 157;
    public static final int rcpMobileInformation = 160;                // Skin Information
    public static final int rcpMobileINIT = 161;                    // Mobile Init Information
    public static final int rcpKT_INIT = 162;                        // Mobile Init Information (KT)
    public static final int rcpMobileAppOpt = 163;

    //RC Viewer <-> USB Viewer
    public static final int rcpMobileUSBStart = 170;
    public static final int rcpMobileUSBStop = 171;
    public static final int rcpMobileUSBStatus = 172;

    //App Info[2] (Screen Lock)
    public static final int rcpMobileApplicationRunReceive = 180;
    public static final int rcpMobileApplicationRunOK = 181;
    public static final int rcpMobileApplicationRunCancel = 182;

    //Screen Lock
    public static final int rcpMobilePauseRequest = 190;                // 뷰어가 단말로 먼저 화면 잠금  요청 했을때
    public static final int rcpMobilePauseRespose = 191;                // 단말에서 뷰어로 먼저 화면 잠금 요청 했을때
    public static final int rcpMobileUnPauseRequest = 192;              // 뷰어가 단말로 화면 잠금 해지 요청 했을 때
    public static final int rcpMobileUnPauseResposeAgree = 193;         // 단말에서 해지 요청 수락 했을 때
    public static final int rcpMobileUnPauseResposeReject = 194;        // 단말에서 해지 요청 거절 했을 때

    public static final int rcpMobileScreenChannelStart = 195;            // 화면전송 스타트 버튼 누를시.
    public static final int rcpMobileVersion = 200;                        //Protocol Version (start 1)
    public static final int rcpMobileEngineVersion = 201;

    // Mobile FTP
//	public static final int rcpMobileFTPPath = 201;
    public static final int rcpMobileFTPFileList = 202;
    public static final int rcpMobileFTPEnd = 205;

    public static final int rcpMobileUnControllable = 206;
    public static final int rcpMobileControllable = 207;

    public static final int rcpMobileScreenOn = 208;
    public static final int rcpMobileScreenOff = 209;

    public static final short rcpQuickSettingSet = 215;    // set settings
    public static final short rcpQuickSettingGet = 216;    // get settings
    public static final short rcpQuickSettingOpenMenu = 217;    // open settings menu

    public static final short rcpMobileHome = 218;
    public static final short rcpMobileQuickSettingHome = 219;


    // send screen name ON/OFF
    public static final int rcpMobileActivityAppRequestOn = 225;
    public static final int rcpMobileActivityAppRequestOFF = 226;
    public static final int rcpMobileActivityApp = 229;
    public static final int rcpMobileIPAddress = 230; //FTP와 겹침

    //Samsung Printer Only
    public static final int rcpMobile_SamsungPrinterServiceMode = 238;


    //simchange
    public static final int rcpMobileDualSimChange = 61;

    //Recent APP
    public static final int rcpMobileRecentApp = 62;

    public static final int rcpMobileOpenScreenRequest = 65;
    public static final int rcpMobileOpenScreenAck = 66;
    public static final int rcpMobileOpenScreenNak = 67;


}

