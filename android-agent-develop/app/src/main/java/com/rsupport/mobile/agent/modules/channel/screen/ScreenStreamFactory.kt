package com.rsupport.mobile.agent.modules.channel.screen

import com.rsupport.media.stream.ScreenStream

interface ScreenStreamFactory {
    fun create(): ScreenStream?
}