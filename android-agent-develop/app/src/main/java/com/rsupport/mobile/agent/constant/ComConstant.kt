package com.rsupport.mobile.agent.constant

public class ComConstant {

    companion object {
        //Agent_expired_check
        const val AGENT_OK = "0" //Available
        const val AGENT_EXPIRED = "1" //Expiration of service life
        const val AGENT_OVER = "2" //Exceeded Agent number


        //PC_Type
        const val RVFLAG_KEY_NONE: Short = 0x00 //0
        const val RVFLAG_KEY_VPRO: Short = 0x01 //1, vPro
        const val RVFLAG_KEY_RWT: Short = 0x02 //2, RWT
        const val RVFLAG_KEY_HVS: Short = 0x10 //16, HyperV Server
        const val RVFLAG_KEY_HVI: Short = 0x11 //17, HyperV Image
        const val RVFLAG_KEY_VMI: Short = 0x12 //18, VM Image
        const val RVFLAG_KEY_VHDI: Short = 0x13 //19, VirtualPC Image
        const val RVFLAG_KEY_VBOX: Short = 0x14 //20, VBox Image
        const val RVFLAG_KEY_XGEN: Short = 0x15 //21, XGen Image
        const val RVFLAG_KEY_UBUN: Short = 0x30 //48, Linux - Ubuntu
        const val RVFLAG_KEY_KVM: Short = 0x32 //50  KVM
        const val RVFLAG_KEY_REMOTEWOL: Short = 0x33 //51, Remote WOL
        const val RVFLAG_KEY_HWWOL: Short = 0x34 //52, HW WOL
        const val RVFLAG_KEY_MAC: Short = 0x40 //64, MacOS X
        const val RVFLAG_KEY_ANDROID: Short = 0x50 //80, MacOS X

        //vPro_AMT_Power_Status
        const val VPRO_AMT_POWER_RESET: Short = 0x10 //16
        const val VPRO_AMT_POWER_ON: Short = 0x11 //17
        const val VPRO_AMT_POWER_OFF: Short = 0X12 //18

        //vPro_AMT_Error_case
        const val AMTERR_NONE = 0
        const val AMTERR_NOTACCESS = 1
        const val AMTERR_INVALIDAUTH = 2

        //RView Server Privilege Values(Season 1)
        const val RVPOPTS_RCONTROL: Long = 0x0001
        const val RVPOPTS_REXPLORER: Long = 0x0002
        const val RVPOPTS_RSCAPTURE: Long = 0x0004
        const val RVPOPTS_RPROCESS: Long = 0x0008
        const val RVPOPTS_RSYSTEM: Long = 0x0010
        const val RVPOPTS_RVPN: Long = 0x0020 //         - 32
        const val RVPOPTS_RVPRO: Long = 0x0040 //         - 64

        const val RVPOPTS_RV_ALL: Long = 0xFFFF

        //RView Server Privilege Values(Season 2)
        const val RVPOPTS2_RCONTROL: Long = 0x00010000
        const val RVPOPTS2_RCONTROL_FILE: Long = 0x00000001
        const val RVPOPTS2_RCONTROL_CAPTURE: Long = 0x00000002
        const val RVPOPTS2_RCONTROL_RPRINT: Long = 0x00000004
        const val RVPOPTS2_RCONTROL_CAM: Long = 0x00000008
        const val RVPORTS2_RCONTROL_CLIP: Long = 0x00000010 //클립보드 권한

        const val RVPOPTS2_RCONTROL_ALL: Long = 0x000100FF //원격제어 모든 권한 & 라이센스 권한

        const val RVPOPTS2_REXPLORER: Long = 0x00020000
        const val RVPOPTS2_EXTEND_IVPN: Long = 0x01000000
        const val RVPOPTS2_EXTEND_VPRO: Long = 0x02000000
        const val RVPOPTS2_EXTEND_MOBILE: Long = 0x04000000
        const val RVPOPTS2_EXTEND_VIRTUAL: Long = 0x08000000
        const val RVPORTS2_EXTEND_LIVEVIEW: Long = 0x10000000 //라이브뷰 라이선스
        const val RVPOPTS2_ALL: Long = -0x1

        const val WEB_ERR: Short = 0
        const val WEB_ERR_NO = (WEB_ERR + 110.toShort()).toShort()
        const val WEB_ERR_INVALID_PARAMETER = (WEB_ERR + 111.toShort()).toShort()
        const val WEB_ERR_NOT_FOUND_USERID = (WEB_ERR + 112.toShort()).toShort()
        const val WEB_ERR_NOT_FOUND_AGENTID = (WEB_ERR + 113.toShort()).toShort()
        const val WEB_ERR_INVALID_USER_ACCOUNT = (WEB_ERR + 114.toShort()).toShort()
        const val WEB_ERR_ALREADY_USINGSESSION = (WEB_ERR + 115.toShort()).toShort()
        const val WEB_ERR_BLOCK_MOBILELOGIN = (WEB_ERR + 116.toShort()).toShort()
        const val WEB_ERR_INVITE_EXPIRED = (WEB_ERR + 120.toShort()).toShort()
        const val WEB_ERR_INVITE_ALREADY = (WEB_ERR + 121.toShort()).toShort()
        const val WEB_ERR_APP_VERSION = (WEB_ERR + 130.toShort()).toShort()
        const val WEB_ERR_INVAILD_COMPANYID = (WEB_ERR + 131.toShort()).toShort()
        const val WEB_ERR_NEED_SWITCH_MEMBER = (WEB_ERR + 132.toShort()).toShort()
        const val WEB_ERR_NEED_UPGRADE_MEMBER = (WEB_ERR + 133.toShort()).toShort()
        const val WEB_ERR_AES_NOT_FOUND_USERID = (WEB_ERR + 140.toShort()).toShort()
        const val WEB_ERR_AES_INVALID_USER_ACCOUNT = (WEB_ERR + 141.toShort()).toShort()
        const val WEB_ERR_USER_ACCOUNT_LOCK = (WEB_ERR + 142.toShort()).toShort()
        const val WEB_ERR_OTP_AUTH_FAIL = (WEB_ERR + 145.toShort()).toShort()
        const val WEB_ERR_WOL_NOT_FOUND_AGENT = (WEB_ERR + 146.toShort()).toShort()
        const val WEB_ERR_ARP_NOT_FOUND_AGENT = (WEB_ERR + 147.toShort()).toShort()
        const val WEB_ERR_ALREADY_SAME_WORKING = (WEB_ERR + 211.toShort()).toShort()
        const val WEB_ERR_ALREADY_DELETE_AGENTID = (WEB_ERR + 212.toShort()).toShort()
        const val WEB_ERR_AGENT_NOT_LOGIN = (WEB_ERR + 213.toShort()).toShort()
        const val WEB_ERR_ONLY_WEBSETUP = (WEB_ERR + 214.toShort()).toShort()
        const val WEB_ERR_AGENT_EXPIRED = (WEB_ERR + 215.toShort()).toShort()
        const val WEB_ERR_INVALID_MACADDRESS = (WEB_ERR + 300.toShort()).toShort()
        const val WEB_ERR_INVALID_LOCALIP = (WEB_ERR + 301.toShort()).toShort()
        const val WEB_ERR_INVALID_ROLE = (WEB_ERR + 310.toShort()).toShort()
        const val WEB_ERR_LIC_EXPIRED = (WEB_ERR + 405.toShort()).toShort()
        const val WEB_ERR_LIC_SERVICE_ERROR = (WEB_ERR + 406.toShort()).toShort()
        const val WEB_ERR_ACTIVE = (WEB_ERR + 413.toShort()).toShort()
        const val WEB_ERR_UNAUTH_MAC_ADDRESS = (WEB_ERR + 600.toShort()).toShort()
        const val WEB_ERR_UNAUTH_DEVICE = (WEB_ERR + 601.toShort()).toShort()
        const val WEB_ERR_UNAUTH_USER = (WEB_ERR + 700.toShort()).toShort()
        const val WEB_ERR_UNAUTH_PASSWORD_FAIL = (WEB_ERR + 701.toShort()).toShort()
        const val WEB_ERR_PASSWORD_EXPIRED = (WEB_ERR + 702.toShort()).toShort()
        const val ERROR_ADMIN_ACCOUNT_LOCK_FOR_MINUTES = (WEB_ERR + 706.toShort()).toShort()

        const val WEB_ERR_LGU_NORMAL = (WEB_ERR + 800.toShort()).toShort()
        const val WEB_ERR_LGU_NON_PARTICIPATION_PARTY_SYSTEM = (WEB_ERR + 801.toShort()).toShort()
        const val WEB_ERR_LGU_USING_EXTERNAL_SYSTEM_PAUSED = (WEB_ERR + 802.toShort()).toShort()
        const val WEB_ERR_LGU_LOST_PHONE_STATUS = (WEB_ERR + 803.toShort()).toShort()
        const val WEB_ERR_LGU_NON_PARTICIPATION_PARTY_SERVICE = (WEB_ERR + 804.toShort()).toShort()

        const val WEB_ERR_SQL_ERROR = (WEB_ERR + 911.toShort()).toShort()

        const val CMD_ERR = 90000
        const val CMD_ERR_NOAGENT = CMD_ERR + 100
        const val CMD_ERR_TOKEN = CMD_ERR + 200
        const val CMD_ERR_COMSND = CMD_ERR + 300
        const val CMD_ERR_DUPAGENT = CMD_ERR + 400
        const val CMD_ERR_SESSION_CONNECT_FAIL = CMD_ERR + 500
        const val CMD_ERR_SESSION_SEND_FAIL = CMD_ERR + 501
        const val CMD_ERR_SESSION_SOCKET_FAIL = CMD_ERR + 502
        const val CMD_ERR_ALREADY_REMOTE_CONTROL = CMD_ERR + 503
        const val CMD_ERR_OPTION_FILE_CREATE_FAIL = CMD_ERR + 504
        const val CMD_ERR_PROCESS_EXCUTE_FAIL = CMD_ERR + 505
        const val CMD_ERR_PROCESS_KILL_FAIL = CMD_ERR + 506
        const val CMD_ERR_GET_PROCESS_LIST_FAIL = CMD_ERR + 507
        const val CMD_ERR_AGENT_RESET_CONFIG_FAIL = CMD_ERR + 508
        const val CMD_ERR_NOT_ALLOWED_IP_ADDRESS = CMD_ERR + 509
        const val CMD_ERR_GET_SCREENSHOT_FAIL = CMD_ERR + 510
        const val CMD_ERR_GET_SYSTEM_INFO_FAIL = CMD_ERR + 511
        const val CMD_ERR_AMT_EXECUTE_FAIL = CMD_ERR + 512
        const val CMD_ERR_CSSLEEP_EXECUTE_FAIL = CMD_ERR + 513
        const val CMD_ERR_CSWAKEUP_EXECUTE_FAIL = CMD_ERR + 514
        const val CMD_ERR_REMOTE_CONNECT_FAIL = CMD_ERR + 515
        const val CMD_ERR_AMT_INVALID_AUTH = CMD_ERR + 517
        const val CMD_ERR_AMT_NOT_ACCESS = CMD_ERR + 518
        const val CMD_ERR_AMT_INVALID_COMMAND = CMD_ERR + 519
        const val CMD_ERR_SYSTEM_REBOOT_FAIL = CMD_ERR + 520
        const val CMD_ERR_AGENTCONNECT_UNABLE = CMD_ERR + 521
        const val CMD_ERR_ETC = CMD_ERR + 9000

        const val NET_ERR: Short = 10000
        const val NET_ERR_BIND = (NET_ERR + 100.toShort()).toShort()
        const val NET_ERR_CONNECT = (NET_ERR + 200.toShort()).toShort()
        const val NET_ERR_HTTPRETRY = (NET_ERR + 300.toShort()).toShort()
        const val NET_ERR_MALFORMED = (NET_ERR + 400.toShort()).toShort()
        const val NET_ERR_NOROUTE = (NET_ERR + 500.toShort()).toShort()
        const val NET_ERR_PORTUNREACHABLE = (NET_ERR + 600.toShort()).toShort()
        const val NET_ERR_PROTOCOL = (NET_ERR + 700.toShort()).toShort()
        const val NET_ERR_TIMEOUT = (NET_ERR + 800.toShort()).toShort()
        const val NET_ERR_UNKNOWNSERVER = (NET_ERR + 900.toShort()).toShort()
        const val NET_ERR_UNKNOWNHOST = (NET_ERR + 110.toShort()).toShort()
        const val NET_ERR_SOCKET = (NET_ERR + 120.toShort()).toShort()
        const val NET_ERR_EXCEPTION = (NET_ERR + 130.toShort()).toShort()
        const val NET_ERR_WAS = (NET_ERR + 140.toShort()).toShort()
        const val NET_ERR_PROXYINFO_NULL = (NET_ERR + 301.toShort()).toShort()
        const val NET_ERR_PROXY_VERIFY = (NET_ERR + 302.toShort()).toShort()

        const val OTP_ERR: Short = 5000
        const val OTP_ERR_NO_INSTALL = (OTP_ERR + 100.toShort()).toShort()
        const val OTP_ERR_NO_AUTHINFO = (OTP_ERR + 101.toShort()).toShort()
        const val OTP_ERR_INVALID_USER = (OTP_ERR + 102.toShort()).toShort()
        const val OTP_ERR_FAIL_AUTH = (OTP_ERR + 103.toShort()).toShort()
        const val OTP_ERR_WRONG_PWD = (OTP_ERR + 104.toShort()).toShort()

        //	public static final String RV_REMOTEDOWNLOAD = "REMOTEDOWNLOAD";
        const val RV_REMOTECONTROL = "REMOTECONTROL"
        const val RV_REMOTEEXPLORER = "REMOTEEXPLORER"
        const val RV_GETSCREENSHOT = "GETSCREENSHOT"
        const val RV_SYSTEMLOGOFF = "SYSTEMLOGOFF"
        const val RV_SYSTEMSHUTDOWN = "SYSTEMSHUTDOWN"
        const val RV_SYSTEMREBOOT = "SYSTEMREBOOT"
        const val RV_PROCESSEXECUTE = "PROCESSEXECUTE"
        const val RV_PROCESSLIST = "PROCESSLIST"
        const val RV_PROCESSKILL = "PROCESSKILL"
        const val RV_SYSTEMINFO = "SYSTEMINFO"
        const val RV_AGENTRESTART = "AGENTRESTART"
        const val RV_REMOTEINVITE = "REMOTEINVITE"
        const val RV_AGENTUNINSTALL = "AGENTUNINSTALL"
        const val RV_WAKEONLAN = "RWAKEONLAN"
        const val RV_WAKEONLANALL = "RWAKEONLANALL"

        const val RVVIEWER: Short = 5000
        const val RVWEBSVR: Short = 3000
        const val VIEWER_PROCESS_LIST_REQUEST = (RVVIEWER + 0x03.toShort()).toShort()
        const val VIEWER_PROCESS_KILL_REQUEST = (RVVIEWER + 0x04.toShort()).toShort()
        const val VIEWER_PROCESS_EXECUTE_REQUEST = (RVVIEWER + 0x05.toShort()).toShort()
        const val VIEWER_SYSTEM_REBOOT_REQUEST = (RVVIEWER + 0x06.toShort()).toShort()
        const val VIEWER_REMOTE_CONTROL_REQUEST = (RVVIEWER + 0x07.toShort()).toShort()
        const val VIEWER_REMOTE_EXPLORER_REQUEST = (RVVIEWER + 0x08.toShort()).toShort()
        const val VIEWER_AGENT_RESTART_REQUEST = (RVVIEWER + 0x09.toShort()).toShort()
        const val VIEWER_GET_SCREENSHOT_REQUEST = (RVVIEWER + 0x0A.toShort()).toShort()
        const val VIEWER_SYSTEM_INFO_REQUEST = (RVVIEWER + 0x0B.toShort()).toShort()
        const val VIEWER_REMOTE_VPN_REQUEST = (RVVIEWER + 0x0C.toShort()).toShort()
        const val VIEWER_REMOTE_INVITE_REQUEST = (RVVIEWER + 0x0D.toShort()).toShort()
        const val WEBSVR_AGENT_AUTO_UNINSTALL = (RVWEBSVR + 0x05.toShort()).toShort()
        const val VIEWER_AGENT_WOL_RELAY = (RVVIEWER + 0x19.toShort()).toShort() // 12-0716 by csh, Wake On Lan Relay 처리

        const val SECURITYTYPE_DEFAULT: Short = 0X0000
        const val SECURITYTYPE_WEB_ID: Short = 0X0001
        const val SECURITYTYPE_WEB_PWD: Short = 0X0002
        const val SECURITYTYPE_AGENT_ID: Short = 0X0004
        const val SECURITYTYPE_AGENT_PWD: Short = 0X0008
    }
}