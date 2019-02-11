package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.serverpackets.CharSelectInfo

class CharacterRestore : L2GameClientPacket() {
    private var _slot: Int = 0

    override fun readImpl() {
        _slot = readD()
    }

    override fun runImpl() {
        if (!FloodProtectors.performAction(client, FloodProtectors.Action.CHARACTER_SELECT))
            return

        client.markRestoredChar(_slot)

        val csi = CharSelectInfo(client.accountName!!, client.sessionId!!.playOkID1, 0)
        sendPacket(csi)
        client.setCharSelectSlot(csi.characterSlots)
    }
}