package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.instancemanager.SevenSignsFestival
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.L2GameClient
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.CharSelectInfo
import com.l2kt.gameserver.network.serverpackets.RestartResponse
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager

class RequestRestart : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.activeEnchantItem != null || player.isLocked || player.isInStoreMode) {
            sendPacket(RestartResponse.valueOf(false))
            return
        }

        if (player.isInsideZone(ZoneId.NO_RESTART)) {
            player.sendPacket(SystemMessageId.NO_RESTART_HERE)
            sendPacket(RestartResponse.valueOf(false))
            return
        }

        if (AttackStanceTaskManager.isInAttackStance(player)) {
            player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING)
            sendPacket(RestartResponse.valueOf(false))
            return
        }

        if (player.isFestivalParticipant && SevenSignsFestival.isFestivalInitialized) {
            player.sendPacket(SystemMessageId.NO_RESTART_HERE)
            sendPacket(RestartResponse.valueOf(false))
            return
        }

        player.removeFromBossZone()

        val client = client

        // detach the client from the char so that the connection isnt closed in the deleteMe
        player.client = null

        // removing player from the world
        player.deleteMe()

        client.activeChar = null
        client.state = L2GameClient.GameClientState.AUTHED

        sendPacket(RestartResponse.valueOf(true))

        // send char list
        val cl = CharSelectInfo(client.accountName!!, client.sessionId!!.playOkID1)
        sendPacket(cl)
        client.setCharSelectSlot(cl.characterSlots)
    }
}