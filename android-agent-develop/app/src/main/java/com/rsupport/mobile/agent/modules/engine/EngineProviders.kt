package com.rsupport.mobile.agent.modules.engine

import android.content.Context

class EngineProviders {
    companion object {
        @JvmStatic
        fun of(context: Context): EngineProvider {
            return EngineProvider(DefaultEngineFactory(context))
        }

        @JvmStatic
        fun of(context: Context, factory: EngineFactory): EngineProvider {
            return EngineProvider(factory)
        }
    }
}
