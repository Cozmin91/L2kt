package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.AllianceInfo
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestAllyInfo : L2GameClientPacket() {
    public override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        var sm: SystemMessage

        val allianceId = activeChar.allyId
        if (allianceId > 0) {
            val ai = AllianceInfo(allianceId)
            activeChar.sendPacket(ai)

            // send for player
            sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_INFO_HEAD)
            activeChar.sendPacket(sm)

            sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_NAME_S1)
            sm.addString(ai.name)
            activeChar.sendPacket(sm)

            sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1)
            sm.addString(ai.leaderC)
            sm.addString(ai.leaderP)
            activeChar.sendPacket(sm)

            sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2)
            sm.addNumber(ai.online)
            sm.addNumber(ai.total)
            activeChar.sendPacket(sm)

            sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1)
            sm.addNumber(ai.allies.size)
            activeChar.sendPacket(sm)

            sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_HEAD)
            for (aci in ai.allies) {
                activeChar.sendPacket(sm)

                sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_NAME_S1)
                sm.addString(aci.clan.name)
                activeChar.sendPacket(sm)

                sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEADER_S1)
                sm.addString(aci.clan.leaderName)
                activeChar.sendPacket(sm)

                sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEVEL_S1)
                sm.addNumber(aci.clan.level)
                activeChar.sendPacket(sm)

                sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2)
                sm.addNumber(aci.online)
                sm.addNumber(aci.total)
                activeChar.sendPacket(sm)

                sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_SEPARATOR)
            }

            sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_FOOT)
            activeChar.sendPacket(sm)
        } else
            activeChar.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES)
    }
}