package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.serverpackets.CharDeleteFail
import com.l2kt.gameserver.network.serverpackets.CharDeleteOk
import com.l2kt.gameserver.network.serverpackets.CharSelectInfo

class CharacterDelete : L2GameClientPacket() {
    private var _slot: Int = 0

    override fun readImpl() {
        _slot = readD()
    }

    override fun runImpl() {
        if (!FloodProtectors.performAction(client, FloodProtectors.Action.CHARACTER_SELECT)) {
            sendPacket(CharDeleteFail.REASON_DELETION_FAILED)
            return
        }

        when (client.markToDeleteChar(_slot).toInt()) {
            -1 // Error
            -> {
            }

            0 // Success!
            -> sendPacket(CharDeleteOk.STATIC_PACKET)

            1 -> sendPacket(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER)

            2 -> sendPacket(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED)
            else -> {
            }
        }

        val csi = CharSelectInfo(client.accountName!!, client.sessionId!!.playOkID1, 0)
        sendPacket(csi)
        client.setCharSelectSlot(csi.characterSlots)
    }
}