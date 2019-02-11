package com.l2kt.commons.logging.formatter

import java.util.logging.LogRecord

import com.l2kt.commons.logging.MasterFormatter

class GMAuditFormatter : MasterFormatter() {
    override fun format(record: LogRecord): String? {
        return "[" + MasterFormatter.getFormatedDate(record.millis) + "]" + MasterFormatter.SPACE + record.message + MasterFormatter.CRLF
    }
}