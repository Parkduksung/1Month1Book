package com.rsupport.mobile.agent.api

import com.rsupport.mobile.agent.api.model.*
import com.rsupport.mobile.agent.utils.Result

interface ApiService {
    companion object {
        const val KNOX_BLACK_LIST_PARAM_ERROR = 111

        // 단말 업데이트 필요
        const val KNOX_BLACK_LIST_MUST_UPDATE_ERROR = 854

        // 미지원 단말
        const val KNOX_BLACK_LIST_NOT_SUPPORT_ERROR = 855
    }

    fun consoleLogin(corpID: String?, userID: String, userPwd: String, isSecure: Boolean = true): Result<ConsoleLoginResult>

    /**
     * Knox 를 지원하는 단말인지를 확인한다.
     * 지원하지 않는 단말일경우 [Result.Failure.throwable] 이 RSException 이고 errorCodr 가 [ApiService.KNOX_BLACK_LIST_MUST_UPDATE_ERROR] 일경우
     * 단말을 업데이트하면 지원 가능하다.
     * @return Knox 를 지원하면 [Result.Success], 그렇지 않으면 [Result.Failure]
     */
    fun reqeustCheckSupportKnox(): Result<CheckSupportKnoxResult>

    /**
     * Agent GroupList 를 조회한다.
     */
    fun getAgentGroupList(lType: String, bizID: String): Result<List<GroupInfo>>

    /**
     * Agent SubGroupList 를 조회한다.
     */
    fun getAgentSubGroupList(lType: String, bizID: String): Result<List<GroupInfo>>

    /**
     * Agent Group 을 검색한다.
     */
    fun getGroupSearch(lType: String, cType: String, searchText: String, parentGroupId: String): Result<List<GroupInfo>>

    /**
     * Agent 를 삭제한다.
     */
    fun deleteAgent(guid: String, userId: String, userPwd: String, bizId: String): Result<AgentDeleteResult>

    /**
     * Agent 로그인을 한다.
     */
    fun agentLogin(guid: String): Result<AgentLoginResult>

    /**
     * Agent 를 로그아웃한다.
     */
    fun agentLogout(guid: String): Result<AgentLogoutResult>

    /**
     * Agent 접속이 완료되었음을 서버에 알린다.
     * @param guid 접속 GUID
     * @param loginKey 접속 요청시(MQTT 5007) 전송 받은 loginKey
     * @param encoderType [EncoderType]
     */
    fun notifyConnected(guid: String, loginKey: String, encoderType: EncoderType): Result<NotifyConnectedResult>

    /**
     * Agent 접속 종료되었음을 서버에 알린다.
     * @param guid agentGuid
     * @param loginKey 접속 요청시(MQTT 5007) 전송 받은 loginKey
     */
    fun notifyDisconnected(guid: String, logKey: String): Result<NotifyDisconnectedResult>

    /**
     * FCM register id 를 등록한다.
     * @param guid agent Guid
     * @param registerID fcm register key
     *
     * @see [com.rsupport.mobile.agent.modules.push.fcm.MyFirebaseInstanceIDService]
     * @see [com.rsupport.mobile.agent.modules.push.fcm.MyFirebaseMessagingService]
     */
    fun registerFcmId(guid: String, registerID: String): Result<FcmRegisterResult>


    /**
     * @param guid agent Guid
     * @param id 로그인 id
     * @param pass 로그인 password
     * @param name agentName
     * @param agentBizID [consoleLogin] 호출후 받아오는 bizid [ConsoleLoginResult.bizID]
     */
    fun agentInstall(guid: String, id: String, pass: String, agentName: String, agentBizID: String?): Result<AgentInstallResult>


    /**
     * 연결 동의 결과를 업데이트하낟
     * @param guid agentGuid
     * @param logKey 서버로부터 전송받은 logkey
     * @param result "1" 수락, "0" 거절
     * @param url 서버로부터 받은 url
     * @see [com.rsupport.mobile.agent.service.command.AgentCommand5048]
     */
    fun agentConnectAgreeResult(guid: String, logKey: String, result: String, url: String): Result<ConnectAgreeResult>

    /**
     * @param accessId 디바이스 접근 아이디
     * @param accessPass 디바이스 접근 비밀번호
     * @param bizId [consoleLogin] 호출후 받아오는 bizid [ConsoleLoginResult.bizID]
     */
    fun checkAccessIDValidate(accessId: String, accessPass: String, bizId: String): Result<CheckAccessIDValidateResult>

    /**
     * 원격제어 준비가 상태를 업데이트한다.
     * @param guid Agent Guid
     * @param result 성공 '0' 실패 '1"
     * @param sessionIP MQTT Push 서버 address
     * @param sessionPort MQTT Push 서버 port
     */
    fun agentSessionResult(guid: String, result: String, sessionIP: String, sessionPort: String): Result<AgentSessionResult>

    /**
     * Knox 키를 조회한다
     * @param appVersion Agent app version name
     */
    fun requestKnoxEnterpriseKey(appVersion: String): Result<KnoxKeyResult>

    /**
     * @param guid agentGuid
     * @param newDeviceName 새로운 deviceName
     * @param ssl "0"이면 SSL 사용안함, "1" 이면 SSL 사용
     * @param localIp agent ip
     * @param macAddress agent mac address
     */
    fun deviceNameChange(guid: String, newDeviceName: String, ssl: String, localIp: String, macAddress: String): Result<ChangeDeviceNameResult>

    /**
     * @param guid AgentGuid
     * @param oldId 이전 접속 아이디
     * @param oldPasswd 이전 접속 비밀번호
     * @param newId 새로운 접속 아이디
     * @param newPasswd 새로운 접속 비밀번호
     */
    fun agentAccountChange(guid: String, oldId: String, oldPasswd: String, newId: String, newPasswd: String): Result<AccountChangeResult>

    /**
     * LiveView 를 서버에 업로드한다
     * @param server serverURL
     * @param page pageURL
     * @param port port
     * @param guid agentGuid
     * @param width imageWidth
     * @param height imageHeight
     * @param imagePath image file path
     * @param filePath 저정될 위치
     */
    fun sendLiveView(server: String, page: String, port: Int, guid: String, width: String, height: String, imagePath: String, filePath: String): Result<SendLiveViewResult>

}

enum class EncoderType(val type: String) {
    /**
     * 사용하지 않음.
     */
    VRVD("VRVD"),

    /**
     * [EngineConfigSetting.isSoftEncoding]
     * apilevel > 16 && !EngineConfigSetting.isSoftEncoding 일때만 사용한다.
     */
    HXENGINE("HXENGINE"),

    /**
     * H264 Encoder
     */
    XENC("XENC"),
}



