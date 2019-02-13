package com.l2kt.commons.logging.filter

import java.util.logging.Filter
import java.util.logging.LogRecord

import com.l2kt.commons.util.ArraysUtil

import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.model.item.type.ItemType

class ItemFilter : Filter {

    override fun isLoggable(record: LogRecord): Boolean {
        if (record.loggerName != "item")
            return false

        val messageList = record.message.split(":").dropLastWhile { it.isEmpty() }.toTypedArray()
        if (messageList.size < 2 || !EXCLUDE_PROCESS.contains(messageList[1]))
            return true

        val item = record.parameters[1] as ItemInstance
        return !ArraysUtil.contains<ItemType>(EXCLUDE_TYPE, item.itemType)

    }

    companion object {
        private val EXCLUDE_PROCESS = "Consume"
        private val EXCLUDE_TYPE = arrayOf(EtcItemType.ARROW, EtcItemType.SHOT, EtcItemType.HERB)
    }
}