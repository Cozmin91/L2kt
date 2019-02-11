package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.instancemanager.SevenSignsFestival
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager

class Logout : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.activeEnchantItem != null || player.isLocked) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (player.isInsideZone(ZoneId.NO_RESTART)) {
            player.sendPacket(SystemMessageId.NO_LOGOUT_HERE)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (AttackStanceTaskManager.isInAttackStance(player)) {
            player.sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (player.isFestivalParticipant && SevenSignsFestival.getInstance().isFestivalInitialized) {
            player.sendPacket(SystemMessageId.NO_LOGOUT_HERE)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        player.removeFromBossZone()
        player.logout(true)
    }
}