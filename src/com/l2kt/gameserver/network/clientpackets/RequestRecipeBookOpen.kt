package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.RecipeBookItemList

class RequestRecipeBookOpen : L2GameClientPacket() {
    private var _isDwarven: Boolean = false

    override fun readImpl() {
        _isDwarven = readD() == 0
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.isCastingNow || player.isAllSkillsDisabled) {
            player.sendPacket(SystemMessageId.NO_RECIPE_BOOK_WHILE_CASTING)
            return
        }

        player.sendPacket(RecipeBookItemList(player, _isDwarven))
    }
}