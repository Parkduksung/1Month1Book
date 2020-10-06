package com.rsupport.mobile.agent.utils

import com.rsupport.util.log.RLog
import com.rsupport.util.log.printer.ILogPrinter

class IgnoreLogPrinter : ILogPrinter {

    override fun print(tag: String, p1: RLog.Level?, message: String) {}

    override fun getName(): String = ""

    override fun setMinimumPrintLevel(p0: RLog.Level?) {}

}