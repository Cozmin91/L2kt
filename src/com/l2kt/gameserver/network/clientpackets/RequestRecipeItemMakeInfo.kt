package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.RecipeItemMakeInfo

class RequestRecipeItemMakeInfo : L2GameClientPacket() {
    private var _id: Int = 0

    override fun readImpl() {
        _id = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        activeChar.sendPacket(RecipeItemMakeInfo(_id, activeChar))
    }
}