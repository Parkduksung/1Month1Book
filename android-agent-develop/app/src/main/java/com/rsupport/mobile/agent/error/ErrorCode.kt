package com.rsupport.mobile.agent.error

interface ErrorCode {
    companion object {
        private const val START_INDEX = 60000

        // 에이전트 등록 66100~66199
        private const val LOGIN_INDEX = START_INDEX + 6100
        const val LOGIN_INVALID_URL = LOGIN_INDEX // URL 주소가 유효하지 않다.
        const val LOGIN_EMPTY_ID = LOGIN_INDEX + 1 // 아이디가 입력되지 않았다.
        const val LOGIN_EMPTY_PWD = LOGIN_INDEX + 2 // 비밀번호가 입력되지 않았다.
        const val LOGIN_EXPIRED_PWD = LOGIN_INDEX + 3 // 비밀번호사용 기간 만료
        const val LOGIN_INVALID_ID_AES = LOGIN_INDEX + 4 // [AES 암호화]등록되지 않은 ID
        const val LOGIN_INVALID_USER_ACCOUNT_OR_PWD = LOGIN_INDEX + 5 // [AES 암호화]등록되지 않은 ID
        const val LOGIN_FORCE_UPDATE = LOGIN_INDEX + 6 // 강제 업데이트
        const val LOGIN_UPGRADE_MEMBER = LOGIN_INDEX + 7 // member upgrade 확인
        const val LOGIN_NET_ERR_PROXY_VERIFY = LOGIN_INDEX + 8 // Proxy 설정오류

        // 설정 66200~66299
        private const val SETTING_INDEX = START_INDEX + 6200
        const val SETTING_INVALID_ACCOUNT_OR_PWD = SETTING_INDEX + 1 // id 또는 pwd 가 다르다.
        const val SETTING_ALREADY_DELETE_AGENT = SETTING_INDEX + 2 // Agent deleted 에이전트가 서버에서 이미 삭제되었다.
        const val SETTING_NET_ERR_PROXY_VERIFY = SETTING_INDEX + 3 // Proxy 설정오류

        // Engine 오류 68100~68199
        private const val ENGINE_INDEX = START_INDEX + 8100
        const val ENGINE_KNOX_BLACK_LIST_WEB_111 = ENGINE_INDEX + 10 // Knox black list 파라미터가 부족하거나, 잘못된 경우
        const val ENGINE_KNOX_BLACK_LIST_WEB_854 = ENGINE_INDEX + 11 // Knox black list 단말업데이트 필요
        const val ENGINE_KNOX_BLACK_LIST_WEB_855 = ENGINE_INDEX + 12 // Knox black list 지원불가능단말

        const val ENGINE_FIND_WEB_ERROR = ENGINE_INDEX + 90 // Engine 찾는중 웹서버 오류
        const val ENGINE_NOT_SUPPORTED = ENGINE_INDEX + 99 // Engine 지원하지 않음.


        const val UNKNOWN_ERROR = 69999 // 알수 없는 오류
    }
}








