package com.rsupport.mobile.agent.status

class AgentStatus(private val statusContainer: StatusContainer) {

    companion object {
        /**
         * Agent 설치를 하지 않았을때
         */
        const val AGENT_STATUS_NOLOGIN: Short = 0x00

        /**
         * Agent 설치, 로그인 상태
         */
        const val AGENT_STATUS_LOGIN: Short = 0x01

        /**
         * Agent 설치, 로그아웃 상태
         */
        const val AGENT_STATUS_LOGOUT: Short = 0x02

        /**
         * 원격 제어중인 상태
         */
        const val AGENT_STATUS_REMOTING: Short = 0x03
    }

    private var statusChangedListenerList: MutableList<OnStatusChangedListener> = mutableListOf()
    private var status = statusContainer.get()

    fun addListener(statusChangedListener: OnStatusChangedListener) {
        if (!statusChangedListenerList.contains(statusChangedListener)) {
            statusChangedListenerList.add(statusChangedListener)
        }
    }

    fun remoteListener(statusChangedListener: OnStatusChangedListener) {
        if (statusChangedListenerList.contains(statusChangedListener)) {
            statusChangedListenerList.remove(statusChangedListener)
        }
    }

    fun get(): Short {
        return statusContainer.get()
    }

    fun setLoggedIn() {
        statusContainer.set(AGENT_STATUS_LOGIN)
        notifyStatus(AGENT_STATUS_LOGIN)
    }

    private fun notifyStatus(status: Short) {
        statusChangedListenerList.forEach {
            it.onChanged(status.toInt())
        }
    }

    fun setLogOut() {
        statusContainer.set(AGENT_STATUS_LOGOUT)
        notifyStatus(AGENT_STATUS_LOGOUT)
    }

    fun setRemoting() {
        statusContainer.set(AGENT_STATUS_REMOTING)
        notifyStatus(AGENT_STATUS_REMOTING)
    }

    fun clear() {
        statusContainer.clear()
        notifyStatus(statusContainer.get())
    }

    interface StatusContainer {
        fun get(): Short
        fun set(status: Short)
        fun clear()
    }

    interface OnStatusChangedListener {

        /**
         * @param status agent 의 상태
         * @see [AgentStatus.AGENT_STATUS_NOLOGIN]
         * @see [AgentStatus.AGENT_STATUS_LOGIN]
         * @see [AgentStatus.AGENT_STATUS_LOGOUT]
         * @see [AgentStatus.AGENT_STATUS_REMOTING]
         */
        fun onChanged(status: Int)
    }
}
