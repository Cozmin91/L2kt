package com.l2kt.commons.logging

import java.text.SimpleDateFormat
import java.util.logging.Formatter
import java.util.logging.LogRecord

open class MasterFormatter : Formatter() {

    override fun format(record: LogRecord): String? {
        return null
    }

    companion object {
        const val SHIFT = "\tat "
        const val CRLF = "\r\n"
        const val SPACE = "\t"

        fun getFormatedDate(timestamp: Long): String {
            return SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(timestamp)
        }
    }
}