package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.RaidPointManager
import com.l2kt.gameserver.network.serverpackets.ExGetBossRecord

class RequestGetBossRecord : L2GameClientPacket() {
    private var _bossId: Int = 0

    override fun readImpl() {
        _bossId = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val points = RaidPointManager.getInstance().getPointsByOwnerId(player.objectId)
        val ranking = RaidPointManager.getInstance().calculateRanking(player.objectId)
        val list = RaidPointManager.getInstance().getList(player)

        player.sendPacket(ExGetBossRecord(ranking, points, list))
    }
}