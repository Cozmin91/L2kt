package com.l2kt.commons.logging.filter

import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.model.item.type.ItemType
import java.util.logging.Filter
import java.util.logging.LogRecord

class ItemFilter : Filter {

    override fun isLoggable(record: LogRecord): Boolean {
        if (record.loggerName != "item")
            return false

        val messageList = record.message.split(":").dropLastWhile { it.isEmpty() }.toTypedArray()
        if (messageList.size < 2 || !EXCLUDE_PROCESS.contains(messageList[1]))
            return true

        val item = record.parameters[1] as ItemInstance
        return !EXCLUDE_TYPE.contains(item.itemType)

    }

    companion object {
        private const val EXCLUDE_PROCESS = "Consume"
        private val EXCLUDE_TYPE : Array<ItemType> = arrayOf(EtcItemType.ARROW, EtcItemType.SHOT, EtcItemType.HERB)
    }
}