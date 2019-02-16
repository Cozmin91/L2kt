package com.l2kt.gameserver.handler.chathandlers

import com.l2kt.gameserver.handler.IChatHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.FloodProtectors.Action
import com.l2kt.gameserver.network.serverpackets.CreatureSay

class ChatHeroVoice : IChatHandler {

    override fun handleChat(type: Int, activeChar: Player, target: String, text: String) {
        if (!activeChar.isHero)
            return

        if (!FloodProtectors.performAction(activeChar.client, Action.HERO_VOICE))
            return

        val cs = CreatureSay(activeChar.objectId, type, activeChar.name, text)
        for (player in World.players)
            player.sendPacket(cs)
    }

    override val chatTypeList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(17)
    }
}