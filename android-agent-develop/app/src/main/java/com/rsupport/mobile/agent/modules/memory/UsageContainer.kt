package com.rsupport.mobile.agent.modules.memory

class UsageContainer<out T : KeyContainer>(private val usageList: List<T>) {

    val empty: Boolean
        get() = usageList.isEmpty()

    fun find(keyObject: KeyObject): T? {
        return usageList.findLast {
            it.keyObject == keyObject
        }
    }
}

data class KeyObject(val name: String)