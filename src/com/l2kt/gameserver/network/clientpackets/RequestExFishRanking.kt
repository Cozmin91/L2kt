package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.FishingChampionshipManager

class RequestExFishRanking : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
            FishingChampionshipManager.getInstance().showMidResult(player)
    }
}