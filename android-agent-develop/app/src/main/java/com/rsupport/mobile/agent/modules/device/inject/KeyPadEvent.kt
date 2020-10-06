package com.rsupport.mobile.agent.modules.device.inject

import com.rsupport.mobile.agent.utils.Converter

/**
 * Viewer 에서 전달 받은 data 를 KeyEvent 로 변환한다.
 */
sealed class KeyPadEvent {

    /**
     * 정상 key event 가 agent 로부터 전송 되었을때 사용된다.
     * [KeyPadEvent.from] 을 이용하여 생성한다.
     * @see KeyPadEvent.Data
     */
    data class Events(val events: List<Data>) : KeyPadEvent()

    /**
     * 사용할 수 없는 key event 일때 사용한다.
     * [KeyPadEvent.from] 을 이용하여 생성한다.
     */
    object InvalidData : KeyPadEvent()

    /**
     * KeyEvent
     * @param action [android.view.KeyEvent.ACTION_UP], [android.view.KeyEvent.ACTION_DOWN] 등의 이벤트
     * @param keyCode [android.view.KeyEvent.KEYCODE_0] 등의 이벤트
     * @see android.view.KeyEvent
     */
    data class Data(val action: Int, val keyCode: Int)


    companion object {
        /**
         * Viewer 로부터 전달 받은 keyEvent data 를 KeyPadEvent 를 생성하는 factory
         * [0] : Count - Count * (Action + KeyCode)
         * [1~2] : Action
         * [3~4] : KeyCode
         * @param data keyEvent Data
         */
        @JvmStatic
        fun from(data: ByteArray): KeyPadEvent {
            return if (!check(data)) InvalidData else {
                val eventCount = getEventCount(data)
                Events(createData(eventCount, data))
            }
        }

        /**
         * Android KeyEvent Action 과 KeyEvent 로 부터 KeyPadEvent 를 생성한다.
         */
        @JvmStatic
        fun from(action: Int, keyCode: Int): KeyPadEvent {
            return Events(listOf(Data(action, keyCode)))
        }

        private fun getEventCount(data: ByteArray): Int {
            if (data.isEmpty()) return 0
            return data[0].toInt()
        }

        private fun check(data: ByteArray): Boolean {
            return if (data.isEmpty()) false else {
                // 첫번째 byte 는 전체 keyEvent 의 갯수
                val eventCount = getEventCount(data)
                // 하나의 keyEvent 는 총 4byte 로 되어있다. index 0~1 : Action, index 2~3 : KeyEvent
                return data.size - 1 >= eventCount * 4
            }
        }

        private fun createData(eventCount: Int, data: ByteArray): List<Data> {
            return mutableListOf<Data>().apply {
                for (index in 1 until eventCount * 4 step 4) {
                    add(Data(Converter.readShortLittleEndian(data, index).toInt(), Converter.readShortLittleEndian(data, index + 2).toInt()))
                }
            }
        }
    }
}