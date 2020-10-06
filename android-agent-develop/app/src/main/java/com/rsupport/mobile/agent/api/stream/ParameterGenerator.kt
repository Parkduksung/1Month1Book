package com.rsupport.mobile.agent.api.stream

import com.rsupport.util.log.RLog
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class ParameterGenerator {

    @Throws(UnsupportedEncodingException::class)
    fun generate(parameters: HashMap<String, String>): String {
        val keySet: Set<String> = parameters.keys
        val buf = StringBuffer()
        var isFirst = true

        RLog.v("========================= ParameterGenerator =========================")

        val iterator = keySet.iterator()

        while (iterator.hasNext()) {
            if (isFirst) {
                isFirst = false
            } else {
                buf.append('&')
            }
            val key = iterator.next()
            val value = parameters.get(key)

            RLog.v("***** $key : $value")

            buf.append(URLEncoder.encode(key, "UTF-8"))
            buf.append('=')
            buf.append(URLEncoder.encode(value, "UTF-8"))
        }

        RLog.v("======================================================================")
        return buf.toString()
    }
}