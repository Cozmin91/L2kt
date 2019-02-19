package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.manor.CropProcure
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import java.util.*

class RequestSetCrop : L2GameClientPacket() {

    private var _manorId: Int = 0
    private lateinit var _items: MutableList<CropProcure>

    override fun readImpl() {
        _manorId = readD()
        val count = readD()
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
            return

        _items = ArrayList(count)
        for (i in 0 until count) {
            val itemId = readD()
            val sales = readD()
            val price = readD()
            val type = readC()

            if (itemId < 1 || sales < 0 || price < 0) {
                _items.clear()
                return
            }

            if (sales > 0)
                _items.add(CropProcure(itemId, sales, type, sales, price))
        }
    }

    override fun runImpl() {
        if (_items.isEmpty())
            return

        val manor = CastleManorManager
        if (!manor.isModifiablePeriod) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        // Check player privileges
        val player = client.activeChar
        if (player?.clan == null || player.clan!!.castleId != _manorId || player.clanPrivileges and Clan.CP_CS_MANOR_ADMIN != Clan.CP_CS_MANOR_ADMIN || player.currentFolk?.canInteract(player) == false
        ) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        // Filter crops with start amount lower than 0 and incorrect price
        val list = ArrayList<CropProcure>(_items.size)
        for (cp in _items) {
            val s = manor.getSeedByCrop(cp.id, _manorId)
            if (s != null && cp.startAmount <= s.cropLimit && cp.price >= s.cropMinPrice && cp.price <= s.cropMaxPrice)
                list.add(cp)
        }

        // Save crop list
        manor.setNextCropProcure(list, _manorId)
    }

    companion object {
        private const val BATCH_LENGTH = 13
    }
}