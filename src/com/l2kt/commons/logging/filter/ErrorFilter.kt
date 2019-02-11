package com.l2kt.commons.logging.filter

import java.util.logging.Filter
import java.util.logging.LogRecord

class ErrorFilter : Filter {
    override fun isLoggable(record: LogRecord): Boolean {
        return record.thrown != null
    }
}