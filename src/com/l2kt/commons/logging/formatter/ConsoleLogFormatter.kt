package com.l2kt.commons.logging.formatter

import java.io.PrintWriter
import java.io.StringWriter
import java.util.logging.LogRecord

import com.l2kt.commons.logging.MasterFormatter

class ConsoleLogFormatter : MasterFormatter() {
    override fun format(record: LogRecord): String? {
        val sw = StringWriter()
        sw.append(record.message)
        sw.append(MasterFormatter.CRLF)

        val throwable = record.thrown
        throwable?.printStackTrace(PrintWriter(sw))

        return sw.toString()
    }
}