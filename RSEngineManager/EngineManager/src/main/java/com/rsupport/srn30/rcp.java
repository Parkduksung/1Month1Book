package com.rsupport.srn30;

public class rcp {
	// enum rcpPayloadType
	public final static int rcpChannel = 200;// PROTOCOL_COMMAND_START
	public final static int rcpChannelNop = 201;// 채널 유지 데이타
	public final static int rcpKeyMouseCtrl = 202;// 마우스 키보드 제어
	public final static int rcpLaserPointer = 203;// 레이저 포인터
	public final static int rcpDraw = 204;// 그리기
	public final static int rcpMonitors = 205;// 모니터 정보
	public final static int rcpFavorite = 206;// 즐겨찾기
	public final static int rcpSysInfo = 207;// system info
	public final static int rcpProcess = 208;// process info
	public final static int rcpSFTP = 209;// 파일 전송
	public final static int rcpDragDropFTP = 210;// Drag&Drop 파일 전송
	public final static int rcpPrint = 211;// 원격 프린트
	public final static int rcpRebootConnect = 212;// 원격 재부팅후 접속
	public final static int rcpSessionShare = 213;// 세션 공유
	public final static int rcpReConnect = 214;// 재연결
	public final static int rcpClipboard = 215;// 클립보드
	public final static int rcpWhiteBoard = 216;// 화이트보드
	public final static int rcpOption = 217;// 옵션
	public final static int rcpAnotherAccountReConnect = 218;// 원격 PC의 다른 계정으로
																// 연결
	public final static int rcpRVScreenShow = 219; // 역화면 보기
	public final static int rcpRemoteInfo = 220;// 상대방 정보
	public final static int rcpChat = 221; // 채팅
	public final static int rcpScreenCtrl = 222;// 리모트 화면 제어 명령어
	public final static int rcpResolution = 223;// 해상도 변경 관련
	public final static int rcpAppShare = 224; // 프로그램 공유
	public final static int rcpSoundShare = 225;// 소리공유
	public final static int rcpSessionState = 226; // 원격 PC의 세션 상태
	public final static int rcpDateTime = 227; // 시간관련처리
	public final static int rcpSystem = 228;// 시스템 명령 관련 처리
	public final static int rcpNetworkZone = 229;// 3G/Wifi network state.
	public final static int rcpHostState_deprecated = 230; // 모바일 상태 - kjj
	public final static int rcpMobile_deprecated = 231; // 모바일 Payload - kjj
	public final static int rcpScreenshot = 232;// 모바일 Payload - kjj
	public final static int rcpScreenRecord = 233; // 모바일 Payload - kjj

	// enum rcpScreenCtrlMsgId
	public final static int rcpScreenSuspend = 0; // 화면 전달 일시 중지
	public final static int rcpScreenResume = 1; // 화면 전달 다시 시작
	public final static int rcpScreenMinimize = 2; // 최소화
	public final static int rcpScreenMaximize = 3; // 최대화
	public final static int rcpScreenPresentation = 4; // 전체화면
	public final static int rcpScreenAlwaysOnTop = 5; // 항상위
	public final static int rcpScreenAutoScroll = 6; // 자동 스크롤
	public final static int rcpScreenRefresh = 7; // 화면 갱신 (원하는 영역 전달 RECT)
	public final static int rcpScreenSessionChanged = 8;
	public final int rcpSessionChanged = 1; // data[0] 고객이 제한된 계정일 경우만 받음 (DDI
											// 모드 제외)
	public final int rcpDesktopChanged = 2; // data[0] 고객이 제한된 계정일 경우만 받음
	public final int rcpMenuLockReqeust = 3; // data[0] 고객이 제한된 계정이고, DDI 모드 일
												// 경우 [2009/7/17 jhjang]

	public final static int rcpScreenStart = 9;
	public final static int rcpScreenStop = 10;

	public final static int rcpScreenBlankStart = 50; // 원격 화면 잠금 시작
	public final static int rcpScreenBlankEnd = 51; // 원격 화면 잠금 종료
	public final static int rcpScreenBlankStatus = 52; // 원격 화면 잠금 상태 if data[0]
														// is '1', became blank.
														// '0' release blank.
	// enum rcpOptionMsgId
	public final static int rcpOption_rcOption = 0;			// _Function + _Opt + _SF_MaxFileSize
	public final static int rcpOption_Funtion = 1;			// 기능
	public final static int rcpOption_Opt = 2;				// 옵션
	public final static int rcpOption_SF_MaxFileSize = 3;	// 파일 전송시 제한 용량
	public final static int rcpOption_SCap = 4;				// SCAP : = 4 : 
	
	//rcpChannelMsgId;
	public final static int rcpChannelListenRequest = 0;			// channel listen 요청 - 0
	public final static int rcpChannelListenConfirm = 1;			// channel listen 수락 
	public final static int rcpChannelListenReject = 2;				// channel listen 거절
	public final static int rcpChannelListenFail = 3;				// channel listen 실패 - 3
	public final static int rcpChannelConnectRequest = 4;			// 대기하고 있는 channel로 connect 요청 = 4
	public final static int rcpChannelConnectConfirm = 5;			// 대기하고 있는 channel로 connect 수락 = 5
	public final static int rcpChannelConnectReject = 6;			// 대기하고 있는 channel로 connect 거절
	public final static int rcpChannelConnectFail = 7;
	public final static int rcpChannelClose = 8;
	public final static int rcpChannelNetSpeedRequest = 9;			// Net Speed 요청
	public final static int rcpChannelNetSpeedResponse = 10;			// Net Speed 응답
	public final static int rcpChannelIPInfo = 11;					// mobizen2web - pass mobile ip/port info to viewer(web-broswer) to try p2p connect, KJJ = 11
}
