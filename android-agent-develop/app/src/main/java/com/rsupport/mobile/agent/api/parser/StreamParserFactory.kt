package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.utils.Result
import java.io.InputStream

interface StreamParserFactory {
    fun <T, M> create(clazz: Class<T>): StreamParser<M>
}

interface StreamParser<T> {
    fun parse(inputStream: InputStream): Result<T>
}

class DefaultStreamParserFactory : StreamParserFactory {
    override fun <T, M> create(clazz: Class<T>): StreamParser<M> {
        return clazz.newInstance() as StreamParser<M>
    }
}

