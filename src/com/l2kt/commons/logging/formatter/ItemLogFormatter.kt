package com.l2kt.commons.logging.formatter

import java.util.logging.LogRecord

import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.logging.MasterFormatter

import com.l2kt.gameserver.model.item.instance.ItemInstance

class ItemLogFormatter : MasterFormatter() {
    override fun format(record: LogRecord): String? {
        val sb = StringBuilder()

        StringUtil.append(
            sb,
            "[",
            MasterFormatter.getFormatedDate(record.millis),
            "] ",
            MasterFormatter.SPACE,
            record.message,
            MasterFormatter.SPACE
        )

        for (p in record.parameters) {
            if (p == null)
                continue

            if (p is ItemInstance) {

                StringUtil.append(sb, p.count, MasterFormatter.SPACE)

                if (p.enchantLevel > 0)
                    StringUtil.append(sb, "+", p.enchantLevel, " ")

                StringUtil.append(sb, p.item.name, MasterFormatter.SPACE, p.objectId)
            } else
                sb.append(p.toString())

            sb.append(MasterFormatter.SPACE)
        }
        sb.append(MasterFormatter.CRLF)

        return sb.toString()
    }
}