package com.rsupport.mobile.agent.modules.engine

class EngineProvider(private val factory: EngineFactory) {
    fun get(): EngineTypeCheck {
        return factory.create()
    }
}
