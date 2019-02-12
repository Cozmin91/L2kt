package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.entity.Hero

/**
 * Format chS c (id) 0xD0 h (subid) 0x0C S the hero's words :)
 * @author -Wooden-
 */
class RequestWriteHeroWords : L2GameClientPacket() {
    private var _heroWords: String = ""

    override fun readImpl() {
        _heroWords = readS()
    }

    override fun runImpl() {
        val player = client.activeChar
        if (player == null || !player.isHero)
            return

        if (_heroWords.isEmpty() || _heroWords.length > 300)
            return

        Hero.getInstance().setHeroMessage(player, _heroWords)
    }
}