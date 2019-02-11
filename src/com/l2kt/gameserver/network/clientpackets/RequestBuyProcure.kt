package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.holder.IntIntHolder
import java.util.*

class RequestBuyProcure : L2GameClientPacket() {

    private var _manorId: Int = 0
    private var _items: MutableList<IntIntHolder>? = null

    override fun readImpl() {
        _manorId = readD()

        val count = readD()
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
            return

        _items = ArrayList(count)
        for (i in 0 until count) {
            readD() // service
            val itemId = readD()
            val cnt = readD()

            if (itemId < 1 || cnt < 1) {
                _items = null
                return
            }

            _items!!.add(IntIntHolder(itemId, cnt))
        }
    }

    override fun runImpl() {
        L2GameClientPacket.LOGGER.warn(
            "RequestBuyProcure: normally unused, but infos found for manorId {}.",
            _manorId
        )
    }

    companion object {
        private const val BATCH_LENGTH = 8
    }
}