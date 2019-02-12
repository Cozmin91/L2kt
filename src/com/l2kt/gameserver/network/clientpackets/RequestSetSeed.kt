package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.manor.SeedProduction
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import java.util.*

class RequestSetSeed : L2GameClientPacket() {

    private var _manorId: Int = 0
    private lateinit var _items: MutableList<SeedProduction>

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

            if (itemId < 1 || sales < 0 || price < 0) {
                _items.clear()
                return
            }

            if (sales > 0)
                _items.add(SeedProduction(itemId, sales, price, sales))
        }
    }

    override fun runImpl() {
        if (_items.isEmpty())
            return

        val manor = CastleManorManager.getInstance()
        if (!manor.isModifiablePeriod) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        // Check player privileges
        val player = client.activeChar
        if (player == null || player.clan == null || player.clan.castleId != _manorId || player.clanPrivileges and Clan.CP_CS_MANOR_ADMIN != Clan.CP_CS_MANOR_ADMIN || !player.currentFolk.canInteract(
                player
            )
        ) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        // Filter seeds with start amount lower than 0 and incorrect price
        val list = ArrayList<SeedProduction>(_items.size)
        for (sp in _items) {
            val s = manor.getSeed(sp.id)
            if (s != null && sp.startAmount <= s.seedLimit && sp.price >= s.seedMinPrice && sp.price <= s.seedMaxPrice)
                list.add(sp)
        }

        // Save new list
        manor.setNextSeedProduction(list, _manorId)
    }

    companion object {
        private const val BATCH_LENGTH = 12
    }
}