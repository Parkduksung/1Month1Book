package com.rsupport.mobile.agent.extension


inline fun <T> T.guard(block: T.() -> Unit): T {
    if (this == null || this == false) block(); return this
}