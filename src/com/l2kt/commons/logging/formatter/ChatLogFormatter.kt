package com.l2kt.commons.logging.formatter

import java.util.logging.LogRecord

import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.logging.MasterFormatter

class ChatLogFormatter : MasterFormatter() {
    override fun format(record: LogRecord): String? {
        val sb = StringBuilder()

        StringUtil.append(sb, "[", getFormatedDate(record.millis), "] ")

        for (p in record.parameters) {
            if (p == null)
                continue

            StringUtil.append(sb, p, " ")
        }

        StringUtil.append(sb, record.message, MasterFormatter.CRLF)

        return sb.toString()
    }
}