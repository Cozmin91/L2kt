package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.serverpackets.*

class RequestGMCommand : L2GameClientPacket() {
    private var _targetName: String = ""
    private var _command: Int = 0

    override fun readImpl() {
        _targetName = readS()
        _command = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        // prevent non gm or low level GMs from viewing player stuff
        if (!activeChar.isGM || !activeChar.accessLevel.allowAltG)
            return

        val target = World.getPlayer(_targetName)
        val clan = ClanTable.getClanByName(_targetName)

        if (target == null && (clan == null || _command != 6))
            return

        when (_command) {
            1 // target status
            -> {
                sendPacket(GMViewCharacterInfo(target!!))
                sendPacket(GMViewHennaInfo(target))
            }

            2 // target clan
            -> if (target?.clan != null)
                sendPacket(GMViewPledgeInfo(target.clan!!, target))

            3 // target skills
            -> sendPacket(GMViewSkillInfo(target!!))

            4 // target quests
            -> sendPacket(GMViewQuestList(target!!))

            5 // target inventory
            -> {
                sendPacket(GMViewItemList(target!!))
                sendPacket(GMViewHennaInfo(target))
            }

            6 // player or clan warehouse
            -> if (target != null)
                sendPacket(GMViewWarehouseWithdrawList(target))
            else
                sendPacket(GMViewWarehouseWithdrawList(clan!!))
        }
    }
}