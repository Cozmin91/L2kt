package com.l2kt.commons.logging.formatter

import java.util.logging.LogRecord

import com.l2kt.commons.logging.MasterFormatter

class FileLogFormatter : MasterFormatter() {
    override fun format(record: LogRecord): String? {
        return "[" + MasterFormatter.getFormatedDate(record.millis) + "]" + MasterFormatter.SPACE + record.level.name + MasterFormatter.SPACE + record.message + MasterFormatter.CRLF
    }
}